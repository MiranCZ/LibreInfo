package com.example.mhdstuff.parsing.types;

import com.example.mhdstuff.parsing.storage.IdStorage;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Vehicle(int id, int idB, int idC, int vType, int lType, Location location, int bearing,
                      TransportLine line, int routeId, String course, boolean lowFloor, int delay,
                      Stop lastStop, Stop finalStop, Optional<String> finalStopName, boolean inactive) {

    public static List<Vehicle> parseVehicles(JsonArray array, IdStorage storage) {
        List<Vehicle> result = new ArrayList<>();

        for (JsonElement element : array) {
            result.add(parse(element.getAsJsonObject(), storage));
        }

        return result;
    }

    public static Vehicle parse(JsonObject obj, IdStorage storage) {
        if (obj == null) return null;

        // numbers of vehicles (idk how they handle it if there is more than three lol)
        int id = obj.get("ID").getAsInt();
        int idB = obj.get("IDB").getAsInt();
        int idC = obj.get("IDC").getAsInt();

        // idfk what this is
        int vType = obj.get("VType").getAsInt();
        int lType = obj.get("LType").getAsInt();

        Location location = Location.parse(obj);

        // *probably* how many people are currently in it? not sure
        int bearing = obj.get("Bearing").getAsInt();

        TransportLine line = TransportLine.parseFromIdAndName(storage.lineStorage(), obj.get("LineID").getAsInt(), obj.get("LineName").getAsString());

        //TODO figure out what this means
        int routeId = obj.get("RouteID").getAsInt();

        // this seems to be a number, but is presented as string in the response
        // TODO figure out if it can be safely assumed to be an int
        String course = obj.get("Course").getAsString();

        // low floor is an educated guess from LF here
        boolean lowFloor = obj.get("LF").getAsBoolean();

        int delay = obj.get("Delay").getAsInt();
        Stop lastStop = storage.stopStorage().getStop(obj.get("LastStopID").getAsInt());
        Stop finalStop = storage.stopStorage().getStop(obj.get("FinalStopID").getAsInt());

        Optional<String> finalStopName;

        if (obj.has("FinalStopName")) {
            finalStopName = Optional.of(obj.get("FinalStopName").getAsString());
        } else {
            finalStopName = Optional.empty();
        }

        // not sure what inactive signals
        boolean inactive = obj.get("IsInactive").getAsBoolean();

        return new Vehicle(
                id, idB, idC, vType, lType, location, bearing, line, routeId, course,
                lowFloor, delay, lastStop, finalStop, finalStopName, inactive
        );
    }

}
