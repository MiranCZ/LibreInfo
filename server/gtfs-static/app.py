from flask import Flask, jsonify
import json

server = Flask(__name__)

@server.route('/shapes', methods=['GET'])
def get_events():
    # TODO implement
    return jsonify({"result":"ok"})
