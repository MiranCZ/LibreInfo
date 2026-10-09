package io.github.mirancz.libreinfo.parsing.storage;

import io.github.mirancz.libreinfo.exception.AppException;
import io.github.mirancz.libreinfo.parsing.types.stop.Stop;
import io.github.mirancz.libreinfo.parsing.types.stop.StopExtKt;
import io.github.mirancz.libreinfo.parsing.types.stop.StopId;
import io.github.mirancz.libreinfo.util.AppInputStream;
import io.github.mirancz.libreinfo.util.search.FuzzyStopSearch;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.ToIntFunction;

public class StopStorage implements AppStorage {

    public static StopStorage parse(AppInputStream is) throws AppException {
        List<Stop> stops;
        try {
            stops = StopExtKt.parseStops(is);
        } catch (IOException e) {
            throw AppException.dataLoad(e);
        }

        return new StopStorage(stops);
    }


    private final Stop[] stops;
    private final FuzzyStopSearch searcher;


    public StopStorage(List<Stop> stops) {
        stops.sort(Comparator.comparingInt(value -> value.getId().getId()));

        this.stops = new Stop[stops.get(stops.size()-1).getId().getId()+1];
        Arrays.fill(this.stops, Stop.Companion.getNONE());

        for (Stop stop : stops) {
            this.stops[stop.getId().getId()] = stop;
        }

        this.searcher = new FuzzyStopSearch(stops);
    }


    public Stop getStop(StopId id) {
        return getStop(id.getId());
    }

    public Stop getStop(int id) {
        if (id < 0 || id >= stops.length) {
            return Stop.Companion.getNONE();
        }

        return stops[id];
    }

    public Stop[] getAllStops() {
        return stops;
    }

    public int getStopsLengths() {
        return stops.length;
    }

    public FuzzyStopSearch getSearcher() {
        return searcher;
    }
}
