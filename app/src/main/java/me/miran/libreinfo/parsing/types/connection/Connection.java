package me.miran.libreinfo.parsing.types.connection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import me.miran.libreinfo.parsing.storage.StopStorage;
import me.miran.libreinfo.parsing.types.DateTime;

public record Connection(DateTime departure, DateTime arrival, List<ConnectionPart> parts) {

    public static Connection parse(JsonObject obj, StopStorage stopStorage) {
        DateTime departure = DateTime.parseISO8601(obj.get("departure").getAsString());
        DateTime arrival = DateTime.parseISO8601(obj.get("arrival").getAsString());

        List<ConnectionPart> parts = new ArrayList<>();

        for (JsonElement leg : obj.getAsJsonArray("legs")) {
            parts.add(ConnectionPart.parse(leg.getAsJsonObject(), stopStorage));
        }

        return new Connection(departure, arrival, parts);
    }

}
