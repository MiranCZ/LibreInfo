import logging
import socket
import sys

import docker
import httpx
from docker.models.containers import Container

from config import *
from util import md5_of_file, stream_to_file

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s  %(levelname)-8s  %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
log = logging.getLogger(__name__)


class Compose:
    """Resolves Compose services to their containers by label.

    Lookups are scoped to our own project, so no service needs an explicit
    `container_name` and several copies of the stack can share a host.
    """

    def __init__(self, client: docker.DockerClient) -> None:
        self.client = client

        try:
            me = client.containers.get(socket.gethostname())
        except docker.errors.NotFound:
            raise RuntimeError(
                "cannot resolve our own container by hostname; "
                "do not override `hostname:` for this service"
            ) from None

        project = me.labels.get("com.docker.compose.project")
        if not project:
            raise RuntimeError("not running under Docker Compose: no project label")

        self.project = project
        log.info(f"Compose project: {project}")

    def get(self, service: str) -> Container:
        containers = self.client.containers.list(
            all=True,
            filters={
                "label": [
                    f"com.docker.compose.project={self.project}",
                    f"com.docker.compose.service={service}",
                ]
            },
        )
        if not containers:
            raise RuntimeError(f"no container for service '{service}' in project '{self.project}'")
        return containers[0]


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


def run_motis_import(compose: Compose) -> None:
    log.info("Running MOTIS import...")

    container = compose.get("motis-import")
    container.start()

    for line in container.logs(stream=True, follow=True):
        log.info(f"[motis-import] {line.decode().rstrip()}")

    exit_code = container.wait()["StatusCode"]
    if exit_code != 0:
        raise RuntimeError(f"motis-import exited with code {exit_code}")

    log.info("MOTIS import finished successfully")


def run_gtfs_parser(compose: Compose) -> None:
    log.info("Running gtfs-parser...")

    container = compose.get("gtfs-parser")
    container.start()

    for line in container.logs(stream=True, follow=True):
        log.info(f"[gtfs-parser] {line.decode().rstrip()}")

    exit_code = container.wait()["StatusCode"]
    if exit_code != 0:
        raise RuntimeError(f"gtfs-parser exited with code {exit_code}")

    log.info("gtfs-parser finished successfully")

    reload_gtfs_rt()


def reload_gtfs_rt() -> None:
    """Make gtfs-rt pick up the id tables gtfs-parser just wrote."""
    url = f"{GTFS_RT_URL}/internal/reload"
    log.info("Reloading gtfs-rt id tables...")

    try:
        r = httpx.post(url, timeout=30)
    except httpx.TransportError as e:
        # not running (yet): it reads the tables itself when it starts
        log.warning(f"gtfs-rt unreachable, skipping reload ({e!r})")
        return

    r.raise_for_status()
    log.info(f"gtfs-rt reloaded: {r.text}")


def ensure_running(compose: Compose, service: str) -> bool:
    """Start container if not already running. Returns True if it was started."""
    container = compose.get(service)
    if container.status != "running":
        log.info(f"{service}: not running (status={container.status}), starting...")
        container.start()
        log.info(f"{service}: started")
        return True
    return False


def restart_motis(compose: Compose) -> None:
    log.info("Restarting Motis...")
    compose.get("motis").restart()
    log.info("Motis restarted")


def restart_static_api(compose: Compose) -> None:
    log.info("Restarting gtfs-static...")
    compose.get("gtfs-static").restart()
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
        compose = Compose(docker.from_env())

        if not gtfs_changed and not osm_changed:
            log.info("Nothing changed. Ensuring containers are running...")
            ensure_running(compose, "gtfs-static")
            # motis reads the zip written by gtfs-parser, so that has to come first
            if not PARSER_DONE_FILE.exists():
                log.info("gtfs-parser has not run yet, bootstrapping...")
                run_gtfs_parser(compose)
                PARSER_DONE_FILE.write_text("ok")
            if not MOTIS_IMPORTED_FILE.exists():
                log.info("MOTIS not imported yet, bootstrapping import...")
                run_motis_import(compose)
                MOTIS_IMPORTED_FILE.write_text("ok")
                restart_motis(compose)
            else:
                ensure_running(compose, "motis")
            log.info("Done")
            return

        reasons = []
        if gtfs_changed:
            reasons.append("GTFS feed updated")
        if osm_changed:
            reasons.append("OSM extract updated")
        log.info(f"Changes detected: {', '.join(reasons)}")

        if gtfs_changed or not PARSER_DONE_FILE.exists():
            run_gtfs_parser(compose)
            PARSER_DONE_FILE.write_text("ok")

        run_motis_import(compose)
        MOTIS_IMPORTED_FILE.write_text("ok")
        restart_motis(compose)
        restart_static_api(compose)
    except Exception as e:
        log.error(f"Update failed during processing: {e}")
        sys.exit(1)

    log.info("Update complete")


if __name__ == "__main__":
    main()
