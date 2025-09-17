package com.example.mhdstuff.parsing.types;

import com.example.mhdstuff.parsing.storage.LineStorage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 *
 * @param id Global unique id
 * @param stopID ID of the stop this post corresponds to
 * @param postID ID unique to the stop
 */
public record Post(int id, int stopID, int postID, String name, Location location, boolean isPublic,
                   List<TransportLine> lines, StopMode stopMode
) {


    public static List<Post> parsePosts(JsonArray array, LineStorage lineStorage) {
        return TypeHelper.parseList(array, (o) -> parse(o, lineStorage));
    }

    public static Post parse(JsonObject obj, LineStorage lineStorage) {
        if (obj == null) return null;

        int id = obj.get("ID").getAsInt();
        int stopID = obj.get("StopID") .getAsInt();
        int postID = obj.get("PostID").getAsInt();

        String name = obj.get("Name").getAsString();
        Location location = Location.parse(obj);
        boolean isPublic = obj.get("IsPublic").getAsBoolean();
        List<TransportLine> lines = TransportLine.parseTransportLines(obj.get("LineList").getAsString(), lineStorage);
        StopMode stopMode = StopMode.parse(obj.get("StopMode").getAsString());

        return new Post(id, stopID, postID, name, location, isPublic, lines, stopMode);
    }
}
