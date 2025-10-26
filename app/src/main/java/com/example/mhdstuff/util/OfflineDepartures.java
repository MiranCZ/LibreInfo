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
        return getOffline(storage, stopId, 5, Time.now());
    }

    public static List<Departure> getOffline(IdStorage storage, int stopId, JsonObject delays) {
        return getOffline(storage, stopId, 5, Time.now(), delays);
    }

    public static List<Departure> getOffline(IdStorage storage, int stopId, int maxSize) {
        return getOffline(storage, stopId, maxSize, Time.now());
    }

    public static List<Departure> getOffline(IdStorage storage, int stopId, int maxSize, Time fromTime) {
        return getOffline(storage, stopId, maxSize, fromTime, null);
    }

    public static List<Departure> getOffline(IdStorage storage, int stopId, int maxSize, Time fromTime, JsonObject delays) {
        RouteStop[] stops = storage.routeStopStorage().getRouteStopsParsed(stopId);

        CalendarStorage calendarStorage = storage.calendarStorage();

        record Holder(RouteStop stop, VehicleInfo info) {
        }

        Map<Short, List<Holder>> postToStop = new HashMap<>();

        for (RouteStop stop : stops) {

            VehicleInfo info = new VehicleInfo();
            boolean delaySet = false;
            if (delays != null) {
                Pair<Integer, Integer> lineRoute = storage.apiStorage().getLineIdAndRoute(stop.tripId());

                String lineId = lineRoute.left().toString();
                String routeId = lineRoute.right().toString();

                if (delays.has(lineId)) {
                    JsonObject delaysList = delays.getAsJsonObject(lineId);

                    if (delaysList.has(routeId)) {
                        int delay = delaysList.getAsJsonObject(routeId).get("delay").getAsInt();

                        stop.setDelay(delay);
                        delaySet = true;

                        info = new VehicleInfo(delay, stop.delay());
                    }
                }
            }
            if (!delaySet) {
                stop.setDelay(0);
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
            Set<Trip> includedTrips = new HashSet<>();
            for (Holder holder : entries) {
                RouteStop stop = holder.stop;
                if (found.contains(stop)) continue;
                found.add(stop);

                if (maxSize != -1 && ind > (maxSize - 1)) break;
                if (fromTime.compareTo(stop.departure()) <= 0) {
                    Trip trip = storage.tripStorage().getTrips()[stop.tripId()];

                    if (!calendarStorage.available(nowDate, trip.serviceId())) continue;
                    if (includedTrips.contains(trip)) continue;

                    String heading = storage.tripStorage().getTripHeadsign(trip);

                    if (trip.blockId() != -1) {
                        List<Trip> neighbors = new ArrayList<>(storage.tripStorage().getTripsForBlock(trip.blockId()));

                        neighbors.removeIf(t ->  !calendarStorage.available(nowDate, t.serviceId()));

                        heading = storage.tripStorage().getHeadsignForTripList(neighbors, storage);
                        includedTrips.addAll(neighbors);
                    }

                    // TODO is leaving?
                    TimeMark timeMark = new TimeMark(
                            stop.stopTime(),
                            holder.info.hasBoth(),
                            false
                    );
                    departureEntries.add(new DepartureEntry(
                            storage.lineStorage().getAlias(trip.lineId()),
                            heading,
                            stopId,
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

    public static List<Departure> getOfflineForPost(IdStorage storage, int stopId, int postId, int maxSize, Time fromTime, JsonObject delays) {
        RouteStop[] stops = storage.routeStopStorage().getRouteStopsParsed(stopId);

        CalendarStorage calendarStorage = storage.calendarStorage();

        record Holder(RouteStop stop, VehicleInfo info) {
        }

        List<Holder> entries = new ArrayList<>();

        for (RouteStop stop : stops) {
            if (stop.postId() != postId) continue;

            VehicleInfo info = new VehicleInfo();
            boolean delaySet = false;
            if (delays != null) {
                Pair<Integer, Integer> lineRoute = storage.apiStorage().getLineIdAndRoute(stop.tripId());

                String lineId = lineRoute.left().toString();
                String routeId = lineRoute.right().toString();

                if (delays.has(lineId)) {
                    JsonObject delaysList = delays.getAsJsonObject(lineId);

                    if (delaysList.has(routeId)) {
                        int delay = delaysList.getAsJsonObject(routeId).get("delay").getAsInt();

                        stop.setDelay(delay);
                        delaySet = true;

                        info = new VehicleInfo(delay, stop.delay());
                    }
                }
            }
            if (!delaySet) {
                stop.setDelay(0);
            }

            entries.add(new Holder(stop, info));
        }

        List<Departure> result = new ArrayList<>();

        CalendarStorage.Date nowDate = CalendarStorage.Date.now();


        List<DepartureEntry> departureEntries = new ArrayList<>();


        int ind = 0;

        entries.sort(Comparator.comparing(h -> h.stop.departure()));

        Set<RouteStop> found = new HashSet<>();
        Set<Trip> includedTrips = new HashSet<>();
        for (Holder holder : entries) {
            RouteStop stop = holder.stop;
            if (found.contains(stop)) continue;
            found.add(stop);

            if (maxSize != -1 && ind > (maxSize - 1)) break;
            if (fromTime.compareTo(stop.departure()) <= 0) {
                Trip trip = storage.tripStorage().getTrips()[stop.tripId()];
                if (!calendarStorage.available(nowDate, trip.serviceId())) continue;
                if (includedTrips.contains(trip)) continue;

                String heading = storage.tripStorage().getTripHeadsign(trip);

                if (trip.blockId() != -1) {
                    List<Trip> neighbors = new ArrayList<>(storage.tripStorage().getTripsForBlock(trip.blockId()));

                    neighbors.removeIf(t ->  !calendarStorage.available(nowDate, t.serviceId()));

                    heading = storage.tripStorage().getHeadsignForTripList(neighbors, storage);
                    includedTrips.addAll(neighbors);
                }

                // TODO is leaving?
                TimeMark timeMark = new TimeMark(
                        stop.stopTime(),
                        holder.info.hasBoth(),
                        false
                );
                departureEntries.add(new DepartureEntry(
                        storage.lineStorage().getAlias(trip.lineId()),
                        heading,
                        stopId,
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

        result.sort(Comparator.comparingInt(Departure::postID));

        return result;
    }


}
