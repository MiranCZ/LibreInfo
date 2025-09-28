package com.example.mhdstuff.parsing.types;

import com.example.mhdstuff.parsing.storage.LineStorage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public record Stop(int id, String name, Location location) {


    public static Stop NONE = new Stop(-1, "UNKNOWN", Location.NONE);

    public static List<Stop> parseStops(DataInputStream is) throws IOException {
        List<Stop> result = new ArrayList<>();

        while (is.readBoolean()) {
            result.add(parse(is));
        }

        return result;
    }

    public static Stop parse(DataInputStream is) throws IOException {
        int stopId = is.readInt();

        int nameLen = is.readInt();
        byte[] result = new byte[nameLen];
        int read = is.read(result);
        if (read != result.length) {
            throw new IOException("Failed to read stop name");
        }

        String name = new String(result, StandardCharsets.UTF_8);

        double lat = is.readDouble();
        double lon = is.readDouble();

        return new Stop(stopId, name, new Location(lat, lon));
    }

}
