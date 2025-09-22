package com.example.mhdstuff.parsing.types.departure;

import com.example.mhdstuff.parsing.storage.LineStorage;
import com.example.mhdstuff.util.request.soap.SoapSaneObject;
import com.google.gson.JsonObject;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Departures(String message, List<Departure> departures) {


    public static Departures parse(SoapSaneObject obj, LineStorage storage) {
        if (obj == null) return null;

        String message = "";

        for (Object o : obj.getSoapSaneObject("InfoTextSet")) {
            message += o+"\n\n";
        }
        message = message.trim();

        Map<Integer, List<DepartureEntry>> departureMap = new HashMap<>();

        for (Object departureObj : obj.getSoapSaneObject("DeparturesL")) {
            DepartureEntry entry = DepartureEntry.parse(SoapSaneObject.parse((SoapObject) departureObj), storage);

            departureMap.computeIfAbsent(entry.postID(), k -> new ArrayList<>()).add(entry);
        }

        List<Departure> departures = new ArrayList<>();
        for (Map.Entry<Integer, List<DepartureEntry>> entry : departureMap.entrySet()) {
            departures.add(new Departure(entry.getKey(),entry.getKey()+"", entry.getValue()));
        }
        departures.sort(Comparator.comparingInt(Departure::postID));

        return new Departures(message, departures);
    }

}
