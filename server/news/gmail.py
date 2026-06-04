import base64
import email.utils
import os
import quopri

from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from googleapiclient.discovery import build

SCOPES = ["https://www.googleapis.com/auth/gmail.modify"]
TOKEN_PATH = os.environ.get("TOKEN_PATH", "/secrets/token.json")


def build_service():
    creds = Credentials.from_authorized_user_file(TOKEN_PATH, SCOPES)
    if creds.expired and creds.refresh_token:
        creds.refresh(Request())
        with open(TOKEN_PATH, "w") as f:
            f.write(creds.to_json())
    return build("gmail", "v1", credentials=creds)


def _decode_part(part) -> str:
    data = part.get("body", {}).get("data", "")
    if not data:
        return ""
    raw = base64.urlsafe_b64decode(data + "==")
    headers = {h["name"].lower(): h["value"] for h in part.get("headers", [])}
    if headers.get("content-transfer-encoding", "").lower() == "quoted-printable":
        raw = quopri.decodestring(raw)
    return raw.decode("utf-8", errors="replace")


def _extract_html(payload: dict) -> str:
    mime_type = payload.get("mimeType", "")
    parts = payload.get("parts", [])

    if mime_type == "text/html":
        return _decode_part(payload)
    for part in parts:
        html = _extract_html(part)
        if html:
            return html
    return ""


def _parse_message(msg: dict) -> dict:
    payload = msg["payload"]
    headers = {h["name"].lower(): h["value"] for h in payload.get("headers", [])}

    raw_date = headers.get("date", "")
    try:
        timestamp = int(email.utils.parsedate_to_datetime(raw_date).timestamp())
    except Exception:
        timestamp = 0

    return {
        "id": msg["id"],
        "subject": headers.get("subject", ""),
        "sender": headers.get("from", ""),
        "date": timestamp,
        "body_html": _extract_html(payload),
    }


def fetch_unread(service, sender: str, subject: str = "", debug: bool = False) -> list:
    query = f"from:{sender}" if debug else f"from:{sender} is:unread"
    if subject:
        query += f" subject:{subject}"
    kwargs = {"userId": "me", "q": query}
    if debug:
        kwargs["maxResults"] = 10
    result = service.users().messages().list(**kwargs).execute()
    stubs = result.get("messages", [])
    if not stubs:
        return []

    return [
        _parse_message(
            service.users().messages().get(userId="me", id=s["id"], format="full").execute()
        )
        for s in stubs
    ]


def mark_read(service, ids: list):
    if not ids:
        return
    service.users().messages().batchModify(
        userId="me",
        body={"ids": ids, "removeLabelIds": ["UNREAD"]},
    ).execute()
