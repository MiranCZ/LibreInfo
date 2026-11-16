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
import io.github.mirancz.libreinfo.model.TripSchedule
import io.github.mirancz.libreinfo.model.Trip
import io.github.mirancz.libreinfo.model.types.TripStop
import io.github.mirancz.libreinfo.model.types.Time
import java.io.IOException

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

    fun writeTrips(gtfs: ParsedGtfs, tripSchedules: List<TripSchedule>, os: CheckedOutputStream) {
        val trips = gtfs.trips

        val tripIdToTripSchedule = HashMap<Int, TripSchedule>()
        for (route in tripSchedules) {
            tripIdToTripSchedule[route.tripId] = route
        }

        val mapped = trips.map { t ->
            val lineId = ParseUtil.lineIdFromRoute(t.route_id)

            val route = tripIdToTripSchedule[t.trip_id]

            val length = route!!.length

            val blockId = t.block_id ?: -1

            Trip(
                t.trip_id,
                lineId,
                t.service_id,
                t.trip_headsign,
                blockId,
                t.wheelchair_accessible == 1,
                route.startPos,
                length
            )
        }

        TripsCodec.write(os, mapped)
    }

    fun parseRoutes(gtfs: ParsedGtfs): List<TripSchedule> {
        data class WipRoute(
            var tripId: Int,
            var startPos: Int,
            var length: Int,
            var stops: MutableList<TripStop>
        ) {
            constructor(tripId: Int, startPos: Int) : this(tripId, startPos, -1, ArrayList())

            fun toRoute(): TripSchedule {
                return TripSchedule(
                    tripId, startPos, length, stops
                )
            }
        }

        var currentRoute: WipRoute? = null
        var prevSequence = -1

        val stopIdToRoute: MutableMap<Short, MutableList<Int>> = HashMap()
        val tripStops: MutableList<TripStop> = ArrayList()

        var routeStopIndex = 0
        var index = 0

        val tripSchedules: MutableList<TripSchedule> = ArrayList()

        val stopTimes = gtfs.stopTimes

        fun addRouteStop(stopInfo: StopInfo, e: GtfsStopTimeEntry) {
            val tripStop = TripStop(
                tripStops.size, currentRoute!!.tripId, stopInfo.stopId, stopInfo.postId, index++,
                parseTime(e.arrival_time), parseTime(e.departure_time)
            )

            stopIdToRoute.computeIfAbsent(tripStop.stopId) { ArrayList() }
                .add(routeStopIndex++)

            tripStops.add(tripStop)

            currentRoute!!.stops.add(tripStop)
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
                tripSchedules.add(currentRoute.toRoute())
                currentRoute = WipRoute(tripId, lineNumber)
                index = 0

                addRouteStop(stopInfo, e)
            }

            prevSequence = sequence
        }
        if (currentRoute != null) {
            currentRoute.length = stopTimes.size - currentRoute.startPos
            tripSchedules.add(currentRoute.toRoute())
        }

        return tripSchedules
    }

    fun parseTime(time: String): Time {
        val parts: Array<String?> = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val hours = parts[0]!!.toUByte()
        val minutes = parts[1]!!.toUByte()
//        val second = parts[2]!!.toInt()

        return Time(hours, minutes)
    }




}