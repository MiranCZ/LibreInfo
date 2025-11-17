package me.miran.mhdstuff.raptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.miran.mhdstuff.parsing.storage.CalendarStorage;
import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.parsing.storage.StopStorage;
import me.miran.mhdstuff.parsing.types.RouteStop;
import me.miran.mhdstuff.parsing.types.RouteStopsContainer;
import me.miran.mhdstuff.parsing.types.Time;
import me.miran.mhdstuff.parsing.types.Trip;
import me.miran.mhdstuff.util.OfflineDepartures;
import me.miran.mhdstuff.util.Pair;

public class Raptor {

    public static List<Path> getDepartures(short fromStop, short toStop) {
        System.out.println("Started");
//        Time now = Time.now();

        Time now = new Time(1,20);
        IdStorage storage = IdStorage.getInstance();

        long ms = System.currentTimeMillis();
        try {
            System.out.println("FROM: "+storage.stopStorage().getStop(fromStop));
            System.out.println("TO: "+storage.stopStorage().getStop(toStop));

            return getDepartures(storage, fromStop, toStop, new ExploreHandler(storage.stopStorage()), now);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("TOOK: " + (System.currentTimeMillis()-ms));

        return List.of();
    }

    private static List<Path> getDepartures(IdStorage storage, short fromStop, short toStop, ExploreHandler explore, Time time) {
        List<Connection> result = new ArrayList<>();
        var nowDate = CalendarStorage.Date.now();

        Time maxTime = Time.INF;
        maxTime = updateStops(storage, nowDate, new Node(null,0, -1,null, fromStop, Time.INF, Time.INF, 0), toStop, explore, time, null, result, maxTime);

        int a = 0;
        List<Node> updated;
        while (!(updated = explore.pollUpdated()).isEmpty()) {
            for (Node n : updated) {
                maxTime = updateStops(storage, nowDate, n, toStop, explore, n.leaveStop().arrival(), n.leaveStop().postId(), result, maxTime);
            }
            a++;
            System.out.println("ROUND: "+a);
        }
        System.out.println("OUTPUT "+result.size());

//        for (int i = 0; i < explore.map.length; i++) {
//            var e = explore.map[i];
//            if (e != null) {
//                var s  = storage.stopStorage().getStop(i);
//
//                System.out.println("\t"+s + ": " +e);
//            }
//        }

        return printOutput(storage, explore, result);
    }

    private static List<Path> printOutput(IdStorage storage, ExploreHandler explore, List<Connection> result) {
        Map<Integer, Connection> connections = new HashMap<>();
        for (Connection connection : result) {
            int len = connection.nodes.size();

            if (!connections.containsKey(len)) {
                connections.put(len, connection);
            } else  {
                Connection prev = connections.get(len);
                int comp = connection.arrival().compareTo(prev.arrival());

                if (comp < 0 || (comp == 0 && connection.cost() < prev.cost())) {
                    connections.put(len, connection);
                }
            }
        }


        List<Path> output = new ArrayList<>();
        for (Connection connection : connections.values()) {
            List<Node> nodes = connection.nodes;

            int prevStop = nodes.get(0).enterStop();

            StopStorage ss = storage.stopStorage();
            System.out.println("--"+ explore.getRound() + " -- "+nodes.size());

            List<PathNode> pathNodes = new ArrayList<>();
            for (int i = 1; i < nodes.size(); i++) {
                Node con = nodes.get(i);
                printConnectionInfo(storage, con, con.tripId(), ss, prevStop, con.enterStop());


                Trip trip = storage.tripStorage().getTrips()[con.tripId()];


                pathNodes.add(new PathNode(trip,ss.getStop(prevStop),con.enterTime(),ss.getStop(con.enterStop()), con.leaveTime(), con.transferTime()));
                prevStop = con.enterStop();
            }
            output.add(new Path(pathNodes));
            System.out.println();
        }

        return output;
    }


    private static Time updateStops(IdStorage storage, CalendarStorage.Date nowDate, Node node, short toStop, ExploreHandler explore, Time fromTime, Short postId, List<Connection> output, Time maxTime) {
        for (Result res : getDepartures(storage, nowDate, node.enterStop(), fromTime, maxTime, postId, node.enterStop())) {
            RouteStop dep = res.stop();

            if (dep.arrival().isHigher(maxTime)) continue;

            RouteStop[] stops = storage.tripStorage().getTrips()[dep.tripId()].getRouteStops(storage.routeStopStorage());
            double cost = res.transferTime();

            for (int i = dep.sequence()+1; i < stops.length; i++) {
                RouteStop stop = stops[i];
                if (stop.arrival().isHigher(maxTime)) continue;

                if (stop.stopId() == toStop && explore.isTimeLower(stop, node.cost()+cost)) {
                    List<Node> nodes = new ArrayList<>();
                    Node n = node;
                    while (n != null) {
                        nodes.add(n);
                        n = n.parent();
                    }
                    nodes = nodes.reversed();

                    nodes.addLast(new Node(node, res.transferTime(), stop.tripId(),stop, toStop, stop.arrival(), dep.departure(), node.cost()+cost));
                    output.add(new Connection(nodes));

                    explore.putIfLower(stop, node, dep.departure(),res.transferTime(), cost);
                    maxTime = stop.arrival();
                    break;
                }

                explore.putIfLower(stop, node, dep.departure(), res.transferTime(), cost);
            }
        }

        return maxTime;
    }


    record Connection(List<Node> nodes) {

        public Time arrival() {
            return info().leaveTime();
        }

        public double cost() {
            return info().cost();
        }

        private Node info() {
            return nodes.get(nodes.size()-1);
        }

    }

    private static void printConnectionInfo(IdStorage storage, Node node, int tripId, StopStorage ss, int prevStop, int toStop) {
        Trip trip = storage.tripStorage().getTrips()[tripId];

        System.out.println(trip.lineId() + " " + storage.tripStorage().getTripHeadsign(trip) + " (" + tripId + ")");

        if (node.transferTime() != 0) {
            System.out.println("\t\ttransfer "+node.transferTime() + " min");
        }

        System.out.println("\t" + node.enterTime().format() + "\t" + ss.getStop(prevStop).name);
        System.out.println("\t"+node.leaveTime().format()+"\t"+ss.getStop(toStop).name);
    }


    private static List<Result> getDepartures(IdStorage storage, CalendarStorage.Date nowDate, short stopId, Time from, Time maxTime, Short fromPostId, short fromStopId) {
        CalendarStorage calendarStorage = storage.calendarStorage();
        var stopMap = storage.stopRouteContainerStorage().stopIdToRoute;

        RouteStopsContainer[] stops = stopMap[stopId];

        List<Result> result = new ArrayList<>();


        for (RouteStopsContainer stop : stops) {
            short postId = stop.postId();

            short transferTime = 0;

            if (fromPostId != null) {
                transferTime = storage.transferStorage().getTransferTime(fromStopId, fromPostId, stopId, postId);
            }

            Time time;
            if (transferTime == 0) {
                time = from;
            } else {
                time = from.addMinutes(transferTime);
            }

            if (!calendarStorage.available(nowDate, stop.serviceId())) continue;

            long[] stopsed = stop.stops();

            int usedStop = -1;
            int lowerLimit = time.getMinsDiff(stop.startTime());
            int higherLimit = maxTime.getMinsDiff(stop.startTime());

            for (long l : stopsed) {
                int routeStopId = (int) ((l >> 32) & 0xFFFFFF);
                int minuteOffest = (int) (l & 0xFFFFFF);

//                Time departure = stop.startTime().add(minuteOffest);

                if (higherLimit < minuteOffest) {
                    break;
                }

                if (minuteOffest > lowerLimit) {
                    usedStop = routeStopId;
                    break;
                }
            }

            if (usedStop != -1) {
                RouteStop s = storage.routeStopStorage().getRouteStop(usedStop);
                result.add(new Result(s, transferTime));
            }
        }

//        System.out.println("\t returned " + result.size() + " departures");
        return result;
    }


}
