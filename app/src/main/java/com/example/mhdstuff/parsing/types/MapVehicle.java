package com.example.mhdstuff.parsing.types;

import com.example.mhdstuff.parsing.storage.LineStorage;
import com.google.gson.JsonObject;

public record MapVehicle(int id, Location location, int bearing, LineAlias line) implements VehicleBase{



    public static MapVehicle parse(JsonObject obj, LineStorage storage) {
        JsonObject attrs = obj.getAsJsonObject("attributes");

        int id = attrs.get("id").getAsInt();
        Location location = Location.parse(attrs);

        int bearing = attrs.get("bearing").getAsInt();

        int lineId = attrs.get("lineid").getAsInt();
        String lineName = attrs.get("linename").getAsString();

        LineAlias line = storage.getAlias(lineId);

        return new MapVehicle(id, location, bearing, line);
    }

}
