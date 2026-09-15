package io.github.mirancz.libreinfo.parsing.types;

import io.github.mirancz.libreinfo.parsing.types.stop.Stop;

public record RouteStop(int routeId, short stopId, int tripId, short postId, short sequence, StopTime stopTime) {

    public RouteStop(int routeId, short stopId, int tripId, short postId, short sequence, Time arrival, Time departure) {
        this(routeId, stopId, tripId, postId, sequence, new StopTime(arrival, departure));
    }

    public Time arrival() {
        return stopTime.arrival();
    }

    public Time departure() {
        return stopTime.departure();
    }

    public boolean equals(Stop stop) {
        return stopId == stop.id.internal();
    }

}
