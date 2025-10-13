package com.example.mhdstuff.parsing.types;

import com.example.mhdstuff.parsing.storage.RouteStopStorage;

public record Trip(short serviceId, short lineId, int headsignId, boolean lowFloor, int startPos, byte length) {
    public RouteStop[] getRouteStops(RouteStopStorage storage) {
        return storage.getRouteStopsFromSegmentParsed(startPos, length);
    }
}
