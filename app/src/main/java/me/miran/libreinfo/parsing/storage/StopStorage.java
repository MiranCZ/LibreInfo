package me.miran.libreinfo.parsing.storage;

import me.miran.libreinfo.parsing.types.stop.Stop;
import me.miran.libreinfo.parsing.types.stop.StopId;
import me.miran.libreinfo.util.AppInputStream;
import me.miran.libreinfo.util.FuzzySearch;
import me.miran.libreinfo.util.PreferencesHolder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class StopStorage {

    public static StopStorage parse(AppInputStream is, PreferencesHolder favStops, StopMapper mapper) {
        List<Stop> stops;
        try(is) {
            stops = Stop.parseStops(is, favStops, mapper);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return new StopStorage(stops, mapper);
    }


    private final List<Stop> stops;
    private final Stop[] idToStop;
    private final FuzzySearch<Stop> searcher;
    public final StopMapper mapper;


    public StopStorage(List<Stop> stops, StopMapper mapper) {
        this.stops = stops;
        this.idToStop = new Stop[mapper.internalStopsLength()];
        this.mapper = mapper;

        Arrays.fill(idToStop, Stop.NONE);

        for (Stop stop : stops) {
            idToStop[stop.id.internal()] = stop;
        }

        this.searcher = new FuzzySearch<>(stops, stop -> stop.name);
    }

    public Stop getStop(StopId.StopIdHolder holder) {
        if (holder.type == StopId.StopIdType.INTERNAL) {
            return getInternalStop(holder.id);
        }

        if (holder.type == StopId.StopIdType.ORIGINAL) {
            return getOriginalStop(holder.id);
        }

        throw new IllegalStateException();
    }

    private Stop getInternalStop(int id) {
        if (id < 0 || id >= idToStop.length) {
            return Stop.NONE;
        }

        return idToStop[id];
    }

    private Stop getOriginalStop(int id) {
        return getInternalStop(mapper.getMapped(id));
    }

    public List<Stop> getAllStops() {
        return stops;
    }

    public FuzzySearch<Stop> getSearcher() {
        return searcher;
    }
}
