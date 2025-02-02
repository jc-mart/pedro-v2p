package com.example.pedrov2p.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

const val LOCATION_TAG: String = "LOCATION_HELPER"

class LocationHelper(context: Context) {
    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    var lastLocation: Location? = null
    private val locationRequest = CurrentLocationRequest.Builder()
        .setGranularity(Granularity.GRANULARITY_FINE)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setMaxUpdateAgeMillis(5000)
        .setDurationMillis(1000) // TODO: Adjust if returning null
        .build()
    private var cancelToken: CancellationTokenSource? = null

    /**
     * TODO: Make this suspend and run on coroutine?
     */
    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        if (cancelToken == null) // In case of multiple location calls
            setCancellationToken()

        fusedLocationClient
            .getCurrentLocation(locationRequest, cancelToken?.token)
            .addOnSuccessListener { location: Location? ->
                lastLocation = location
                Log.d(LOCATION_TAG, "Location coordinates: ${location?.latitude}, " +
                        "${location?.longitude}")
            }
    }

    private fun setCancellationToken() {
        cancelToken = CancellationTokenSource()

        Log.d(LOCATION_TAG, "Cancellation token set")
    }

    fun cancel() {
        cancelToken?.cancel()
        cancelToken = null

        Log.d(LOCATION_TAG, "Location request cancelled")
    }
}