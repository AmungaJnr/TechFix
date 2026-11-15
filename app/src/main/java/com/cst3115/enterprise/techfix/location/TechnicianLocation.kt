package com.cst3115.enterprise.techfix.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Accepts either precise or approximate foreground location permission.
 */
fun hasLocationPermission(context: Context): Boolean {
    val fineGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return fineGranted || coarseGranted
}

/**
 * Requests one location fix rather than continuously tracking the technician.
 *
 * Returns null when no location is available within the timeout.
 * Permission and service failures are handled by the calling screen.
 */
@SuppressLint("MissingPermission")
suspend fun getTechnicianLocation(context: Context): LatLng? {
    if (!hasLocationPermission(context)) {
        throw SecurityException("Location permission is required.")
    }

    val client = LocationServices.getFusedLocationProviderClient(
        context.applicationContext
    )

    val cancellationSource = CancellationTokenSource()

    return try {
        withTimeoutOrNull(20_000L) {
            val location = client.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationSource.token
            ).await()

            location?.let {
                LatLng(it.latitude, it.longitude)
            }
        }
    } finally {
        cancellationSource.cancel()
    }
}