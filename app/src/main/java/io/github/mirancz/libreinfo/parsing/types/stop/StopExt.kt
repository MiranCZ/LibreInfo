package io.github.mirancz.libreinfo.parsing.types.stop

import io.github.mirancz.libreinfo.util.FavouriteStops

fun Stop.isFavourite(): Boolean {
    return FavouriteStops.isFavourite(this.id)
}

fun Stop.setFavourite(favourite: Boolean) {
    return FavouriteStops.setFavourite(this.id, favourite)
}