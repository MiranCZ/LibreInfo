package com.example.mhdstuff.parsing.types.departure;

import com.example.mhdstuff.parsing.storage.LineStorage;
import com.example.mhdstuff.parsing.types.TransportLine;
import com.example.mhdstuff.parsing.types.TypeHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


import java.util.List;

public record DepartureEntry(TransportLine line, String finalStop, boolean lowFloor, boolean hasAC, String timeMark, int vehicleId) {

    public static List<DepartureEntry> parseEntriesList(JsonArray array, LineStorage lineStorage) {
        return TypeHelper.parseList(array, (o) -> parse(o, lineStorage));
    }

    public static DepartureEntry parse(JsonObject obj, LineStorage lineStorage) {
        if (obj == null) return null;

        TransportLine line = TransportLine.parse(obj.get("Line").getAsString(), lineStorage);
        String finalStop = obj.get("FinalStop").getAsString();
        boolean lowFloor = obj.get("IsLowFloor").getAsBoolean();
        boolean hasAC = obj.get("HasAC").getAsBoolean();
        String timeMark = obj.get("TimeMark").getAsString();
        int vehicleID = obj.get("VehicleID").getAsInt();

        return new DepartureEntry(line, finalStop, lowFloor, hasAC, timeMark, vehicleID);
    }
}
