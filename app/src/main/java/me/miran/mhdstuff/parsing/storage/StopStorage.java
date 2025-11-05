package me.miran.mhdstuff.parsing.storage;

import me.miran.mhdstuff.parsing.types.Stop;
import me.miran.mhdstuff.util.FuzzySearch;
import me.miran.mhdstuff.util.PreferencesHolder;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StopStorage {

    public static StopStorage parse(DataInputStream is, PreferencesHolder favStops) {
        List<Stop> stops;
        try(is) {
            stops = Stop.parseStops(is, favStops);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return new StopStorage(stops);
    }


    private final List<Stop> stops;
    private final Map<Integer, Stop> idToStop = new HashMap<>();
    private final FuzzySearch<Stop> searcher;


    public StopStorage(List<Stop> stops) {
        this.stops = stops;
        for (Stop stop : stops) {
            idToStop.put(stop.id, stop);
        }

        this.searcher = new FuzzySearch<>(stops, stop -> stop.name);
    }

    public Stop getStop(int id) {
        if (!idToStop.containsKey(id)) {
//            System.out.println("[WARN] Tried to get non-existent stop with id " + id);
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
