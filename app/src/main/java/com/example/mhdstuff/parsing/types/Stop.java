package com.example.mhdstuff.parsing.types;

import com.example.mhdstuff.parsing.storage.LineStorage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public record Stop(int id, int zone, String name, Location location, boolean isPublic,
                   List<TransportLine> lines,
                   StopMode stopMode
) {


    public static Stop NONE = new Stop(-1, -1, "", Location.NONE, false, List.of(), StopMode.MIXED);

    public static List<Stop> parseStops(JsonArray array, LineStorage storage) {
        return TypeHelper.parseList(array, (o) -> parse(o, storage));
    }

    public static Stop parse(JsonObject object, LineStorage storage) {
        if (object == null) return null;

        int id = object.get("StopID").getAsInt();
        int zone = object.get("Zone").getAsInt();
        String name = object.get("Name").getAsString();
        Location location = Location.parse(object);
        boolean isPublic = object.get("IsPublic").getAsBoolean();
        List<TransportLine> lines = TransportLine.parseTransportLines(object.get("LineList").getAsString(), storage);
        StopMode stopMode = StopMode.parse(object.get("DefaultStopMode").getAsString());


        return new Stop(id, zone, name, location, isPublic, lines, stopMode);
    }

}
