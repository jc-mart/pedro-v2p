package com.example.pedrov2p.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.suspendCoroutine

const val LOCATION_TAG: String = "LOCATION_HELPER"

class LocationHelper(context: Context) {
    // Coroutine setup
    private val currentContext = context
    private var job: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    // Location setup
    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(currentContext)
    private var locationCallback: LocationCallback? = null
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 0
        )
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        // .setIntervalMillis(100) // TODO Adjust if necessary
        // .setWaitForAccurateLocation(true)
        // .setGranularity(Granularity.GRANULARITY_FINE)
        // .setMaxUpdateAgeMillis(100)
        .build()
    // Values updates asynchronously
    var location: Location? = null
        private set
    var available: Boolean = false
        private set

    @SuppressLint("MissingPermission")
    suspend fun startLocationUpdates(): Boolean = suspendCoroutine {
        job = coroutineScope.launch {
            locationCallback = object : LocationCallback() {
                override fun onLocationAvailability(p0: LocationAvailability) {
                    Log.d(LOCATION_TAG, "Location " +
                            "${if (p0.isLocationAvailable) "" else "un"}available")
                }

                override fun onLocationResult(p0: LocationResult) {
                    location = p0.lastLocation
                    available = true

                    Log.d(LOCATION_TAG, "Updated coordinates: ${location?.latitude}, " +
                            "${location?.longitude}")
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                currentContext.mainExecutor,
                locationCallback!!
            )
        }
    }

    fun stopLocationUpdates() {
        job?.cancel()
        job = null

        if (locationCallback != null)
            fusedLocationClient.removeLocationUpdates(locationCallback!!)
        locationCallback = null

        Log.d(LOCATION_TAG, "Stopped location updates")
    }
}