package io.github.mirancz.libreinfo.parsing.types

import io.github.mirancz.libreinfo.parsing.storage.StopStorage
import io.github.mirancz.libreinfo.parsing.types.stop.StopId.Companion.internal
import io.github.mirancz.libreinfo.util.AppInputStream
import java.io.IOException

@Throws(IOException::class)
fun parsePosts(array: AppInputStream, stopStorage: StopStorage): MutableList<Post?> {
    val result: MutableList<Post?> = ArrayList()
    val size = array.readInt()

    for (i in 0..<size) {
        result.add(parse(array, stopStorage))
    }

    return result
}

@Throws(IOException::class)
fun parse(input: AppInputStream, stopStorage: StopStorage): Post {
    val stopId = input.readShort().toInt()
    val postId = input.readShort().toInt()

    val name = input.readString()

    val lat = input.readDouble()
    val lng = input.readDouble()

    val stop = stopStorage.getStop(internal(stopId))

    return Post(stop, postId, name, Location(lat, lng))
}