package com.example.mhdstuff.util;

import com.example.mhdstuff.parsing.storage.CalendarStorage;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.RouteStop;
import com.example.mhdstuff.parsing.types.Time;
import com.example.mhdstuff.parsing.types.TimeMark;
import com.example.mhdstuff.parsing.types.Trip;
import com.example.mhdstuff.parsing.types.departure.Departure;
import com.example.mhdstuff.parsing.types.departure.DepartureEntry;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class OfflineDepartures {

    public static List<Departure> getOffline(IdStorage storage, int stopId) {
        return getOffline(storage, stopId, 5);
    }
    public static List<Departure> getOffline(IdStorage storage, int stopId, int maxSize) {
        RouteStop[] stops = storage.routeStopStorage().getRouteStopsParsed(stopId);

        CalendarStorage calendarStorage = storage.calendarStorage();

        Map<Short, List<RouteStop>> postToStop = new HashMap<>();

        for (RouteStop stop : stops) {
            postToStop.computeIfAbsent(stop.postId(), k -> new ArrayList<>()).add(stop);
        }

        List<Departure> result = new ArrayList<>();

        CalendarStorage.Date nowDate = CalendarStorage.Date.now();

        for (Map.Entry<Short, List<RouteStop>> entry : postToStop.entrySet()) {
            int postId = entry.getKey();

            List<DepartureEntry> departureEntries = new ArrayList<>();

            List<RouteStop> entries = entry.getValue();

            Time now = Time.now();

            int ind = 0;

            entries.sort(Comparator.comparing(RouteStop::departure));

            Set<RouteStop> found = new HashSet<>();
            for (RouteStop stop : entries) {
                if (found.contains(stop)) continue;
                found.add(stop);

                if (ind > (maxSize-1)) break;
                if (now.compareTo(stop.departure()) <= 0) {
                    Trip trip = storage.tripStorage().getTrips()[stop.tripId()];;
                    String heading = storage.tripStorage().getTripHeadsign(trip);

                    if (!calendarStorage.available(nowDate, trip.serviceId())) continue;

                    TimeMark timeMark = new TimeMark(
                            LocalTime.now().plusMinutes(stop.departure().getMinsDiff(Time.now())),
                            false
                    );
                    departureEntries.add(new DepartureEntry(
                            storage.lineStorage().getAlias(trip.lineId()),
                            heading,
                            postId,
                            false, // FIXME the trips might have that info actually
                            timeMark,
                            Optional.empty()
                    ));
                    ind++;
                }
            }

            if (!departureEntries.isEmpty()) {
                Departure departure = new Departure(
                        postId,
                        storage.postStorage().getPost(stopId, postId).name(),
                        departureEntries
                );
                result.add(departure);
            }
        }
        result.sort(Comparator.comparingInt(Departure::postID));

        return result;
    }


}
