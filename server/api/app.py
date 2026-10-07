from flask import Flask, request, jsonify
import requests

from motis_bridge import translate_plan

SCRAPER_URL = "http://scraper:8080"
GTFS_STATIC_URL = "http://gtfs-static:8080"
GTFS_RT_URL = "http://gtfs-rt:8080"
NEWS_URL = "http://news:8080"
MOTIS_URL = "http://motis:8080"
MOTIS_PLAN_PATH = "/api/v6/plan"

api = Flask(__name__)

scraper = requests.Session()
gtfs_static = requests.Session()
gtfs_rt = requests.Session()
motis = requests.Session()


@api.route('/diversions', methods=['GET'])
def get_diversions():
    resp = gtfs_rt.get(f"{GTFS_RT_URL}/diversions")
    return resp.content, resp.status_code, {"Content-Type": "application/json"}


@api.route('/events', methods=['GET'])
def get_events():
    resp = gtfs_rt.get(f"{GTFS_RT_URL}/events")
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


@api.route("/vehicleInfo", methods=["GET"])
def get_vehicle_info():
    resp = gtfs_rt.get(f"{GTFS_RT_URL}/vehicleInfo", params=request.args)
    return resp.content, resp.status_code, {"Content-Type": "application/json"}


@api.route("/stopDelays", methods=["GET"])
def get_stop_delays():
    resp = gtfs_rt.get(f"{GTFS_RT_URL}/stopDelays", params=request.args)
    return resp.content, resp.status_code, {"Content-Type": "application/json"}


@api.route("/departures", methods=["GET"])
def get_departures():
    resp = gtfs_rt.get(f"{GTFS_RT_URL}/departures", params=request.args)
    return resp.content, resp.status_code, {"Content-Type": "application/json"}


@api.route("/routeDelays", methods=["GET"])
def get_route_delays():
    resp = gtfs_rt.get(f"{GTFS_RT_URL}/routeDelays")
    return resp.content, resp.status_code, {"Content-Type": "application/json"}


# @api.route("/geocode", methods=["GET"])
def geocode():
    text = request.args.get("text")
    if not text:
        return jsonify({"error": "`text` is required"})
    params = {"text": text, "type": "ADDRESS,PLACE"}

    return motis.get(f"{MOTIS_URL}/api/v1/geocode", params=params, timeout=20).json()


@api.route("/findConnections", methods=["GET"])
def find_connections():
    from_stop = request.args.get("fromStop")
    to_stop = request.args.get("toStop")
    if not from_stop or not to_stop:
        return jsonify({"error": "fromStop and toStop are required"})

    params = {"fromPlace": "jmk_"+ from_stop, "toPlace": "jmk_"+ to_stop}
    if time := request.args.get("time"):
        params["time"] = time  # ISO-8601; MOTIS defaults to now if omitted
    if request.args.get("arriveBy") == "true":
        params["arriveBy"] = "true"

    max_itineraries = 10

    num = request.args.get("numItineraries", max_itineraries, type=int)
    num = max(1, min(num, max_itineraries))

    if cursor := request.args.get("pageCursor"):
        params["pageCursor"] = cursor  # from a previous response; other params must stay the same
    else:
        num = (num + 1) // 2  # a fresh search fetches a page on each side of `time`, split between them
    params["numItineraries"] = str(num)

    try:
        result = _fetch_plan(params)
    except requests.RequestException as e:
        return jsonify({"error": f"routing backend unavailable {e}"})

    if "pageCursor" not in params:
        result = _with_other_side(params, result)
    return jsonify(result)


def _fetch_plan(params: dict) -> dict:
    resp = motis.get(f"{MOTIS_URL}{MOTIS_PLAN_PATH}", params=params, timeout=20)
    resp.raise_for_status()
    return translate_plan(resp.json())


def _with_other_side(params: dict, page: dict) -> dict:
    """A fresh search only extends one way from `time` (later for departures, earlier for
    arrivals), so also fetch the adjacent page on the other side to make the list scrollable both ways."""
    arrive_by = "arriveBy" in params
    cursor = page["nextPageCursor"] if arrive_by else page["previousPageCursor"]
    other = {"connections": []}
    if cursor:
        try:
            other = _fetch_plan({**params, "pageCursor": cursor})
        except requests.RequestException:
            pass  # still return the main page, it's just one-directional

    if arrive_by:
        connections = page["connections"] + other["connections"]
        closest = len(page["connections"]) - 1
        prev_cursor = page["previousPageCursor"]
        next_cursor = other["nextPageCursor"]
    else:
        connections = other["connections"] + page["connections"]
        closest = len(other["connections"])
        prev_cursor = other["previousPageCursor"]
        next_cursor = page["nextPageCursor"]

    return {
        "connections": connections,
        "previousPageCursor": prev_cursor,
        "nextPageCursor": next_cursor,
        # index of the connection nearest the requested time, for the initial scroll position
        "closestIndex": max(0, min(closest, len(connections) - 1)),
    }


