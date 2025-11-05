package me.miran.mhdstuff.parsing.types;

import me.miran.mhdstuff.parsing.storage.IdStorage;

import com.google.gson.JsonObject;

public record MapVehicle(int id, Location location, int bearing, int delay, LineAlias line, Stop prevStop, Stop finalStop) implements VehicleBase{



    public static MapVehicle parse(JsonObject obj, IdStorage storage) {
        JsonObject attrs = obj.getAsJsonObject("attributes");

        int id = attrs.get("id").getAsInt();
        Location location = Location.parse(attrs);

        int bearing = attrs.get("bearing").getAsInt();

        int lineId = attrs.get("lineid").getAsInt();
        String lineName = attrs.get("linename").getAsString();

        LineAlias line = storage.lineStorage().getAlias(lineId);


        Stop prevStop = storage.stopStorage().getStop(attrs.get("laststopid").getAsInt());
        Stop finalStop = storage.stopStorage().getStop(attrs.get("finalstopid").getAsInt());

        int delay = attrs.get("delay").getAsInt();

        return new MapVehicle(id, location, bearing, delay, line,prevStop, finalStop);
    }

}
