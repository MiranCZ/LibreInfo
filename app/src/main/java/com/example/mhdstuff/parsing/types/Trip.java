package com.example.mhdstuff.parsing.types;

import com.example.mhdstuff.parsing.storage.RouteStopStorage;

public record Trip(int id, short serviceId, short lineId, int headsignId, short blockId, boolean lowFloor, int startPos, byte length) {
    public RouteStop[] getRouteStops(RouteStopStorage storage) {
        return storage.getRouteStopsFromSegmentParsed(startPos, length);
    }
}
