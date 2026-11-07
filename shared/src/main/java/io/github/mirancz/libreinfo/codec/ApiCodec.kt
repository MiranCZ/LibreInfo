package io.github.mirancz.libreinfo.codec

import io.github.mirancz.libreinfo.codec.util.AppInputStream
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.model.ApiEntry
import java.io.IOException

object ApiCodec {

    @JvmStatic
    @Throws(IOException::class)
    fun write(os: CheckedOutputStream, api: List<ApiEntry>) {
        os.writeInt(api.size)

        for (e in api) os.writeInt(e.tripId)
        for (e in api) os.writeShort(e.lineId)
        for (e in api) os.writeShort(e.routeId)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun read(inp: AppInputStream): List<ApiEntry> {
        val length = inp.readInt()

        val result = ArrayList<ApiEntry>()

        val tripIds = Array(length) { inp.readInt() }
        val lineIds = Array(length) { inp.readShort() }

        for (i in 0 until length) {
            result.add(ApiEntry(
                tripIds[i],
                lineIds[i],
                inp.readShort()
            ))
        }

        return result
    }

}