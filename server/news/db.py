import os
import sqlite3
import time

DB_PATH = os.environ.get("DB_PATH", "/data/news.db")


def _connect():
    return sqlite3.connect(DB_PATH)


def init_db():
    with _connect() as conn:
        conn.execute("""
                     CREATE TABLE IF NOT EXISTS emails
                     (
                         id
                         TEXT
                         PRIMARY
                         KEY,
                         subject
                         TEXT,
                         sender
                         TEXT,
                         date
                         INTEGER,
                         title
                         TEXT,
                         url
                         TEXT,
                         content_html
                         TEXT,
                         fetched_at
                         INTEGER
                     )
                     """)


def save_emails(emails: list):
    now = int(time.time())
    with _connect() as conn:
        conn.executemany(
            """
            INSERT OR REPLACE INTO emails
                (id, subject, sender, date, title, url, content_html, fetched_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """,
            [
                (
                    e["id"],
                    e.get("subject"),
                    e.get("sender"),
                    e.get("date"),
                    e.get("title"),
                    e.get("url"),
                    e.get("content_html"),
                    now,
                )
                for e in emails
            ],
        )


def get_emails(limit: int, offset: int) -> list:
    with _connect() as conn:
        conn.row_factory = sqlite3.Row
        rows = conn.execute(
            "SELECT date, title, url, content_html FROM emails ORDER BY date DESC LIMIT ? OFFSET ?",
            (limit, offset),
        ).fetchall()
    return [dict(r) for r in rows]


def get_total() -> int:
    with _connect() as conn:
        return conn.execute("SELECT COUNT(*) FROM emails").fetchone()[0]
