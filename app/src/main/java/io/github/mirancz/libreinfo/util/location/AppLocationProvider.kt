package io.github.mirancz.libreinfo.util.location

import android.Manifest
import android.location.Location
import androidx.annotation.RequiresPermission

interface AppLocationProvider {

    suspend fun getLastKnownLocation(): Location?

}