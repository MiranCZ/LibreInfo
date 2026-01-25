package me.miran.mhdstuff.parsing.types;

import me.miran.mhdstuff.parsing.storage.IdStorage;

import com.google.gson.JsonObject;

public record MapVehicle(int id, Location location, int bearing, int delay, LineAlias line, Stop prevStop, Stop finalStop) {



    public static MapVehicle parse(JsonObject obj, IdStorage storage) {
        if (obj == null) return null;

        int id = obj.get("ID").getAsInt();
        Location location = Location.parse(obj);

        int bearing = obj.get("Bearing").getAsInt();

        int lineId = obj.get("LineID").getAsInt();

        LineAlias line = storage.lineStorage().getAlias(lineId);

        Stop prevStop = storage.stopStorage().getStop(obj.get("LastStopID").getAsInt());
        Stop finalStop = storage.stopStorage().getStop(obj.get("FinalStopID").getAsInt());

        int delay = obj.get("Delay").getAsInt();

        return new MapVehicle(id, location, bearing, delay, line,prevStop, finalStop);
    }

}
