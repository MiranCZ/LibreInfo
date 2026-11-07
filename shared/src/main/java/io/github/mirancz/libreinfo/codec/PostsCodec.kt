package io.github.mirancz.libreinfo.codec

import io.github.mirancz.libreinfo.codec.util.AppInputStream
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.model.Post
import io.github.mirancz.libreinfo.model.Stop
import io.github.mirancz.libreinfo.model.types.Location
import java.io.IOException

object PostsCodec {

    @JvmStatic
    @Throws(IOException::class)
    fun write(os: CheckedOutputStream, posts: List<Post>) {
        for (p in posts) os.writeShort(p.stopId)
        for (p in posts) os.writeShort(p.postId)
        for (p in posts) os.writeLocation(p.location)

        // post names are quite repetitive (can get down to <50%)
        // does not help with export since compression carries anyway, but worth if device storage becomes an issue
        for (p in posts) os.writeString(p.name)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun read(inp: AppInputStream): List<Post> {
        val length = inp.readInt()

        val result = ArrayList<Post>()

        val stopIds = Array(length) { inp.readShort() }
        val postIds = Array(length) { inp.readShort() }
        val locations = Array(length) { inp.readLocation() }

        for (i in 0 until length) {
            result.add(Post(
                stopIds[i],
                postIds[i],
                inp.readString(),
                locations[i]
            ))
        }

        return result
    }


}