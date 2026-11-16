package io.github.mirancz.libreinfo.codec

import io.github.mirancz.libreinfo.codec.util.AppInputStream
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.codec.util.writeMapPool
import io.github.mirancz.libreinfo.model.TripSchedule
import io.github.mirancz.libreinfo.model.types.TripStop
import io.github.mirancz.libreinfo.model.types.StopPost
import io.github.mirancz.libreinfo.model.types.StopPattern
import java.io.IOException
import kotlin.collections.map

object RoutesCodec {

    @JvmStatic
    @Throws(IOException::class)
    fun write(os: CheckedOutputStream, tripSchedules: List<TripSchedule>) {

        val sequencePool = HashMap<StopPattern, Int>()

        val tripStops = ArrayList<TripStop>()

        os.writeInt(tripSchedules.size)
        for (e in tripSchedules) {
            val sequence = StopPattern(e.stops.map { StopPost(it.stopId, it.postId) })

            val id = sequencePool.computeIfAbsent(sequence) { sequencePool.size }

            os.writeInt(id)

            tripStops.addAll(e.stops)
        }

        val stopPostPool = HashMap<StopPost, Int>()

        writeMapPool(sequencePool, { os.writeInt(it) }) { seq ->
            os.writeInt(seq.stops.size)

            for (s in seq.stops) {
                val id = stopPostPool.computeIfAbsent(s) { stopPostPool.size }

                os.writeShort(id)
            }
        }

        writeMapPool(stopPostPool, { os.writeInt(it) }) { s ->
            os.writeShort(s.stopId)
            os.writeShort(s.postId)
        }

        for (r in tripSchedules) os.writeShort(r.stops[0].arrival.toMinutes())

        for (r in tripSchedules) {
            for (i in 1 until r.stops.size) {
                val arrival = r.stops[i].arrival.toMinutes()
                val departure = r.stops[i-1].departure.toMinutes()

                os.write(arrival-departure)
            }
        }

        for (s in tripStops) os.writeVarInt(s.departure.toMinutes() - s.arrival.toMinutes())
    }

    data class ParsedRoutes(val patternPool: Array<StopPattern>, val patterns: Array<StopPattern>, val arrivals: Array<ShortArray>, val dwells: Array<IntArray>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ParsedRoutes

            if (!patterns.contentDeepEquals(other.patterns)) return false
            if (!arrivals.contentDeepEquals(other.arrivals)) return false
            if (!dwells.contentDeepEquals(other.dwells)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = patterns.contentDeepHashCode()
            result = 31 * result + arrivals.contentDeepHashCode()
            result = 31 * result + dwells.contentDeepHashCode()
            return result
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun read(inp: AppInputStream): ParsedRoutes {
        val size = inp.readInt()

        val sequenceIds = IntArray(size) { inp.readInt() }

        val patternPool = Array(inp.readInt()) {
            val stopsSize = inp.readInt()

            ShortArray(stopsSize) { inp.readShort() }
        }

        val stopPostPool = Array(inp.readInt()) {
            StopPost(inp.readShort(), inp.readShort())
        }

        val initialTimes = ShortArray(size) { inp.readShort() }

        val patternObjectPool = Array(patternPool.size) { i ->
            StopPattern(patternPool[i].map { stopPostPool[it.toInt()] })
        }

        val patterns = Array(size) { i ->
            patternObjectPool[sequenceIds[i]]
        }

        val timeDeltas = Array(size) { i ->
            val stopsSize = patterns[i].stops.size - 1

            val arr = IntArray(stopsSize) { 0 }
            arr[0] = inp.read()

            for (n in 1 until arr.size) {
                arr[n] = arr[n-1] + inp.read()
            }

            arr
        }

//        val dwellTimes = IntArray(inp.readInt()) { inp.readVarInt() }
        val dwellTimes = Array(size) { i ->
            IntArray(patterns[i].stops.size) {
                inp.readVarInt()
            }
        }

        // create correct shape


        val arrivals = Array(size) { i ->
            val initial = initialTimes[i]

            ShortArray(timeDeltas[i].size + 1) { j ->
                if (j == 0) initial
                else (initial + (timeDeltas[i][j-1].toShort())).toShort()
            }
        }

        return ParsedRoutes(patternObjectPool, patterns, arrivals, dwellTimes)
    }

}