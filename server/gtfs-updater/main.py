import logging
import sys

import docker
import httpx

from config import *
from util import md5_of_file, stream_to_file

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s  %(levelname)-8s  %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
log = logging.getLogger(__name__)


def check_and_download_gtfs() -> bool:
    """Download GTFS feed if changed. Returns True if a new file was written."""
    log.info(f"Checking GTFS feed: {GTFS_URL}")

    headers: dict[str, str] = {}
    if GTFS_ETAG_FILE.exists():
        headers["If-None-Match"] = GTFS_ETAG_FILE.read_text().strip()

    with httpx.stream("GET", GTFS_URL, headers=headers, follow_redirects=True, timeout=120) as r:
        if r.status_code == 304:
            log.info("GTFS: 304 Not Modified. Nothing to do")
            return False

        r.raise_for_status()
        new_etag = r.headers.get("etag")

        log.info("GTFS: downloading...")
        stream_to_file(r, GTFS_PATH)

    log.info(f"GTFS: downloaded ({GTFS_PATH.stat().st_size / 1_048_576:.1f} MB)")

    new_hash = md5_of_file(GTFS_PATH)
    old_hash = GTFS_HASH_FILE.read_text().strip() if GTFS_HASH_FILE.exists() else ""

    if new_hash == old_hash:
        log.info("GTFS: content hash unchanged. Skipping update")
        return False

    GTFS_HASH_FILE.write_text(new_hash)
    if new_etag:
        GTFS_ETAG_FILE.write_text(new_etag)
    else:
        GTFS_ETAG_FILE.unlink(missing_ok=True)

    log.info("GTFS: changed, will process")
    return True


def check_and_download_osm() -> bool:
    """Download OSM extract from Geofabrik if changed. Returns True if updated."""
    md5_url = OSM_URL + ".md5"
    log.info(f"Checking OSM extract via: {md5_url}")

    r = httpx.get(md5_url, timeout=30)
    r.raise_for_status()

    # Geofabrik .md5 format: "<hash>  <filename>\n"
    new_hash = r.text.strip().split()[0]
    old_hash = OSM_HASH_FILE.read_text().strip() if OSM_HASH_FILE.exists() else ""

    if new_hash == old_hash:
        log.info("OSM: hash unchanged. Nothing to do")
        return False

    log.info(f"OSM: hash changed ({old_hash[:8] or 'none'} → {new_hash[:8]}), downloading...")

    with httpx.stream("GET", OSM_URL, follow_redirects=True, timeout=600) as r:
        r.raise_for_status()
        stream_to_file(r, OSM_PATH)

    actual_hash = md5_of_file(OSM_PATH)
    if actual_hash != new_hash:
        raise RuntimeError(f"OSM download corrupted: expected {new_hash}, got {actual_hash}")

    OSM_HASH_FILE.write_text(new_hash)
    log.info(f"OSM: downloaded ({OSM_PATH.stat().st_size / 1_048_576:.1f} MB)")
    return True


def run_pfaedle(docker_client: docker.DockerClient) -> None:
    log.info("Running pfaedle...")

    PROCESSED_DIR.mkdir(parents=True, exist_ok=True)

    container = docker_client.containers.get("pfaedle")
    container.start()

    for line in container.logs(stream=True, follow=True):
        log.info(f"[pfaedle] {line.decode().rstrip()}")

    exit_code = container.wait()["StatusCode"]
    if exit_code != 0:
        raise RuntimeError(f"pfaedle exited with code {exit_code}")

    log.info("pfaedle finished successfully")


def run_motis_import(docker_client: docker.DockerClient) -> None:
    log.info("Running MOTIS import...")

    container = docker_client.containers.get("motis-import")
    container.start()

    for line in container.logs(stream=True, follow=True):
        log.info(f"[motis-import] {line.decode().rstrip()}")

    exit_code = container.wait()["StatusCode"]
    if exit_code != 0:
        raise RuntimeError(f"motis-import exited with code {exit_code}")

    log.info("MOTIS import finished successfully")


def run_gtfs_parser(docker_client: docker.DockerClient) -> None:
    log.info("Running gtfs-parser...")

    container = docker_client.containers.get("gtfs-parser")
    container.start()

    for line in container.logs(stream=True, follow=True):
        log.info(f"[gtfs-parser] {line.decode().rstrip()}")

    exit_code = container.wait()["StatusCode"]
    if exit_code != 0:
        raise RuntimeError(f"gtfs-parser exited with code {exit_code}")

    log.info("gtfs-parser finished successfully")


def ensure_running(docker_client: docker.DockerClient, name: str) -> bool:
    """Start container if not already running. Returns True if it was started."""
    container = docker_client.containers.get(name)
    if container.status != "running":
        log.info(f"{name}: not running (status={container.status}), starting...")
        container.start()
        log.info(f"{name}: started")
        return True
    return False


def restart_motis(docker_client: docker.DockerClient) -> None:
    log.info("Restarting Motis...")
    docker_client.containers.get("motis").restart()
    log.info("Motis restarted")


def restart_static_api(docker_client: docker.DockerClient) -> None:
    log.info("Restarting gtfs-static...")
    docker_client.containers.get("gtfs-static").restart()
    log.info("gtfs-static restarted")


def main() -> None:
    log.info("Updater starting")

    RAW_DIR.mkdir(parents=True, exist_ok=True)

    try:
        gtfs_changed = check_and_download_gtfs()
        osm_changed = check_and_download_osm()
    except Exception as e:
        log.error(f"Download failed: {e}")
        sys.exit(1)

    try:
        docker_client = docker.from_env()

        if not gtfs_changed and not osm_changed:
            log.info("Nothing changed. Ensuring containers are running...")
            ensure_running(docker_client, "gtfs-static")
            if not MOTIS_IMPORTED_FILE.exists():
                log.info("MOTIS not imported yet, bootstrapping import...")
                run_motis_import(docker_client)
                MOTIS_IMPORTED_FILE.write_text("ok")
                restart_motis(docker_client)
            else:
                ensure_running(docker_client, "motis")
            if not PARSER_DONE_FILE.exists():
                log.info("gtfs-parser has not run yet, bootstrapping...")
                run_gtfs_parser(docker_client)
                PARSER_DONE_FILE.write_text("ok")
            log.info("Done")
            return

        reasons = []
        if gtfs_changed:
            reasons.append("GTFS feed updated")
        if osm_changed:
            reasons.append("OSM extract updated")
        log.info(f"Changes detected: {', '.join(reasons)}")

        if gtfs_changed:
            run_gtfs_parser(docker_client)
            PARSER_DONE_FILE.write_text("ok")

        run_pfaedle(docker_client)
        run_motis_import(docker_client)
        MOTIS_IMPORTED_FILE.write_text("ok")
        restart_motis(docker_client)
        restart_static_api(docker_client)
    except Exception as e:
        log.error(f"Update failed during processing: {e}")
        sys.exit(1)

    log.info("Update complete")


if __name__ == "__main__":
    main()
