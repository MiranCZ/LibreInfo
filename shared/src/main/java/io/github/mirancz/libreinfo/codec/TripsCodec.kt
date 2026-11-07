package io.github.mirancz.libreinfo.codec

import io.github.mirancz.libreinfo.codec.util.AppInputStream
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.codec.util.writeMapPool
import io.github.mirancz.libreinfo.model.CalendarDate
import io.github.mirancz.libreinfo.model.Trip
import java.io.IOException

object TripsCodec {

    @JvmStatic
    @Throws(IOException::class)
    fun write(os: CheckedOutputStream, trips: List<Trip>) {
        os.writeInt(trips.size)

        for (e in trips) os.writeShort(e.routeId)
        for (e in trips) os.writeShort(e.serviceId)

        val headsignPool: MutableMap<String, Int> = HashMap()
        for (e in trips) {
            val headsign = e.headsign

            val headsignId = headsignPool.computeIfAbsent(headsign) { headsignPool.size }

            os.writeInt(headsignId)
        }

        for (e in trips) os.writeShort(e.blockId)
        for (e in trips) os.writeByte(e.bikesAllowed)
        for (e in trips) os.write(e.routeLength.toInt())

        writeMapPool(headsignPool, {os.writeInt(it) }) { os.writeString(it) }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun read(inp: AppInputStream): List<Trip> {
        val length = inp.readInt()

        val result = ArrayList<Trip>()

        val routeIds = Array(length) { inp.readShort() }
        val serviceIds = Array(length) { inp.readShort() }
        val headsignIds = Array(length) { inp.readInt() }
        val blockIds = Array(length) { inp.readShort() }
        val bikesAllowed = Array(length) { inp.readByte() }
        val routeLengths = Array(length) { inp.readByte().toUByte() }

        val headsigns = Array(inp.readInt()) { inp.readString() }

        for (i in 0 until length) {
            result.add(
                Trip(
                    routeIds[i],
                    serviceIds[i],
                    headsigns[headsignIds[i]],
                    blockIds[i],
                    bikesAllowed[i],
                    routeLengths[i]
                )
            )
        }

        return result
    }

}