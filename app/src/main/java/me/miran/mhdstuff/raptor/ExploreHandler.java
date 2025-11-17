package me.miran.mhdstuff.raptor;

import me.miran.mhdstuff.parsing.storage.StopStorage;
import me.miran.mhdstuff.parsing.types.RouteStop;
import me.miran.mhdstuff.parsing.types.Time;

import java.util.*;

public class ExploreHandler {

    public final Entry[] map;
    private final Node[] updatesStops;
    private final int[] updatedIndices;
    private int index = 0;
    private int round = 0;

    public ExploreHandler(StopStorage stopStorage) {
        int size = stopStorage.getAllStops().size()+10;

        map = new Entry[size];
        updatesStops = new Node[size];
        updatedIndices = new int[size];
    }

    public void putIfLower(RouteStop stop, Node parent, Time fromTime, int transferTime, double cost) {
        short stopId = (short) stop.stopId();
        Time time = stop.arrival();

        Entry prev = get(stopId);
        cost += parent.cost();

        if (prev.time.compareTo(time) > 0 || (prev.time.equals(time) && cost < prev.cost())) {
            map[stopId] = new Entry(time, cost);
            if (updatesStops[stopId] == null) {
                updatedIndices[index++] = stopId;
            }
            updatesStops[stopId] = new Node(parent, transferTime, stop.tripId(),stop, stopId, time, fromTime, cost);
        }
    }


    public boolean isTimeLower(RouteStop stop, double cost) {
        Entry prev = get(stop.stopId());
        int comp = prev.time.compareTo(stop.arrival());
        return comp > 0 || (comp == 0 && cost < prev.cost);
    }

    public List<Node> pollUpdated() {
        List<Node> updated = new ArrayList<>();
        for (int i = 0; i < updatedIndices.length && i < index; i++) {
            updated.add(updatesStops[updatedIndices[i]]);
        }

        index = 0;

        round++;
        return updated;
    }

    private Entry get(int stopId) {
        if (map[stopId] == null) return Entry.INF;

        return map[stopId];
    }

    @Override
    public String toString() {
        return "ExploreHandler{" +
                "map=" + map +
                '}';
    }

    public int getRound() {
        return round;
    }

    public record Entry(Time time, double cost) {

        static Entry INF = new Entry(Time.INF, Double.POSITIVE_INFINITY);

    }
}
