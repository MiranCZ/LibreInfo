package io.github.mirancz.gtfs.export.writer

import io.github.mirancz.gtfs.ParsedGtfs
import io.github.mirancz.gtfs.export.ParseUtil
import io.github.mirancz.gtfs.export.SingleWriter
import io.github.mirancz.libreinfo.codec.StopsCodec
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.model.Stop
import io.github.mirancz.libreinfo.model.types.Location
import io.github.mirancz.libreinfo.model.types.ParentStopStation

class StopWriter : SingleWriter("stops") {


    override fun write(gtfs: ParsedGtfs, os: CheckedOutputStream) {
        val stops = gtfs.stops

        val processed = HashSet<Int>()
        stops.removeIf { s ->
            val stopId = ParseUtil.parseStop(s.stop_id).stopId.toInt()

            if (!processed.contains(stopId)) {
                processed.add(stopId)

                false
            } else {
                true
            }
        }

        val mapped = stops.map { s ->
            val stopId = ParseUtil.parseStop(s.stop_id).stopId

            val parentS = ParseUtil.parseStop(s.parent_station)
            val parentPostStation = ParentStopStation(parentS.stopId, parentS.postId, parentS.type)

            Stop(
                stopId,
                s.stop_name,
                parentPostStation,
                Location(s.stop_lat, s.stop_lon)
            )
        }

        StopsCodec.write(os, mapped)
    }


}