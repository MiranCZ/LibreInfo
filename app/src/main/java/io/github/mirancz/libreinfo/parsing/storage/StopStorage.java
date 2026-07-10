package io.github.mirancz.libreinfo.parsing.storage;

import io.github.mirancz.libreinfo.R;
import io.github.mirancz.libreinfo.exception.AppException;
import io.github.mirancz.libreinfo.exception.ErrorType;
import io.github.mirancz.libreinfo.parsing.types.stop.Stop;
import io.github.mirancz.libreinfo.parsing.types.stop.StopId;
import io.github.mirancz.libreinfo.util.AppInputStream;
import io.github.mirancz.libreinfo.util.FuzzyStopSearch;
import io.github.mirancz.libreinfo.util.PreferencesHolder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class StopStorage implements AppStorage {

    public static StopStorage parse(AppInputStream is, PreferencesHolder favStops, StopMapper mapper) throws AppException {
        List<Stop> stops;
        try {
            stops = Stop.parseStops(is, favStops, mapper);
        } catch (IOException e) {
            throw new AppException(R.string.data_load_error, e).withType(ErrorType.DATA);
        }

        return new StopStorage(stops, mapper);
    }


    private final List<Stop> stops;
    private final Stop[] idToStop;
    private final FuzzyStopSearch searcher;
    public final StopMapper mapper;


    public StopStorage(List<Stop> stops, StopMapper mapper) {
        this.stops = stops;
        this.idToStop = new Stop[mapper.internalStopsLength()];
        this.mapper = mapper;

        Arrays.fill(idToStop, Stop.NONE);

        for (Stop stop : stops) {
            idToStop[stop.id.internal()] = stop;
        }

        this.searcher = new FuzzyStopSearch(stops);
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

    public FuzzyStopSearch getSearcher() {
        return searcher;
    }
}
