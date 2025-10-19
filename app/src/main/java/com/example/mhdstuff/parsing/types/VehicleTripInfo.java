package com.example.mhdstuff.parsing.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public record VehicleTripInfo(long lastUpdate, int lastStopId, Map<Integer, Integer> previousStopDelays) {


    public static VehicleTripInfo NONE = new VehicleTripInfo(-1, -1, Map.of());

    public static VehicleTripInfo parse(JsonObject obj) {
        if (obj == null) return null;

        if (obj.has("error")) return NONE;

        long lastUpdate = obj.get("changed_at").getAsLong();
        int lastStopId = obj.get("last_stop").getAsInt();

        Map<Integer, Integer> prevStops = new HashMap<>();

        for (JsonElement prevStopEl : obj.getAsJsonArray("previous_stops")) {
            JsonObject prevStop = prevStopEl.getAsJsonObject();

            int id = prevStop.get("id").getAsInt();
            int delay = prevStop.get("delay").getAsInt();

            prevStops.put(id, delay);
        }

        return new VehicleTripInfo(lastUpdate, lastStopId, prevStops);
    }

}
