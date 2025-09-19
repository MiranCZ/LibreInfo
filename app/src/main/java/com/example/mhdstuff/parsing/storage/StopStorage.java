package com.example.mhdstuff.parsing.storage;

import com.example.mhdstuff.parsing.types.Stop;
import com.example.mhdstuff.util.FuzzySearch;
import com.google.gson.JsonArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StopStorage {

    public static StopStorage parse(JsonArray array, LineStorage lineStorage) {
        List<Stop> stops = Stop.parseStops(array, lineStorage);

        return new StopStorage(stops);
    }


    private final List<Stop> stops;
    private final Map<Integer, Stop> idToStop = new HashMap<>();

    private final FuzzySearch<Stop> searcher;


    public StopStorage(List<Stop> stops) {
        this.stops = stops;
        for (Stop stop : stops) {
            idToStop.put(stop.id(), stop);
        }

        this.searcher = new FuzzySearch<>(stops, Stop::name);
    }

    public Stop getStop(int id) {
        if (!idToStop.containsKey(id)) {
            System.out.println("[WARN] Tried to get non-existent stop with id " + id);
            return Stop.NONE;
        }

        return idToStop.get(id);
    }

    public List<Stop> getAllStops() {
        return stops;
    }

    public FuzzySearch<Stop> getSearcher() {
        return searcher;
    }
}
