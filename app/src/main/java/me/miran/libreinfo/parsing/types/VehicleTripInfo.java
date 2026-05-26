package me.miran.libreinfo.parsing.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import me.miran.libreinfo.parsing.storage.StopMapper;

public record VehicleTripInfo(long lastUpdate, int lastStopId, int delay, Map<Short, Integer> previousStopDelays) {


    public static VehicleTripInfo NONE = new VehicleTripInfo(-1, -1, -1, Map.of());

    public static VehicleTripInfo parse(StopMapper mapper, JsonObject obj) {
        if (obj == null) return null;

        if (obj.has("error")) return NONE;

        long lastUpdate = obj.get("changed_at").getAsLong();
        int lastStopId = obj.get("last_stop").getAsInt();
        int vehDelay = obj.get("delay").getAsInt();

        lastStopId = mapper.getMapped(lastStopId);

        Map<Short, Integer> prevStops = new HashMap<>();

        for (JsonElement prevStopEl : obj.getAsJsonArray("previous_stops")) {
            JsonObject prevStop = prevStopEl.getAsJsonObject();

            int id = prevStop.get("id").getAsInt();
            id = mapper.getMapped(id);

            int delay = prevStop.get("delay").getAsInt();

            prevStops.put((short) id, delay);
        }

        return new VehicleTripInfo(lastUpdate, lastStopId, vehDelay, prevStops);
    }

}
