package io.github.mirancz.libreinfo.codec

import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.codec.util.writeMapPool
import io.github.mirancz.libreinfo.model.Route
import io.github.mirancz.libreinfo.model.types.RouteStop
import io.github.mirancz.libreinfo.model.types.Time
import java.io.IOException

object RoutesCodec {


    @JvmStatic
    @Throws(IOException::class)
    fun write(os: CheckedOutputStream, routes: List<Route>) {
        // FIXME kinda shit naming; but "stop" in this sense is a specific post/platform on a stop
        data class StopPair(val stopId: Short, val postId: Short)
        data class StopSequence(val stops: List<StopPair>)

        val sequencePool = HashMap<StopSequence, Int>()

        val routeStops = ArrayList<RouteStop>()

        os.writeInt(routes.size)
        for (e in routes) {
            val sequence = StopSequence(e.stops.map { StopPair(it.stopId, it.postId) })

            val id = sequencePool.computeIfAbsent(sequence) { sequencePool.size }

            os.writeInt(id)

            routeStops.addAll(e.stops)
        }

        val stopPool = HashMap<StopPair, Int>()

        writeMapPool(sequencePool, { os.writeInt(it) }) { seq ->
            os.writeInt(seq.stops.size)

            for (s in seq.stops) {
                val id = stopPool.computeIfAbsent(s) { stopPool.size }

                os.writeShort(id)
            }
        }

        writeMapPool(stopPool, { os.writeInt(it) }) { s ->
            os.writeShort(s.stopId)
            os.writeShort(s.postId)
        }

        for (r in routes) os.writeShort(r.stops[0].arrival.toMinutes())

        for (r in routes) {
            for (i in 1 until r.stops.size) {
                val arrival = r.stops[i].arrival.toMinutes()
                val departure = r.stops[i-1].departure.toMinutes()

                os.write(arrival-departure)
            }
        }

        os.writeInt(routeStops.size)

        for (s in routeStops) os.writeVarInt(s.departure.toMinutes() - s.arrival.toMinutes())
    }

}