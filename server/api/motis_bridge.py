from datetime import datetime


def _delay_seconds(actual: str | None, scheduled: str | None) -> int:
    """Seconds the actual time runs behind the scheduled time (0 if unknown)."""
    if not actual or not scheduled:
        return 0
    try:
        return int((datetime.fromisoformat(actual) - datetime.fromisoformat(scheduled)).total_seconds())
    except ValueError:
        return 0

def _trim_prefix(item: str | None) -> str | None:
    if item is None:
        return None

    return item[item.index("jmk_") + 4::]

def _translate_stop(stop_id: str | None) -> int | None:
    stop_id = _trim_prefix(stop_id)

    if stop_id is None:
        return None

    z_ind = stop_id.find("Z")
    n_ind = stop_id.find("N")

    ind = max(z_ind, n_ind)
    if ind == -1:
        return None

    return int(stop_id[1:ind])



def _trim_leg(leg: dict) -> dict:
    out = {
        "mode": leg.get("mode"),
        "fromId": _translate_stop((leg.get("from") or {}).get("stopId")),
        "toId": _translate_stop((leg.get("to") or {}).get("stopId")),
        "departure": leg.get("startTime"),
        "arrival": leg.get("endTime"),
        "duration": leg.get("duration"),
        "realtime": leg.get("realTime", False),
        "delay": _delay_seconds(leg.get("startTime"), leg.get("scheduledStartTime"))
        if leg.get("realTime")
        else 0,
        "geometry": (leg.get("legGeometry") or {}).get("points"),
    }
    if leg.get("tripId"):
        out["tripId"] = _trim_prefix(leg["tripId"])
    if leg.get("distance"):
        out["distance"] = leg["distance"]
    return out


def translate_plan(plan: dict) -> dict:
    """Translate a MOTIS /plan response into the compact shape the app consumes."""
    return {
        "connections": [
            {
                "departure": it.get("startTime"),
                "arrival": it.get("endTime"),
                "duration": it.get("duration"),
                "transfers": it.get("transfers"),
                "legs": [_trim_leg(leg) for leg in it.get("legs", [])],
            }
            for it in plan.get("itineraries", [])
        ],
        "previousPageCursor": plan.get("previousPageCursor"),
        "nextPageCursor": plan.get("nextPageCursor"),
    }
