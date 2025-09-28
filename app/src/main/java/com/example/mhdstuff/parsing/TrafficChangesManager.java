package com.example.mhdstuff.parsing;

import com.example.mhdstuff.parsing.storage.LineStorage;
import com.example.mhdstuff.parsing.types.DateTime;
import com.example.mhdstuff.parsing.types.Diversion;
import com.example.mhdstuff.parsing.types.TransportLine;
import com.example.mhdstuff.util.Pair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrafficChangesManager {
    private static final long MINUTE_IN_MS = 1_000 * 60;
    private static final Map<String, Pair<Long, List<Diversion>>> cache = new HashMap<>();
    private static Document doc;
    private static long lastUpdated = 0;


    public static Elements getList(String selector) {
        if (doc == null || (System.currentTimeMillis() - lastUpdated) > 2 * MINUTE_IN_MS) {
            doc = getDocument();
            lastUpdated = System.currentTimeMillis();
        }

        return doc.select(selector);
    }

    public static List<Diversion> parse(String selector, LineStorage lineStorage) {
        if (cache.containsKey(selector)) {
            var value = cache.get(selector);
            long updated = value.left();
            if ((System.currentTimeMillis() - updated) < MINUTE_IN_MS) {
                return value.right();
            }
        }

        var elements = parseElements(getList(selector), lineStorage);
        cache.put(selector, new Pair<>(System.currentTimeMillis(), elements));

        return elements;
    }
    private static Document getDocument() {
        Document document;
        try {
            document = Jsoup.connect("https://www.dpmb.cz/zmeny-v-doprave").get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return document;
    }

    private static List<Diversion> parseElements(Elements list, LineStorage lineStorage) {
        if (list.isEmpty()) return null;
        if (list.size() > 1) {
            System.out.println("[WARN] Multiple elementes");
        }

        List<Diversion> infos = new ArrayList<>();
        for (Element child : list.get(0).children()) {
            String title = child.select("h3").select("a").select("span").html();
            Element times = child.select("div.platnost").get(0);

            List<String> lines = new ArrayList<>();

            StringBuilder content = new StringBuilder();
            for (Element div : child.select("div")) {
                if(div.className().contains("field--type-text-with-summary")) {
                    for (Element article : div.select("article")) {
                        if(article.className().contains("media--type-ikonka")) {
                            article.remove();
                        }
                    }
                    div.select("img").remove(); // just to be sure
                    for (Element p : div.select("p")) {
                        if (p.text().isBlank()) p.remove();
                    }
                    content.append(div.html());
                }
            }

            for (Element l : child.select("div.field__item")) {
                if (l.children().size() == 1 && l.children().get(0).className().contains("taxonomy-term")) {
                    lines.add(l.select("div:nth-child(1) > span:nth-child(1) > div:nth-child(1)").html());
                }
            }

            Pair<DateTime, DateTime> timeInfo = getTimes(times);

            DateTime from = timeInfo.left();
            DateTime to = timeInfo.right();

            infos.add(new Diversion(title, null, from, to, content.toString(),
                    lines.stream().map(s -> TransportLine.parse(s, lineStorage)).collect(Collectors.toList())
            ));
        }

        return infos;
    }

    private static Pair<DateTime, DateTime> getTimes(Element times) {
        String fromStr = getElementHtml(times, "div:nth-child(2) > time:nth-child(1)");
        String toStr = getElementHtml(times, "div:nth-child(4) > time:nth-child(1)");

        DateTime from = DateTime.parse(fromStr);
        DateTime to = DateTime.parse(toStr);

        return new Pair<>(from, to);
    }

    private static String getElementHtml(Element parent, String selector) {
        Elements result = parent.select(selector);
        if (result.isEmpty()) return null;

        if (result.size() > 1) {
            System.out.println("[WARN] Multiple elements!");
        }

        return result.get(0).html();
    }

}
