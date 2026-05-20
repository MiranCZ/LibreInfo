from flask import Flask, jsonify, request
import requests

SCRAPER_URL = "http://scraper:8080"
GTFS_STATIC_URL = "http://gtfs-static:8090"

api = Flask(__name__)
scraper = requests.Session()
gtfs_static = requests.Session()


@api.route('/diversions', methods=['GET'])
def get_diversions():
    resp = scraper.get(f"{SCRAPER_URL}/diversions")
    return resp.content, resp.status_code, {"Content-Type": "application/json"}


@api.route('/events', methods=['GET'])
def get_events():
    resp = scraper.get(f"{SCRAPER_URL}/events")
    return resp.content, resp.status_code, {"Content-Type": "application/json"}

@api.route('/shapes', methods=['GET'])
def get_shapes():
    resp = gtfs_static.get(f"{GTFS_STATIC_URL}/shapes")
    return resp.content, resp.status_code, {"Content-Type": "application/json"}

@api.route("/vehicles", methods=["GET"])
def get_vehicles():
    return jsonify([])


@api.route("/vehicleinfo", methods=["GET"])
def get_vehicle_info():
    return jsonify({})


@api.route("/stopdelays", methods=["GET"])
def get_stop_delays():
    return jsonify({})


@api.route("/routedelays", methods=["GET"])
def get_route_delays():
    return jsonify({})
