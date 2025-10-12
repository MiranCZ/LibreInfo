package com.example.mhdstuff.parsing.types;

public record RouteStop(int tripId, short postId, short sequence, Time arrival, Time departure, int delay) {

    public RouteStop(int tripId, short postId, short sequence, Time arrival, Time departure) {
        this(tripId, postId, sequence, arrival, departure, 0);
    }

    public RouteStop withDelay(int delay) {
        return new RouteStop(tripId, postId, sequence, arrival, departure, delay);
    }
}
