package com.example.busease.data.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 0f
)

object LocationHelper {

    /**
     * Attempts to get the current or best last known location using Android LocationManager.
     * Checks GPS_PROVIDER, NETWORK_PROVIDER, and PASSIVE_PROVIDER.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): UserLocation? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        // 1. Try to find the freshest last known location
        var bestLocation: Location? = null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        for (provider in providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        if (bestLocation == null || loc.time > bestLocation.time || loc.accuracy < bestLocation.accuracy) {
                            bestLocation = loc
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore provider error
            }
        }

        if (bestLocation != null) {
            return UserLocation(
                latitude = bestLocation.latitude,
                longitude = bestLocation.longitude,
                accuracyMeters = bestLocation.accuracy
            )
        }

        // 2. Request a single location update if available
        return suspendCancellableCoroutine { continuation ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(
                            UserLocation(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracyMeters = location.accuracy
                            )
                        )
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            var requested = false
            for (provider in listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)) {
                try {
                    if (locationManager.isProviderEnabled(provider)) {
                        locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                        requested = true
                        break
                    }
                } catch (e: Exception) {
                    // Try next provider
                }
            }

            if (!requested) {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }

            continuation.invokeOnCancellation {
                try {
                    locationManager.removeUpdates(listener)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }
}
