package io.github.mirancz.libreinfo.parsing.types;

import io.github.mirancz.libreinfo.parsing.types.stop.Stop;

public record RouteStop(short stopId, int tripId, short postId, short sequence, StopTime stopTime) {

    public RouteStop(short stopId, int tripId, short postId, short sequence, Time arrival, Time departure) {
        this(stopId, tripId, postId, sequence, new StopTime(arrival, departure));
    }

    public Time arrival() {
        return stopTime.getArrival();
    }

    public Time departure() {
        return stopTime.getDeparture();
    }

    public void setDelay(int delay) {
        stopTime.setDelay(delay);
    }

    public int delay() {
        return stopTime.getDelay();
    }

    public boolean equals(Stop stop) {
        return stopId == stop.id.internal();
    }

}
