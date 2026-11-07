package io.github.mirancz.libreinfo.codec

import io.github.mirancz.libreinfo.codec.util.AppInputStream
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.model.LineAlias
import io.github.mirancz.libreinfo.model.Stop
import io.github.mirancz.libreinfo.model.types.Location
import io.github.mirancz.libreinfo.model.types.ParentStopStation
import io.github.mirancz.libreinfo.model.types.PostType
import java.io.IOException
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.ln

object StopsCodec {

    @JvmStatic
    @Throws(IOException::class)
    fun write(os: CheckedOutputStream, stops: List<Stop>) {
        os.writeInt(stops.size)

        for (e in stops) {
            val parent = e.parentStation

            var packedPost: Short = parent.postId

            if (parent.postType == PostType.PLATFORM) {
                packedPost = packedPost.or(1.shl(15).toShort())
            }

            if (parent.stopId != e.id) {
                throw IllegalStateException("Parent stop id does not match stop id of child!");
            }

            os.writeShort(e.id)
            os.writeShort(packedPost)
            os.writeLocation(e.location)
        }

        for (e in stops) os.writeString(e.name)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun read(inp: AppInputStream): List<Stop> {
        val length = inp.readInt()

        val result = ArrayList<Stop>()

        val ids = Array(length) { inp.readShort() }
        val posts = Array(length) { inp.readShort() }
        val locations = Array(length) { inp.readLocation() }

        for (i in 0 until length) {
            val packedPost = posts[i]

            val type = if (packedPost.toInt().shr(15).and(1) == 1) {
                PostType.PLATFORM
            } else {
                PostType.POST
            }
            val parent = ParentStopStation(ids[i], packedPost.and(1.shl(15).inv().toShort()), type)

            result.add(Stop(
                ids[i],
                inp.readString(),
                parent,
                locations[i],
            ))
        }

        return result
    }

}