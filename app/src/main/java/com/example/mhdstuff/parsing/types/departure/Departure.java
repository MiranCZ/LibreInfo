package com.example.mhdstuff.parsing.types.departure;

import com.example.mhdstuff.parsing.storage.LineStorage;
import com.example.mhdstuff.parsing.types.TypeHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.List;

public record Departure(int stopID, int postID, String name, List<DepartureEntry> entries) {

    public static List<Departure> parseDepartures(JsonArray array, LineStorage lineStorage) {
        return TypeHelper.parseList(array, (o) -> parse(o, lineStorage));
    }

    public static Departure parse(JsonObject obj, LineStorage lineStorage) {
        if (obj == null) return null;

        int stopID = obj.get("StopID").getAsInt();
        int postID = obj.get("PostID").getAsInt();
        String name = obj.get("Name").getAsString();

        List<DepartureEntry> entries = DepartureEntry.parseEntriesList(
                obj.get("Departures").getAsJsonArray(), lineStorage
        );

        return new Departure(stopID, postID, name, entries);
    }

}
