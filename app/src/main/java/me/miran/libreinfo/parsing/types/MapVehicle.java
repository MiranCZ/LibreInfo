package me.miran.libreinfo.parsing.types;

import me.miran.libreinfo.parsing.storage.manager.IdStorage;
import me.miran.libreinfo.parsing.types.stop.Stop;
import me.miran.libreinfo.parsing.types.stop.StopId;

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

        Stop prevStop = storage.stopStorage().getStop(StopId.original(prevStopId));
        Stop finalStop = storage.stopStorage().getStop(StopId.original(finalStopId));

        int delay = obj.get("Delay").getAsInt();

        return new MapVehicle(id, location, bearing, delay, line,prevStop, finalStop);
    }

}
