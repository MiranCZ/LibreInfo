package com.example.mhdstuff.parsing.types.departure;

import com.example.mhdstuff.parsing.storage.LineStorage;
import com.google.gson.JsonObject;

import java.util.List;

public record Departures(int stopId, int postId, String message, List<Departure> departures) {

    public static Departures parse(JsonObject obj, LineStorage storage) {
        if (obj == null) return null;

        int stopID = obj.get("StopID").getAsInt();
        int postID = obj.get("PostID").getAsInt();

        String message = obj.get("Message").getAsString();
        List<Departure> departures = Departure.parseDepartures(obj.get("PostList").getAsJsonArray(), storage);

        return new Departures(stopID, postID, message, departures);
    }

}
