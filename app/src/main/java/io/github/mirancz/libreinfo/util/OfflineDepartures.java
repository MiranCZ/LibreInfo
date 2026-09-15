package io.github.mirancz.libreinfo.util;

import io.github.mirancz.libreinfo.parsing.storage.CalendarStorage;
import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage;
import io.github.mirancz.libreinfo.parsing.types.RouteDelayEntry;
import io.github.mirancz.libreinfo.parsing.types.RouteStop;
import io.github.mirancz.libreinfo.parsing.types.Time;
import io.github.mirancz.libreinfo.parsing.types.TimeMark;
import io.github.mirancz.libreinfo.parsing.types.Trip;
import io.github.mirancz.libreinfo.parsing.types.departure.Departure;
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureEntry;
import io.github.mirancz.libreinfo.parsing.types.departure.VehicleInfo;
import io.github.mirancz.libreinfo.parsing.types.response.RouteDelaysResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class OfflineDepartures {

    public static List<Departure> getOffline(IdStorage storage, int stopId) {
        return getOffline(storage, stopId, 5, Time.now());
    }

    public static List<Departure> getOffline(IdStorage storage, int stopId, int maxSize, RouteDelaysResponse delays) {
        return getOffline(storage, stopId, maxSize, Time.now(), delays);
    }

    public static List<Departure> getOffline(IdStorage storage, int stopId, int maxSize) {
        return getOffline(storage, stopId, maxSize, Time.now());
    }

    public static List<Departure> getOffline(IdStorage storage, int stopId, int maxSize, Time fromTime) {
        return getOffline(storage, stopId, maxSize, fromTime, null);
    }

    public static List<Departure> getOffline(IdStorage storage, int stopId, int maxSize, Time fromTime, RouteDelaysResponse delaysResponse) {
        return collectDepartures(storage, stopId, stop -> true, maxSize, fromTime, delaysResponse);
    }

    public static List<Departure> getOfflineForPost(IdStorage storage, int stopId, int postId, int maxSize, Time fromTime, RouteDelaysResponse delaysResponse) {
        return collectDepartures(storage, stopId, stop -> stop.postId() == postId, maxSize, fromTime, delaysResponse);
    }

    private static List<Departure> collectDepartures(IdStorage storage, int stopId, Predicate<RouteStop> filter, int maxSize, Time fromTime, RouteDelaysResponse delaysResponse) {
        RouteStop[] stops = storage.routeStopStorage().getRouteStopsParsed(stopId);
        Map<Integer, Map<Integer, RouteDelayEntry>> delays = null;

        if (delaysResponse != null) {
            delays = delaysResponse.getRouteDelays();
        }

        CalendarStorage calendarStorage = storage.calendarStorage();
        CalendarStorage.Date nowDate = CalendarStorage.Date.now();

        record Holder(RouteStop stop, VehicleInfo info, TimeMark time) {
        }

        Map<Short, List<Holder>> postToStop = new HashMap<>();

        for (RouteStop stop : stops) {
            if (!filter.test(stop)) continue;

            VehicleInfo info = new VehicleInfo();

            Integer delay = null;
            if (delays != null) {
                Pair<Integer, Integer> lineRoute = storage.apiStorage().getLineIdAndRoute(stop.tripId());

                int lineId = lineRoute.left();
                int routeId = lineRoute.right();
                var delaysList = delays.get(lineId);
                if (delaysList != null) {
                    var entry = delaysList.get(routeId);
                    if (entry != null) {
                        delay = entry.getDelay();

                        info = new VehicleInfo(entry.getVehicleId(), delay);
                    }
                }
            }

            TimeMark.TimeMode timeMode = TimeMark.TimeMode.NORMAL;

            // for terminal stops compare using arrival time instead
            // (since departure time is the vehicle already leaving for its next route)
            var trip = storage.tripStorage().getTrips()[stop.tripId()];
            var lastStopId = trip.startPos() + trip.length() - 1;
            if (lastStopId == stop.routeId()) {
                timeMode = TimeMark.TimeMode.TERMINUS;

                // ...unless the vehicle continues from this post as the next trip of its block, then the departure is real
                // (with arrival == departure both modes give the same times, so only check the block when it can matter)
                // this doesn't tank performance that much since not many cases exist... still not ideal tho
                if (trip.blockId() != -1 && !stop.stopTime().immediateDeparture()) {
                    for (Trip t : storage.tripStorage().getTripsForBlock(trip.blockId())) {
                        if (t.id() == trip.id() || !calendarStorage.available(nowDate, t.serviceId())) continue;

                        // time check needed due to vehicle round-trips
                        RouteStop first = storage.routeStopStorage().getRouteStop(t.startPos());
                        if (first.stopId() == stop.stopId() && first.postId() == stop.postId()
                                && first.departure().equals(stop.departure())) {
                            timeMode = TimeMark.TimeMode.NORMAL;
                            break;
                        }
                    }
                }
            }

            // TODO is leaving?
            postToStop.computeIfAbsent(
                    stop.postId(),
                    k -> new ArrayList<>()
            ).add(
                    new Holder(stop, info, new TimeMark(stop.stopTime(), timeMode, delay, false))
            );
        }

        List<Departure> result = new ArrayList<>();

        for (Map.Entry<Short, List<Holder>> entry : postToStop.entrySet()) {
            int postId = entry.getKey();

            List<DepartureEntry> departureEntries = new ArrayList<>();

            List<Holder> entries = entry.getValue();

            int ind = 0;

            entries.sort(Comparator.comparing(h -> h.time.getDelayedDeparture()));

            // FIXME don't think this is even needed..?
//            Set<Integer> found = new HashSet<>();

            Set<Trip> includedTrips = new HashSet<>();
            for (Holder holder : entries) {
                RouteStop stop = holder.stop;
//                if (found.contains(stop.routeId())) continue;
//                found.add(stop.routeId());

                if (maxSize != -1 && ind > (maxSize - 1)) break;

                TimeMark mark = holder.time;

                if (fromTime.compareTo(mark.getDelayedDeparture()) <= 0) {
                    Trip trip = storage.tripStorage().getTrips()[stop.tripId()];

                    if (!calendarStorage.available(nowDate, trip.serviceId())) continue;
                    if (includedTrips.contains(trip)) continue;

                    String heading = storage.tripStorage().getTripHeadsign(trip);

                    if (trip.blockId() != -1) {
                        List<Trip> neighbors = new ArrayList<>(storage.tripStorage().getTripsForBlock(trip.blockId()));

                        neighbors.removeIf(t -> !calendarStorage.available(nowDate, t.serviceId()));

                        heading = storage.tripStorage().getHeadsignForTripList(neighbors, storage);
                        includedTrips.addAll(neighbors);
                    }

                    departureEntries.add(new DepartureEntry(
                            storage.lineStorage().getAlias(trip.lineId()),
                            heading,
                            stopId,
                            postId,
                            trip.lowFloor(),
                            mark,
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
