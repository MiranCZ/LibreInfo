package io.github.mirancz.libreinfo.util.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.github.mirancz.libreinfo.util.AppLog
import io.github.mirancz.libreinfo.util.PermissionHelper
import kotlinx.coroutines.tasks.await

class PlayServicesLocationProvider : AppLocationProvider {

    private val locationClient: FusedLocationProviderClient
    private val context: Context

    constructor(context: Context) {
        locationClient = LocationServices.getFusedLocationProviderClient(context)
        this.context = context.applicationContext
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastKnownLocation(): Location? {
        if (!canFetchLocation()) return null

        try {
            return locationClient.lastLocation.await()
        } catch (e: Exception) {
            AppLog.e("Failed to get location", e)
            return null
        }
    }

    private fun canFetchLocation(): Boolean {
        if (!PermissionHelper.locationEnabled(context)) return false

        val locationManager = context.getSystemService(LocationManager::class.java) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

}