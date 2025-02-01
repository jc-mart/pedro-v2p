package com.example.pedrov2p.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.Priority

const val LOCATION_TAG: String = "LOCATION_HELPER"

class LocationHelper(context: Context) {
    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private var lastLocation: Location? = null
    private val locationRequest = CurrentLocationRequest.Builder()
        .setGranularity(Granularity.GRANULARITY_FINE)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setMaxUpdateAgeMillis(5000)
        .setDurationMillis(1000) // TODO: Adjust if returning null
        .build()

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        fusedLocationClient.getCurrentLocation(locationRequest, null)
    }
}