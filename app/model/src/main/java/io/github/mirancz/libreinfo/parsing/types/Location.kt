package io.github.mirancz.libreinfo.parsing.types

import kotlinx.serialization.Serializable

@Serializable
data class Location(val latitude: Double, val longitude: Double) {

    companion object {
        @JvmStatic
        val NONE = Location(latitude=-1.0, longitude=-1.0)
    }

}