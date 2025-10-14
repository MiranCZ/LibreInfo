package com.example.mhdstuff.util;

import com.example.mhdstuff.parsing.storage.CalendarStorage;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.RouteStop;
import com.example.mhdstuff.parsing.types.Time;
import com.example.mhdstuff.parsing.types.TimeMark;
import com.example.mhdstuff.parsing.types.Trip;
import com.example.mhdstuff.parsing.types.Vehicle;
import com.example.mhdstuff.parsing.types.departure.Departure;
import com.example.mhdstuff.parsing.types.departure.DepartureEntry;
import com.example.mhdstuff.parsing.types.departure.VehicleInfo;
import com.google.gson.JsonObject;

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
        return getOffline(storage, stopId, 5, null);
    }

    public static List<Departure> getOffline(IdStorage storage, int stopId, JsonObject delays) {
        return getOffline(storage, stopId, 5, Time.now(), delays);
    }

    public static List<Departure> getOffline(IdStorage storage, int stopId, int maxSize) {
        return getOffline(storage, stopId, maxSize, Time.now());
    }
    public static List<Departure> getOffline(IdStorage storage, int stopId, int maxSize, Time fromTime) {
        return getOffline(storage, stopId, maxSize,fromTime, null);
    }

    public static List<Departure> getOffline(IdStorage storage, int stopId, int maxSize, Time fromTime, JsonObject delays) {
        RouteStop[] stops = storage.routeStopStorage().getRouteStopsParsed(stopId);

        CalendarStorage calendarStorage = storage.calendarStorage();

        record Holder(RouteStop stop, Optional<VehicleInfo> info) {
        }

        Map<Short, List<Holder>> postToStop = new HashMap<>();

        for (RouteStop stop : stops) {

            Optional<VehicleInfo> info = Optional.empty();
            if (delays != null) {
                Pair<Integer, Integer> lineRoute = storage.apiStorage().getLineIdAndRoute(stop.tripId()+1);
                String key = lineRoute.left()+"/"+ lineRoute.right();
                if (delays.has(key)) {
                    System.out.println(key);

                    stop = stop.withDelay(delays.get(key).getAsJsonObject().get("delay").getAsInt());

                    info = Optional.of(new VehicleInfo(delays.get(key).getAsJsonObject().get("id").getAsInt(), stop.delay()));
                }
            }

            postToStop.computeIfAbsent(stop.postId(), k -> new ArrayList<>()).add(new Holder(stop, info));
        }

        List<Departure> result = new ArrayList<>();

        CalendarStorage.Date nowDate = CalendarStorage.Date.now();

        for (Map.Entry<Short, List<Holder>> entry : postToStop.entrySet()) {
            int postId = entry.getKey();

            List<DepartureEntry> departureEntries = new ArrayList<>();

            List<Holder> entries = entry.getValue();

            int ind = 0;

            entries.sort(Comparator.comparing(h -> h.stop.departure()));

            Set<RouteStop> found = new HashSet<>();
            for (Holder holder : entries) {
                RouteStop stop = holder.stop;
                if (found.contains(stop)) continue;
                found.add(stop);

                if (maxSize != -1 && ind > (maxSize-1)) break;
                if (fromTime.compareTo(stop.departure()) <= 0) {
                    Trip trip = storage.tripStorage().getTrips()[stop.tripId()];
                    String heading = storage.tripStorage().getTripHeadsign(trip);

                    if (!calendarStorage.available(nowDate, trip.serviceId())) continue;

                    TimeMark timeMark = new TimeMark(
                            stop.departure(),
                            holder.info.isPresent()
                    );
                    departureEntries.add(new DepartureEntry(
                            storage.lineStorage().getAlias(trip.lineId()),
                            heading,
                            postId,
                            trip.lowFloor(),
                            timeMark,
                            stop.tripId(),
                            holder.info
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
