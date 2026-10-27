from flask import Flask, jsonify
from flask_caching import Cache
from caching import Caching
from page_parser import PageParser
import json

flask_cache = Cache(config={'CACHE_TYPE': 'SimpleCache'})
scraper = Flask(__name__)
flask_cache.init_app(scraper)
cache = Caching(flask_cache)


@scraper.route('/diversions', methods=['GET'])
def get_diversions():
    result = get_traffic_changes()
    if result.get("diversions") is not None:
        return "{\"diversions\": " + json.dumps(result.get("diversions")) + "}"

    return json.dumps(result)

@scraper.route('/events', methods=['GET'])
def get_events():
    result = get_traffic_changes()
    if result.get("events") is not None:
        return "{\"events\": " + json.dumps(result.get("events")) + "}"

    return json.dumps(result)

def stale_cache(timeout, key):
    def decorator(func):
        def wrapper():
            return cache.cached_stale(timeout, key, func)
        return wrapper
    return decorator

@stale_cache(timeout=60, key="site_fetch")
def get_traffic_changes():
    return PageParser.get_and_parse()
