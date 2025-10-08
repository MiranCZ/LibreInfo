package com.example.mhdstuff.parsing.types;

import com.example.mhdstuff.parsing.storage.LineStorage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @param stopID ID of the stop this post corresponds to
 * @param postID ID unique to the stop
 */
public record Post(int stopID, int postID, String name, Location location, boolean isPublic, List<TransportLine> lines) {


    public static List<Post> parsePosts(DataInputStream array, LineStorage lineStorage) throws IOException {
        List<Post> result = new ArrayList<>();
        int size = array.readInt();

        for (int i = 0; i < size; i++) {
            result.add(parse(array, lineStorage));
        }

        return result;
    }

    public static Post parse(DataInputStream is, LineStorage lineStorage) throws IOException {
        int stopId = is.readInt();
        int postId = is.readShort();

        int nameLen = is.readInt();
        byte[] bytes = new byte[nameLen];
        int l = is.read(bytes);
        if (l != nameLen) {
            throw new IllegalStateException();
        }

        String name = new String(bytes, StandardCharsets.UTF_8);

        double lat = is.readDouble();
        double lng = is.readDouble();

        boolean isPublic = is.readBoolean();
        List<TransportLine> lines = new ArrayList<>();

        int lineSize = is.readInt();

        for (int j = 0; j < lineSize; j++) {
            int id = is.readInt();
            lines.add(lineStorage.getLine(id));
        }


        return new Post(stopId, postId, name, new Location(lat, lng), isPublic, lines);
    }
}
