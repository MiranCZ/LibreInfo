from flask import Flask, request
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
import requests

SCRAPER_URL = "http://scraper:8080"
GTFS_STATIC_URL = "http://gtfs-static:8080"
GTFS_RT_URL = "http://gtfs-rt:8080"
NEWS_URL = "http://news:8080"

api = Flask(__name__)
# TODO figure out what limits make sense here
limiter = Limiter(
    get_remote_address,
    app=api,
    default_limits=["500 per hour", "40 per minute"],
    storage_uri="memory://",
)

scraper = requests.Session()
gtfs_static = requests.Session()
gtfs_rt = requests.Session()


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
    resp = gtfs_static.get(f"{GTFS_STATIC_URL}/shapes", params=request.args)
    return resp.content, resp.status_code, {"Content-Type": "application/json"}

@api.route('/news', methods=['GET'])
def get_news():
    resp = gtfs_static.get(f"{NEWS_URL}/news", params=request.args)
    return resp.content, resp.status_code, {"Content-Type": "application/json"}

@api.route("/vehicles", methods=["GET"])
def get_vehicles():
    resp = gtfs_rt.get(f"{GTFS_RT_URL}/vehicles")
    return resp.content, resp.status_code, {"Content-Type": "application/json"}


@api.route("/vehicleinfo", methods=["GET"])
def get_vehicle_info():
    resp = gtfs_rt.get(f"{GTFS_RT_URL}/vehicleinfo", params=request.args)
    return resp.content, resp.status_code, {"Content-Type": "application/json"}


@api.route("/stopdelays", methods=["GET"])
def get_stop_delays():
    resp = gtfs_rt.get(f"{GTFS_RT_URL}/stopdelays", params=request.args)
    return resp.content, resp.status_code, {"Content-Type": "application/json"}


@api.route("/routedelays", methods=["GET"])
def get_route_delays():
    resp = gtfs_rt.get(f"{GTFS_RT_URL}/routedelays")
    return resp.content, resp.status_code, {"Content-Type": "application/json"}
