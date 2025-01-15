package com.example.pedrov2p.data

data class PedroUiState(
    // TODO Good idea to make these a list for every individual RTT pass
    // Values retrieved from Wi-Fi RTT
    val distance: Array<Int?>,
    val distanceStdDev:Array<Int?>,
    val rssi: Array<Int?>,
    val is80211azNtbMeasurement: Array<Int?>,
    val is80211mcMeasurement: Array<Int?>,
    val attemptedMeasurements: Array<Int?>,
    val successfulMeasurements: Array<Int?>,
    // PEDRO specific
    val pedroVerified: Boolean? = null,
    val timeThreshold: Long? = null,
    // Getting granular with MM
    val distanceThreshold: Int? = null,
    val maxIterations: Int = 1,
)
