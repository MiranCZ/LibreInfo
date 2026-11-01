package io.github.mirancz.gtfs.export.writer;

import io.github.mirancz.gtfs.ParsedGtfs;
import io.github.mirancz.gtfs.export.Writer;
import io.github.mirancz.gtfs.ids.StopTimeEntry;
import io.github.mirancz.gtfs.ids.TripEntry;
import io.github.mirancz.util.CheckedOutputStream;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.github.mirancz.gtfs.export.ParseUtil.parseRoute;
import static io.github.mirancz.gtfs.export.ParseUtil.parseStop;

public class TripWriter implements Writer {

    @Override
    public void write(ParsedGtfs gtfs, Function<String, CheckedOutputStream> outputProvider) {
        List<Route> routes;
        try (var os = outputProvider.apply("route_stops")) {
            routes = parseAndRoutes(gtfs, os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (var os = outputProvider.apply("trips")) {
            writeTrips(gtfs, routes, os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    protected void writeTrips(ParsedGtfs gtfs, List<Route> routes, CheckedOutputStream os) throws IOException {
        Map<Integer, Route> tripIdToRoute = new HashMap<>();
        for (Route route : routes) {
            tripIdToRoute.put(route.tripId(), route);
        }

        Map<String, Integer> headsignPool = new HashMap<>();


        var trips = gtfs.trips();

        os.writeInt(trips.size());


        for (TripEntry e : trips) {
            int routeId = parseRoute(e.route_id());

            os.writeShort(routeId);
        }


        for (TripEntry e : trips) {
            os.writeShort(e.service_id());
        }

        for (TripEntry e : trips) {
            String headSign = e.trip_headsign();

            int headsignId;
            if (headsignPool.containsKey(headSign)) {
                headsignId = headsignPool.get(headSign);
            } else {
                headsignPool.put(headSign, (headsignId = headsignPool.size()));
            }

            os.writeInt(headsignId);
        }

        for (TripEntry e : trips) {
            int blockId = -1;
            if (e.block_id() != null) {
                blockId = e.block_id();
            }

            os.writeShort(blockId);
        }

        for (TripEntry e : trips) {
            os.writeByte(e.bikes_allowed());
        }

        // just running-sum of lengths
//        for (TripEntry e : trips) {
//            Route route = tripIdToRoute.get(e.trip_id());
//
//            os.writeInt(route.startPos());
//        }
        for (TripEntry e : trips) {
            Route route = tripIdToRoute.get(e.trip_id());

            int length = route.length;

            os.writeByte(length);
        }


        String[] headsigns = new String[headsignPool.size()];

        for (Map.Entry<String, Integer> entry : headsignPool.entrySet()) {
            headsigns[entry.getValue()] = entry.getKey();
        }

        os.writeInt(headsigns.length);

        for (String headsign : headsigns) {
            os.writeString(headsign);
        }

    }

    private List<Route> parseAndRoutes(ParsedGtfs gtfs, CheckedOutputStream routeStopsOs) throws IOException {
        Route currentRoute = null;
        int prevSequence = -1;

        Map<Short, List<Integer>> stopIdToRoute = new HashMap<>();
        List<RouteStop> routeStops = new ArrayList<>();

        int routeStopIndex = 0;
        int lineNumber = 0;
        int index = 0;

        List<Route> result = new ArrayList<>();

        var stopTimes = gtfs.stopTimes();

        for (StopTimeEntry e : stopTimes) {
            int tripId = e.trip_id();

            var stopInfo = parseStop(e.stop_id());

            int sequence = e.stop_sequence();


            if (currentRoute == null) {
                currentRoute = new Route(tripId, lineNumber);
                prevSequence = sequence - 1;
            }


            // according to docs sequence must increase but can do so by arbitrary amount
            if (sequence > prevSequence) {
                if (currentRoute.tripId != tripId) {
                    throw new IllegalStateException(currentRoute + " ; " + tripId + " ; " + sequence);
                }

                RouteStop routeStop = new RouteStop(routeStops.size(), currentRoute.tripId, stopInfo.stopId(), stopInfo.postId(), index++,
                        Time.parse(e.arrival_time()), Time.parse(e.departure_time())
                );

                stopIdToRoute.computeIfAbsent(routeStop.stopId(), k -> new ArrayList<>()).add(routeStopIndex++);
                routeStops.add(routeStop);

                currentRoute.stops.add(routeStop);
            } else {
                currentRoute.length = lineNumber-currentRoute.startPos();
                result.add(currentRoute);
                currentRoute = new Route(tripId, lineNumber);
                index = 0;

                RouteStop routeStop = new RouteStop(routeStops.size(), currentRoute.tripId(), stopInfo.stopId(), stopInfo.postId(), index++,
                        Time.parse(e.arrival_time()), Time.parse(e.departure_time())
                );
                stopIdToRoute.computeIfAbsent(routeStop.stopId(), k -> new ArrayList<>()).add(routeStopIndex++);
                routeStops.add(routeStop);

                currentRoute.stops.add(routeStop);
            }

            prevSequence = sequence;
            lineNumber++;

        }
        result.add(currentRoute);


        List<RouteStop> res = result.stream().map((r) -> r.stops).flatMap(new Function<List<RouteStop>, Stream<RouteStop>>() {
            @Override
            public Stream<RouteStop> apply(List<RouteStop> routeStops) {
                return routeStops.stream();
            }
        }).toList();

        if (res.size() != routeStops.size()) {
            throw new IllegalStateException();
        }


        writeRouteStops(routeStopsOs, result);

        return result;
    }

    private void writeRouteStops(CheckedOutputStream os, List<Route> routes) throws IOException {
        // FIXME kinda shit naming; but "stop" in this sense is a specific post/platform on a stop
        record Stop(short stopId, short postId){
        }

        record StopSequence(List<Stop> stops){
        }


        HashMap<StopSequence, Integer> sequencePool = new HashMap<>();

        List<RouteStop> routeStops = new ArrayList<>();

        os.writeInt(routes.size());
        for (Route route : routes) {
            StopSequence sequence = new StopSequence(route.stops.stream().map((s) -> new Stop(s.stopId, s.postId)).toList());

            int id = sequencePool.computeIfAbsent(sequence, (k) -> sequencePool.size());

            os.writeInt(id);

            routeStops.addAll(route.stops);
        }

        HashMap<Stop, Integer> stopPool = new HashMap<>();

        StopSequence[] sequences = new StopSequence[sequencePool.size()];
        for (Map.Entry<StopSequence, Integer> e : sequencePool.entrySet()) {
            sequences[e.getValue()] = e.getKey();
        }

        for (StopSequence sequence : sequences) {
            os.write(sequence.stops.size());
            for (Stop stop : sequence.stops) {
                int id = stopPool.computeIfAbsent(stop, (k) -> stopPool.size());

                os.writeShort(id);
            }
        }

        Stop[] stops = new Stop[stopPool.size()];
        for (Map.Entry<Stop, Integer> e : stopPool.entrySet()) {
            stops[e.getValue()] = e.getKey();
        }

        for (Stop stop : stops) {
            os.writeShort(stop.stopId);
            os.writeShort(stop.postId);
        }

//        for (RouteStop s : routeStops) os.writeInt(s.tripId);

        for (Route route : routes) os.writeShort(toMinutes(route.stops.get(0).arrival));

        for (Route route : routes) {
            for (int i = 1; i < route.stops.size(); i++) {
                os.write(toMinutes(route.stops.get(i).arrival) - toMinutes(route.stops.get(i - 1).departure));
            }
        }

        for (RouteStop s : routeStops) os.writeVarInt(toMinutes(s.departure) - toMinutes(s.arrival));
    }

    private static int toMinutes(Time time) {
        return time.hours * 60 + time.minutes;
    }

    public static final class Route {
        private final int tripId;
        private final int startPos;
        private int length = -1;

        private final List<RouteStop> stops;

        public Route(int tripId, int startPos) {
            this.tripId = tripId;
            this.startPos = startPos;
            this.stops = new ArrayList<>();
        }

        public int tripId() {
            return tripId;
        }

        public int startPos() {
            return startPos;
        }

        public int length() {
            return length;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Route) obj;
            return this.tripId == that.tripId && this.startPos == that.startPos && this.length == that.length;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tripId, startPos, length);
        }

        @Override
        public String toString() {
            return "Route[" + "tripId=" + tripId + ", " + "startPos=" + startPos + ", " + "length=" + length + ']';
        }

    }


    private record RouteStop(int id, int tripId, short stopId, short postId, int sequence, Time arrival,
                             Time departure) {
    }

    public record Time(int hours, int minutes, int second) implements Comparable<Time> {

        public static Time parse(String time) {
            String[] parts = time.split(":");

            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int second = Integer.parseInt(parts[2]);

            return new Time(hours, minutes, second);
        }


        @Override
        public int compareTo(Time o) {
            if (hours == o.hours) {
                return minutes - o.minutes;
            }
            return hours - o.hours;
        }
    }

    private static void writeTime(CheckedOutputStream os, Time time) throws IOException {
        os.writeByte(time.hours);
        os.writeByte(time.minutes);

        if (time.second != 0) {
            throw new IllegalStateException("Wrongly assumed seconds would be zero!");
        }
    }



}
