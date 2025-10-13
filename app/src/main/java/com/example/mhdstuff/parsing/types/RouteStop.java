package com.example.mhdstuff.parsing.types;

public record RouteStop(int stopId, int tripId, short postId, short sequence, Time arrival, Time departure, int delay) {

    public RouteStop(int stopId, int tripId, short postId, short sequence, Time arrival, Time departure) {
        this(stopId, tripId, postId, sequence, arrival, departure, 0);
    }

    public RouteStop withDelay(int delay) {
        return new RouteStop(stopId, tripId, postId, sequence, arrival, departure, delay);
    }
}
