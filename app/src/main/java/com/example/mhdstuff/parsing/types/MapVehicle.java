package com.example.mhdstuff.parsing.types;

import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.storage.LineStorage;
import com.google.gson.JsonObject;

public record MapVehicle(int id, Location location, int bearing, LineAlias line, Stop finalStop) implements VehicleBase{



    public static MapVehicle parse(JsonObject obj, IdStorage storage) {
        JsonObject attrs = obj.getAsJsonObject("attributes");

        int id = attrs.get("id").getAsInt();
        Location location = Location.parse(attrs);

        int bearing = attrs.get("bearing").getAsInt();

        int lineId = attrs.get("lineid").getAsInt();
        String lineName = attrs.get("linename").getAsString();

        LineAlias line = storage.lineStorage().getAlias(lineId);

        Stop finalStop = storage.stopStorage().getStop(attrs.get("finalstopid").getAsInt());

        return new MapVehicle(id, location, bearing, line, finalStop);
    }

}
