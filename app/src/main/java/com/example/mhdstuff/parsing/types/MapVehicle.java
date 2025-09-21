package com.example.mhdstuff.parsing.types;

import com.google.gson.JsonObject;

public record MapVehicle(int id, Location location, int bearing, TransportLine line) {



    public static MapVehicle parse(JsonObject obj) {
        JsonObject attrs = obj.getAsJsonObject("attributes");

        int id = attrs.get("id").getAsInt();
        Location location = Location.parse(attrs);

        int bearing = attrs.get("bearing").getAsInt();

        int lineId = attrs.get("lineid").getAsInt();
        String lineName = attrs.get("linename").getAsString();

        return new MapVehicle(id, location, bearing, new TransportLine(lineId, lineName));
    }

}
