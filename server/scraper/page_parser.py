import requests
import datetime
from zoneinfo import ZoneInfo
from selectolax.parser import HTMLParser


class PageParser:

    @staticmethod
    def __parse_list(el_list):
        infos = []
        if el_list is None:
            return []

        for child in el_list.iter():
            title = child.css_first("h3 a span").text()

            times_node = child.css_first("div.platnost")
            if not times_node:
                continue

            content_parts = []
            for div in child.css("div"):
                if "field--type-text-with-summary" in div.attributes.get("class"):
                    for article in div.css("article.media--type-ikonka"):
                        article.decompose()
                    for img in div.css("img"):
                        img.decompose()
                    ps = list(div.css("p"))
                    for p in ps:
                        if not p.text(strip=True):
                            p.decompose()
                    content_parts.append(div.html)

            content_str = "".join(content_parts)

            # Extract lines
            lines = []
            delay = None

            for l in child.css("div.field__item"):
                children = list(l.iter())

                if len(children) == 1 and children[0].attributes.get("class") is not None and "taxonomy-term" in \
                        children[0].attributes.get("class"):
                    line_node = children[0].css_first("div:nth-child(1) > span:nth-child(1) > div:nth-child(1)")
                    if line_node:
                        lines.append(line_node.text())

            for l in child.css("div"):
                children = list(l.iter())

                for ch in children:
                    if ch.attributes.get("class") is not None and "field--name-field-zdrzeni" in ch.attributes.get("class"):
                        s = list(ch.iter())
                        if len(s) > 0:
                            delay = s[1].text()

            time_info = PageParser.get_times(times_node)
            from_dt, to_dt = time_info

            info = {"title": title, "from": from_dt, "to": to_dt, "content": content_str, "lines": lines}

            if delay is not None:
                info["delay"] = delay
            infos.append(info)

        return infos

    @staticmethod
    def get_times(times_node):
        from_str = times_node.css_first("div:nth-child(2) > time:nth-child(1)")
        to_str = times_node.css_first("div:nth-child(4) > time:nth-child(1)")

        if from_str is None:
            from_str = "?"
        else:
            from_str = from_str.text()

        if to_str is None:
            to_str = "?"
        else:
            to_str = to_str.text()

        return to_iso8601(from_str), to_iso8601(to_str)

    @staticmethod
    def get_and_parse():
        response = requests.get("https://www.dpmb.cz/zmeny-v-doprave")
        if response.status_code != 200:
            return {"error": response.status_code}

        dom = HTMLParser(response.text)
        return {
            "diversions": PageParser.__parse_list(dom.css_first(".view-display-id-attachment_3 > div:nth-child(2)")),
            "events": PageParser.__parse_list(dom.css_first(".view-display-id-attachment_1 > div:nth-child(2)"))
        }


def to_iso8601(s: str):
    if s == "?":
        return None

    parts = s.split(" ")
    day = int(parts[0].removesuffix("."))
    month = int(parts[1].removesuffix("."))
    year = int(parts[2])

    time_parts = parts[3].split(":")
    hour = int(time_parts[0])
    minute = int(time_parts[1])

    dt = datetime.datetime(year, month, day, hour, minute, tzinfo=ZoneInfo("Europe/Prague"))
    return dt.astimezone(datetime.timezone.utc).isoformat(timespec="seconds")

if __name__ == '__main__':
    PageParser.get_and_parse()
