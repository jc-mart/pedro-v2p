package com.example.pedrov2p.data

import android.location.Location
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.WifiAwareSession
import com.example.pedrov2p.ui.components.LocationHelper
import com.example.pedrov2p.ui.components.RttHelper
import com.google.android.gms.location.FusedLocationProviderClient

data class PedroUiState(
    // TODO Good idea to make these a list for every individual RTT pass

    // PEDRO specific
    var pedroVerified: Boolean = false,
    var timeThreshold: Int = 3 * 1000,
    // Getting granular with MM
    var distanceThreshold: Int = 2 * 1000,
    var maxIterations: Int = 5,
    var enableLogging: Boolean = true,
    /**
     * Values retrieved from Wi-Fi RTT
     *
     * Arrays will be for verifying PEDRO
     * Ints will be for updating screen
     */
    var distanceArray: MutableList<Int> = mutableListOf(),
    var distance: Int = -1,
    var distanceStdDevArray: MutableList<Int> = mutableListOf(),
    var distanceStdDev: Int = -1,
    var rssiArray: MutableList<Int> = mutableListOf(),
    var rssi: Int = -1,
    var is80211azNtbMeasurement: Boolean = false,
    var is80211mcMeasurement: Boolean = false,
    var attemptedMeasurementsArray: MutableList<Int> = mutableListOf(),
    var attemptedMeasurements: Int = -1,
    var successfulMeasurementsArray: MutableList<Int> = mutableListOf(),
    var successfulMeasurements: Int = -1,
    var timestampArray: MutableList<Long> = mutableListOf(),
    var timestamp: Long = -1,
    var locations: MutableList<Pair<Double, Double>> = mutableListOf(),
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    // Holds the helpers
    val wifiAwareSession: WifiAwareSession? = null,
    val publishDiscoverySession: PublishDiscoverySession? = null,
    val locationClient: FusedLocationProviderClient? = null,
    val awareHelper: RttHelper? = null,
    val rttHelper: RttHelper? = null,
    val locationHelper: LocationHelper? = null
)
