import logging

from apscheduler.schedulers.background import BackgroundScheduler
from flask import Flask, jsonify, request

from db import get_emails, get_total, init_db
from fetch import run_fetch

server = Flask(__name__)

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s  %(levelname)-8s  %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
log = logging.getLogger(__name__)

init_db()

scheduler = BackgroundScheduler()
scheduler.add_job(run_fetch, "interval", minutes=15, id="gmail_fetch")
scheduler.start()
run_fetch()


@server.route('/news', methods=['GET'])
def get_news():
    limit = request.args.get("limit")

    if limit is None:
        limit = 10
    else:
        try:
            limit = int(limit)
        except ValueError:
            return jsonify({"error": f"Invalid limit '{limit}'"})

    limit = max(1, min(10, limit))

    offset = request.args.get("offset")
    if offset is None:
        offset = 0
    else:
        try:
            offset = int(offset)
        except ValueError:
            return jsonify({"error": f"Invalid offset '{offset}'"})

    emails = get_emails(limit, offset)
    total = get_total()
    return jsonify({"emails": emails, "total": total, "limit": limit, "offset": offset})

