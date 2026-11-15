package io.github.mirancz.libreinfo.codec

import io.github.mirancz.libreinfo.codec.util.AppInputStream
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.codec.util.writeMapPool
import io.github.mirancz.libreinfo.model.StandaloneTrip
import io.github.mirancz.libreinfo.model.Trip
import java.io.IOException

object TripsCodec {

    @JvmStatic
    @Throws(IOException::class)
    fun write(os: CheckedOutputStream, trips: List<StandaloneTrip>) {
        os.writeInt(trips.size)

        for (e in trips) os.writeShort(e.lineId)
        for (e in trips) os.writeShort(e.serviceId)

        val headsignPool: MutableMap<String, Int> = HashMap()
        for (e in trips) {
            val headsign = e.headsign

            val headsignId = headsignPool.computeIfAbsent(headsign) { headsignPool.size }

            os.writeInt(headsignId)
        }

        for (e in trips) os.writeShort(e.blockId)
        for (e in trips) os.writeBoolean(e.lowFloor.toInt() == 1)
        for (e in trips) os.write(e.routeLength.toInt())

        writeMapPool(headsignPool, { os.writeInt(it) }) { os.writeString(it) }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun read(inp: AppInputStream): Pair<Array<Trip>, Array<String>> {
        val length = inp.readInt()

        val result = Array<Trip?>(length) { null }

        val lineIds = Array(length) { inp.readShort() }
        val serviceIds = Array(length) { inp.readShort() }
        val headsignIds = Array(length) { inp.readInt() }
        val blockIds = Array(length) { inp.readShort() }
        val bikesAllowed = Array(length) { inp.readBoolean() }
        val routeLengths = Array(length) { inp.readByte().toUByte() }

        val headsigns = Array(inp.readInt()) { inp.readString() }

        var startPos = 0;
        for (i in 0 until length) {
            result[i] = Trip(
                i,
                lineIds[i],
                serviceIds[i],
                headsignIds[i],
                blockIds[i],
                bikesAllowed[i],
                startPos,
                routeLengths[i]
            )
            startPos += routeLengths[i].toInt()
        }

        return Pair(result.requireNoNulls(), headsigns)
    }

}