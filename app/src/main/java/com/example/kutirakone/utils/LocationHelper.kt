package com.example.kutirakone.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class LocationHelper(private val context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    fun getLastLocation(onResult: (Location?) -> Unit) {
        val tokenSource = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
            .addOnSuccessListener { current ->
                if (current != null) {
                    onResult(current)
                } else {
                    client.lastLocation
                        .addOnSuccessListener { onResult(it ?: locationManager.lastKnownDeviceLocation()) }
                        .addOnFailureListener { onResult(locationManager.lastKnownDeviceLocation()) }
                }
            }
            .addOnFailureListener {
                client.lastLocation
                    .addOnSuccessListener { onResult(it ?: locationManager.lastKnownDeviceLocation()) }
                    .addOnFailureListener { onResult(locationManager.lastKnownDeviceLocation()) }
            }
    }
}

@SuppressLint("MissingPermission")
private fun LocationManager.lastKnownDeviceLocation(): Location? {
    return listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .mapNotNull { provider ->
            try {
                if (isProviderEnabled(provider)) getLastKnownLocation(provider) else null
            } catch (_: SecurityException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }
        }
        .filter { it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0 && (it.latitude != 0.0 || it.longitude != 0.0) }
        .maxByOrNull { it.time }
}
