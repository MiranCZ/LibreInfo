package io.github.mirancz.libreinfo.util.search

import android.location.Location

sealed class SortType {

    data object Alphabetical : SortType()

    data class LocationBased(val location: Location) : SortType()

}