package com.example.mhdstuff.parsing.types;

import com.example.mhdstuff.parsing.storage.LineStorage;

import java.util.ArrayList;
import java.util.List;

public record TransportLine(int id, String displayName) {


    public static List<TransportLine> parseTransportLines(String str, LineStorage storage) {
        if (str == null) return null;
        str = str.strip();

        List<TransportLine> result = new ArrayList<>();

        String[] parts;
        if (str.contains(",")) {
            parts = str.split(",");
        } else {
            parts = str.split(" ");
        }

        for (String part : parts) {
            if (part.isBlank()) continue;

            result.add(parse(part, storage));
        }

        return result;
    }

    public static TransportLine parseFromIdAndName(LineStorage storage, int id, String lineName) {
        if (storage.getAlias(id) != storage.getAlias(lineName)) {
            // wouldn't even be surprised if this happened
            System.out.println("[WARN] Id does not correspond to the same id as name! ("+id + ", " + lineName + ")!\n" +
                    storage.getAlias(id) + " vs " + storage.getAlias(lineName));

            return storage.getLine(id);
        }

        return storage.getLine(id);
    }

    public static TransportLine parse(String str, LineStorage storage) {
        if (str == null) return null;
        str = str.strip();

        if (TypeHelper.isInteger(str)) {
            int id = Integer.parseInt(str);

            return storage.getLine(id);
        }

        return storage.getLine(str);
    }

}
