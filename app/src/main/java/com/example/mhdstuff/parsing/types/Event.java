package com.example.mhdstuff.parsing.types;

import com.example.mhdstuff.parsing.storage.LineStorage;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public record Event(DateTime from, DateTime to,
                    String title, String delay, String text,
                    List<LineAlias> lines) {

    public static List<Event> parseEvents(JsonArray array, LineStorage storage) {
        return TypeHelper.parseList(array, (o) -> parse(o, storage));
    }

    public static Event parse(JsonObject obj, LineStorage storage) {
        if (obj == null) return null;

        String title = obj.get("title").getAsString();

        DateTime from = DateTime.parse(obj.get("from").getAsString());
        DateTime to = DateTime.parse(obj.get("to").getAsString());


//        String place = obj.get("Place").getAsString();
//        String cause = obj.get("Cause").getAsString();

        String delay = obj.get("delay").getAsString();


        String text = obj.get("content").getAsString();

        List<LineAlias> lines = new ArrayList<>();

        for (JsonElement element : obj.get("lines").getAsJsonArray()) {
            lines.add(LineAlias.parse(element.getAsString(), storage));
        }


        return new Event(from, to, title, delay, text, lines);
    }

}
