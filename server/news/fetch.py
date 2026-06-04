import logging
import os

from db import save_emails
from gmail import build_service, fetch_unread, mark_read
from parse import parse_newsletter

log = logging.getLogger(__name__)

SENDER = os.environ.get("NEWSLETTER_SENDER", "")
SUBJECT = os.environ.get("NEWSLETTER_SUBJECT", "")
DEBUG = os.environ.get("DEBUG", "false").lower() == "true"


def run_fetch():
    if not SENDER:
        log.warning("NEWSLETTER_SENDER not set — skipping fetch")
        return
    try:
        service = build_service()
        emails = fetch_unread(service, SENDER, subject=SUBJECT, debug=DEBUG)
        if not emails:
            log.info("No new emails")
            return
        for e in emails:
            parsed = parse_newsletter(e.pop("body_html", ""))
            e.update(parsed)
        save_emails(emails)
        if not DEBUG:
            mark_read(service, [e["id"] for e in emails])
        log.info("Fetch complete, stored %d email(s)", len(emails))
    except Exception as e:
        log.exception("Fetch failed", e)
