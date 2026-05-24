from pathlib import Path

GTFS_URL = "https://kordis-jmk.cz/gtfs/gtfs.zip"
OSM_URL = "https://download.geofabrik.de/europe/czech-republic/jihomoravsky-latest.osm.pbf"

DATA_DIR = Path("/data")

RAW_DIR = DATA_DIR / "raw"
PROCESSED_DIR = DATA_DIR / "processed"
OSM_DIR = DATA_DIR / "osm"

GTFS_PATH = RAW_DIR / "gtfs.zip"
OSM_PATH = OSM_DIR / "region.osm.pbf"

GTFS_ETAG_FILE = RAW_DIR / ".gtfs-etag"
GTFS_HASH_FILE = RAW_DIR / ".gtfs-hash"
OSM_HASH_FILE = OSM_DIR / ".osm-hash"