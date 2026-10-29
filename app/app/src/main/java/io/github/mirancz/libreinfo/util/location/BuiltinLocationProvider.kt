package io.github.mirancz.libreinfo.util.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import io.github.mirancz.libreinfo.util.AppLog
import io.github.mirancz.libreinfo.util.PermissionHelper

/**
 * Gets location by using built-in android methods.
 */
class BuiltinLocationProvider(context: Context) : AppLocationProvider {

    private val locationManager: LocationManager? =
        ContextCompat.getSystemService(context, LocationManager::class.java)
    private val context: Context? = context.applicationContext


    @SuppressLint("MissingPermission")
    override suspend fun getLastKnownLocation(): Location? {
        if (!PermissionHelper.locationEnabled(context)) return null

        try {
            return locationManager?.getLastKnownLocation(getLocationProvider())
        } catch (e: Exception) {
            AppLog.e("Failed to get location", e)

            return null
        }
    }


    private fun getLocationProvider(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            LocationManager.FUSED_PROVIDER
        } else {
            LocationManager.GPS_PROVIDER
        }
    }


}