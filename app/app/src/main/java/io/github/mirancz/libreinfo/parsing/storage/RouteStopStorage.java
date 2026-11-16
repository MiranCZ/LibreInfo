package io.github.mirancz.libreinfo.parsing.storage;

import io.github.mirancz.libreinfo.codec.RoutesCodec;
import io.github.mirancz.libreinfo.codec.util.AppInputStream;
import io.github.mirancz.libreinfo.exception.AppException;
import io.github.mirancz.libreinfo.model.types.StopPattern;
import io.github.mirancz.libreinfo.model.types.StopPost;
import io.github.mirancz.libreinfo.parsing.types.RouteStop;
import io.github.mirancz.libreinfo.parsing.types.Time;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RouteStopStorage implements AppStorage {


    record StopVisit(StopPattern pattern, int position){
    }

    public static RouteStopStorage parse(AppInputStream is, int stopsLength) throws AppException {
        try {
            var result = RoutesCodec.read(is);

            @SuppressWarnings("unchecked")
            List<StopVisit>[] stopIdToPattern = new ArrayList[stopsLength];

            for (int i = 0; i < stopIdToPattern.length; i++) {
                stopIdToPattern[i] = new ArrayList<>();
            }

            for (StopPattern pattern : result.getPatternPool()) {
                var stops = pattern.getStops();
                for (int i = 0; i < stops.size(); i++) {
                    StopPost stop = stops.get(i);
                    stopIdToPattern[stop.getStopId()].add(new StopVisit(pattern, i));
                }
            }

            var patterns = result.getPatterns();
            for (int i = 0; i < patterns.length; i++) {
                StopPattern pattern = patterns[i];

                pattern.getTrips().add(i);
            }

            return new RouteStopStorage(stopIdToPattern, result);
        } catch (IOException e) {
            throw AppException.dataLoad(e);
        }
    }


    private final List<StopVisit>[] stopIdToPattern;
    private final RoutesCodec.ParsedRoutes parsedRoutes;

    private RouteStopStorage(List<StopVisit>[] stopIdToPattern, RoutesCodec.ParsedRoutes parsedRoutes) {
        this.stopIdToPattern = stopIdToPattern;
        this.parsedRoutes = parsedRoutes;
    }

    public RouteStop[] getRouteStopsParsed(int stopId) {
        var coll = stopIdToPattern[stopId];

        List<RouteStop> result = new ArrayList<>();

        for (int i = 0; i < coll.size(); i++) {
            StopVisit visit = coll.get(i);

            for (int trip : visit.pattern.getTrips()) {
                result.add(getRouteStop(trip, visit.position));
            }
        }

        return result.toArray(new RouteStop[0]);
    }

    public RouteStop[] getTripsRouteStops(int tripId) {
        var stops = parsedRoutes.getPatterns()[tripId].getStops();
        RouteStop[] result = new RouteStop[stops.size()];

        for (int i = 0; i < result.length; i++) {
            result[i] = getRouteStop(tripId, i);
        }

        return result;
    }

    public RouteStop getRouteStop(int tripId, int offset) {
        StopPost stop = parsedRoutes.getPatterns()[tripId].getStops().get(offset);

        int routeId = parsedRoutes.getPatterns().length * offset + tripId;

        short arrival = parsedRoutes.getArrivals()[tripId][offset];

        int minutes = arrival % 60;
        int hours = arrival / 60;

        Time arrivalTime = new Time(hours, minutes);

        int dwell = parsedRoutes.getDwells()[tripId][offset];

        return new RouteStop(routeId, stop.getStopId(), tripId, stop.getPostId(), (short) offset, arrivalTime, arrivalTime.addMinutes(dwell));
    }

}
