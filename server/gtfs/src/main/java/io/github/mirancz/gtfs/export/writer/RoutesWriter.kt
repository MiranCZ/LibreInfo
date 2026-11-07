package io.github.mirancz.gtfs.export.writer

import io.github.mirancz.gtfs.ParsedGtfs
import io.github.mirancz.gtfs.export.ParseUtil
import io.github.mirancz.gtfs.export.StopInfo
import io.github.mirancz.gtfs.export.Writer
import io.github.mirancz.gtfs.ids.GtfsStopTimeEntry
import io.github.mirancz.libreinfo.codec.RoutesCodec
import io.github.mirancz.libreinfo.codec.TransitDataCodec
import io.github.mirancz.libreinfo.codec.TripsCodec
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.model.Route
import io.github.mirancz.libreinfo.model.Trip
import io.github.mirancz.libreinfo.model.types.RouteStop
import io.github.mirancz.libreinfo.model.types.Time
import java.io.IOException
import java.util.function.Function

class RoutesWriter : Writer {

    override fun write(gtfs: ParsedGtfs, creator: TransitDataCodec.StreamCreator) {
        val routes = parseRoutes(gtfs)

        try {
            val os =creator.create("routes")
            RoutesCodec.write(os, routes)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        try {
            val os = creator.create("trips")
            writeTrips(gtfs, routes, os)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun writeTrips(gtfs: ParsedGtfs, routes: List<Route>, os: CheckedOutputStream) {
        val trips = gtfs.trips

        val tripIdToRoute = HashMap<Int, Route>()
        for (route in routes) {
            tripIdToRoute[route.tripId] = route
        }

        val mapped = trips.map { t ->
            val routeId = ParseUtil.parseRoute(t.route_id)

            val route = tripIdToRoute[t.trip_id]

            val length = route!!.length

            val blockId = t.block_id ?: -1

            Trip(
                routeId,
                t.service_id,
                t.trip_headsign,
                blockId,
                t.bikes_allowed,
                length
            )
        }

        TripsCodec.write(os, mapped)
    }

    fun parseRoutes(gtfs: ParsedGtfs): List<Route> {
        data class WipRoute(
            var tripId: Int,
            var startPos: Int,
            var length: Int,
            var stops: MutableList<RouteStop>
        ) {
            constructor(tripId: Int, startPos: Int) : this(tripId, startPos, -1, ArrayList())

            fun toRoute(): Route {
                return Route(
                    tripId, startPos, length, stops
                )
            }
        }

        var currentRoute: WipRoute? = null
        var prevSequence = -1

        val stopIdToRoute: MutableMap<Short, MutableList<Int>> = HashMap()
        val routeStops: MutableList<RouteStop> = ArrayList()

        var routeStopIndex = 0
        var index = 0

        val routes: MutableList<Route> = ArrayList()

        val stopTimes = gtfs.stopTimes

        fun addRouteStop(stopInfo: StopInfo, e: GtfsStopTimeEntry) {
            val routeStop = RouteStop(
                routeStops.size, currentRoute!!.tripId, stopInfo.stopId, stopInfo.postId, index++,
                parseTime(e.arrival_time), parseTime(e.departure_time)
            )

            stopIdToRoute.computeIfAbsent(routeStop.stopId) { ArrayList() }
                .add(routeStopIndex++)

            routeStops.add(routeStop)

            currentRoute!!.stops.add(routeStop)
        }

        for ((lineNumber, e) in stopTimes.withIndex()) {
            val tripId = e.trip_id
            val stopInfo = ParseUtil.parseStop(e.stop_id)
            val sequence = e.stop_sequence

            if (currentRoute == null) {
                currentRoute = WipRoute(tripId, lineNumber)
                prevSequence = sequence - 1
            }

            // according to docs sequence must increase but can do so by arbitrary amount
            if (sequence > prevSequence) {
                check(currentRoute.tripId == tripId) { "$currentRoute ; $tripId ; $sequence" }

                addRouteStop(stopInfo, e)
            } else {
                currentRoute.length = lineNumber - currentRoute.startPos
                routes.add(currentRoute.toRoute())
                currentRoute = WipRoute(tripId, lineNumber)
                index = 0

                addRouteStop(stopInfo, e)
            }

            prevSequence = sequence
        }
        if (currentRoute != null) {
            currentRoute.length = stopTimes.lastIndex - currentRoute.startPos
            routes.add(currentRoute.toRoute())
        }

        return routes
    }

    fun parseTime(time: String): Time {
        val parts: Array<String?> = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val hours = parts[0]!!.toUByte()
        val minutes = parts[1]!!.toUByte()
//        val second = parts[2]!!.toInt()

        return Time(hours, minutes)
    }




}