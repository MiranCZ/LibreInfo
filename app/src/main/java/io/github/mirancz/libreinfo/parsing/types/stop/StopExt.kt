package io.github.mirancz.libreinfo.parsing.types.stop

import io.github.mirancz.libreinfo.parsing.storage.StopMapper
import io.github.mirancz.libreinfo.parsing.types.Location
import io.github.mirancz.libreinfo.util.AppInputStream
import io.github.mirancz.libreinfo.util.FavouriteStops
import java.io.IOException

fun Stop.isFavourite(): Boolean {
    return FavouriteStops.isFavourite(this.id)
}

fun Stop.setFavourite(favourite: Boolean) {
    return FavouriteStops.setFavourite(this.id, favourite)
}


@Throws(IOException::class)
fun parseStops(input: AppInputStream, mapper: StopMapper): MutableList<Stop?> {
    val result: MutableList<Stop?> = ArrayList()

    while (input.readBoolean()) {
        result.add(parse(input, mapper))
    }

    return result
}

@Throws(IOException::class)
fun parse(input: AppInputStream, mapper: StopMapper): Stop {
    val stopId = input.readInt()

    val name = input.readString()
    val parentStation = input.readString()

    val lat = input.readDouble()
    val lon = input.readDouble()

    val id = StopId(stopId, mapper.getOriginal(stopId))

    return Stop(id, name!!, parentStation!!, Location(lat, lon))

}