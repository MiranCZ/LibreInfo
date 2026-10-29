from selectolax.parser import HTMLParser


def parse_newsletter(html: str) -> dict:
    tree = HTMLParser(html)

    link = tree.css_first("h2 a")
    title = link.text(strip=True) if link else ""
    url = link.attrs.get("href", "") if link else ""

    body = tree.css_first(".field--name-body")
    if body:
        outer = body.html
        content_html = outer[outer.index(">") + 1 : outer.rindex("<")].strip()
    else:
        content_html = ""

    return {"title": title, "url": url, "content_html": content_html}
