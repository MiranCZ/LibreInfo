package io.github.mirancz.libreinfo.parsing

import io.github.mirancz.libreinfo.parsing.storage.RouteStopStorage
import io.github.mirancz.libreinfo.parsing.types.RouteStop
import io.github.mirancz.libreinfo.model.Trip

fun Trip.getRouteStops(storage: RouteStopStorage): Array<RouteStop> {
    return storage.getRouteStopsFromSegmentParsed(startPos, this.routeLength.toInt())
}