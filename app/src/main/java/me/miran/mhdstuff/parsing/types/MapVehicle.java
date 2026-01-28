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

        int prevStopId = obj.get("LastStopID").getAsInt();
        int finalStopId = obj.get("FinalStopID").getAsInt();

        Stop prevStop = storage.stopStorage().getStop(storage.stopMapper().getMapped(prevStopId));
        Stop finalStop = storage.stopStorage().getStop(storage.stopMapper().getMapped(finalStopId));

        int delay = obj.get("Delay").getAsInt();

        return new MapVehicle(id, location, bearing, delay, line,prevStop, finalStop);
    }

}
