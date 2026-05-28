from flask import Flask, jsonify, request
import csv
import logging
import os.path

server = Flask(__name__)

trip_shapes = {}

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s  %(levelname)-8s  %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
log = logging.getLogger(__name__)

@server.route('/shapes', methods=['GET'])
def get_events():
    trip_id = request.args.get("tripId")

    if trip_id is None:
        return jsonify({"error":"You need to supply the 'tripId' parameter"})

    try:
        trip_id = int(trip_id)
    except ValueError:
        return jsonify({"error":f"Invalid input '{trip_id}'"})

    return jsonify(trip_shapes.get(trip_id, []))


def parse_shapes() -> dict[str, list]:
    result = {}

    with open('/data/shapes.txt', newline='') as csvfile:
        reader = csv.DictReader(csvfile, delimiter=',')

        prev_shape = None
        current = []
        for row in reader:
            shape_id = row['shape_id']
            if prev_shape is not None and shape_id != prev_shape:
                result[shape_id] = current
                current = []

            lat = float(row['shape_pt_lat'])
            lon = float(row['shape_pt_lon'])
            current.append((lat, lon))
            prev_shape = shape_id

    log.info(f"Parsed {len(result)} shapes")
    return result

def parse_trips():
    if not os.path.isfile('/data/trips.txt') or not os.path.isfile('/data/shapes.txt'):
        log.warning("GTFS files not present, exiting!")
        return

    global trip_shapes

    log.info("Parsing static trip shape data...")

    shapes = parse_shapes()

    with open('/data/trips.txt', newline='') as csvfile:
        reader = csv.DictReader(csvfile, delimiter=',')
        for row in reader:
            trip_id = int(row['trip_id'])
            trip_shapes[trip_id] = shapes.get(row['shape_id'], [])

    log.info(f"Created shape map with {len(trip_shapes)} entries")


parse_trips()
