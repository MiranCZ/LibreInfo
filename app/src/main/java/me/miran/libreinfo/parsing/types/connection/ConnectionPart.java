package me.miran.libreinfo.parsing.types.connection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.miran.libreinfo.parsing.storage.StopStorage;
import me.miran.libreinfo.parsing.types.DateTime;
import me.miran.libreinfo.parsing.types.stop.Stop;
import me.miran.libreinfo.parsing.types.stop.StopId;

public record ConnectionPart(DateTime departure, DateTime arrival, Stop fromStop, Stop toStop, TransportMode transport) {

    public static ConnectionPart parse(JsonObject obj, StopStorage stopStorage) {
        DateTime departure = DateTime.parseISO8601(obj.get("departure").getAsString());
        DateTime arrival = DateTime.parseISO8601(obj.get("arrival").getAsString());

        Stop fromStop = getAndParseStop(stopStorage, obj, "fromId");
        Stop toStop = getAndParseStop(stopStorage, obj, "toId");

        JsonElement tripIdEl = obj.get("tripId");


        TransportMode transport;
        if (tripIdEl == null) {
            var distanceEl = obj.get("distance");

            int distanceMetres;

            if (distanceEl != null) {
                distanceMetres = (int) Double.parseDouble(distanceEl.getAsString());
            } else {
                distanceMetres = -1;
            }
            transport = TransportMode.walk(distanceMetres);
        } else {
            int tripId = Integer.parseInt(tripIdEl.getAsString())-1;
            transport = TransportMode.vehicle(tripId);
        }

        return new ConnectionPart(departure, arrival, fromStop, toStop, transport);
    }

    private static Stop getAndParseStop(StopStorage stopStorage, JsonObject obj, String fieldName) {
        String stopIdStr = obj.get(fieldName).getAsString();
        int id = parseStopId(stopIdStr);

        return stopStorage.getStop(StopId.original(id));
    }


    private static int parseStopId(String stopUID) {
        if (!stopUID.startsWith("U")) {
            throw new IllegalStateException("Invalid stop UID: "+stopUID);
        }

        // format is U{stopId}Z{postId} or U{stopId}N{postId}

        int zInd = stopUID.indexOf("Z");
        int nInd = stopUID.indexOf("N");
        int ind = Math.max(zInd, nInd);

        if (ind == -1) throw new IllegalArgumentException("Invalid stop UID: " + stopUID);

        return Integer.parseInt(stopUID.substring(1, ind));
    }

}
