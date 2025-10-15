package com.example.mhdstuff.parsing.types;

import com.example.mhdstuff.parsing.storage.LineStorage;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public record Diversion(String title, Location location, DateTime from, DateTime to,
                        String publicText, List<LineAlias> lines) {

    public static List<Diversion> parseDiversions(JsonArray array, LineStorage storage) {
        return TypeHelper.parseList(array, (o) -> parse(o, storage));
    }

    public static Diversion parse(JsonObject obj, LineStorage storage) {
        if (obj == null) return null;

        String title = obj.get("title").getAsString();
        Location location = Location.parse(obj);
        DateTime from = DateTime.parse(obj.get("from").getAsString());
        DateTime to = DateTime.parse(obj.get("to").getAsString());

        String publicText = obj.get("content").getAsString();

        List<LineAlias> lines = new ArrayList<>();

        for (JsonElement element : obj.get("lines").getAsJsonArray()) {
            lines.add(LineAlias.parse(element.getAsString(), storage));
        }

        return new Diversion(title, location, from, to, publicText, lines);
    }
}
