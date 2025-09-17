package com.example.mhdstuff.parsing.types;

import com.example.mhdstuff.parsing.storage.LineStorage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public record Event(long number, Time validFrom, Time validTo, Time publicFrom, Time publicTo,
                    String title, String place, String cause, MinuteRange delay, String text,
                    List<TransportLine> affectedLines, Location location) {

    public static List<Event> parseEvents(JsonArray array, LineStorage storage) {
        return TypeHelper.parseList(array, (o) -> parse(o, storage));
    }

    public static Event parse(JsonObject obj, LineStorage storage) {
        if (obj == null) return null;
        long number = obj.get("Number").getAsLong();

        Time validFrom = Time.parse(obj.get("ValidFrom").getAsString());
        Time validTo = Time.parse(obj.get("ValidTo").getAsString());
        Time publicFrom = Time.parse(obj.get("PublicFrom").getAsString());
        Time publicTo = Time.parse(obj.get("PublicTo").getAsString());

        String title = obj.get("Title").getAsString();
        String place = obj.get("Place").getAsString();
        String cause = obj.get("Cause").getAsString();

        MinuteRange delay = MinuteRange.parse(obj.get("Delay").getAsString());

        String text = obj.get("Text").getAsString();

        List<TransportLine> affectedLines = TransportLine.parseTransportLines(
                obj.get("AffectedLines").getAsString(), storage
        );

        Location location = Location.parse(obj);

        return new Event(number, validFrom, validTo, publicFrom, publicTo, title, place, cause, delay, text, affectedLines, location);
    }

}
