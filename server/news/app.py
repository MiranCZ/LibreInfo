import logging

from flask import Flask, jsonify

server = Flask(__name__)

trip_shapes = {}

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s  %(levelname)-8s  %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
log = logging.getLogger(__name__)

@server.route('/news', methods=['GET'])
def get_news():
    return jsonify([])

