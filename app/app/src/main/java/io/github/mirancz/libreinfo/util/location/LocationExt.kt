@file:JvmName("LocationExt")

package io.github.mirancz.libreinfo.util.location

import android.location.LocationManager
import io.github.mirancz.libreinfo.parsing.types.Location
import org.maplibre.android.geometry.LatLng

fun Location.toLatLng(): LatLng = LatLng(latitude, longitude)

fun Location.toAndroidLoc(): android.location.Location =
    android.location.Location(LocationManager.GPS_PROVIDER).also {
        it.latitude = latitude
        it.longitude = longitude
    }
