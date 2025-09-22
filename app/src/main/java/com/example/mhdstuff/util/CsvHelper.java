package com.example.mhdstuff.util;

import java.util.ArrayList;
import java.util.List;

public class CsvHelper {

    public static Csv parseCsvEntries(String csv) {
        List<String> lines = csv.lines().toList();

        List<List<String>> result = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            result.add(parseCsvLine(lines.get(i)));
        }

        return new Csv(parseCsvLine(lines.getFirst()), result);
    }

    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();

        while (!line.isEmpty()) {
            int nextDelim = line.indexOf(",");
            int nextQuote = line.indexOf("\"");
            if (nextQuote < nextDelim && nextQuote != -1) {
                int endQuote = line.indexOf("\"", nextQuote+1);

                result.add(line.substring(0, endQuote));
                line = line.substring(endQuote+1);
                if (line.startsWith(",")) {
                    line = line.substring(1);
                }
                continue;
            }
            if (nextDelim == -1) {
                result.add(line);
                break;
            }

//            System.out.println(line);

            result.add(line.substring(0, nextDelim));
            line = line.substring(nextDelim+1);
        }

        return result;
    }

}
