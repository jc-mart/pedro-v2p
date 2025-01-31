package com.example.pedrov2p.data

data class PedroUiState(
    // TODO Good idea to make these a list for every individual RTT pass

    // PEDRO specific
    val pedroVerified: Boolean = false,
    val timeThreshold: Long = 3 * 1000,
    // Getting granular with MM
    val distanceThreshold: Int = 2 * 1000,
    val maxIterations: Int = 5,
    val enableLogging: Boolean = true,
    // Values retrieved from Wi-Fi RTT
    val distance: Array<Int> = Array(maxIterations) { -1 },
    val distanceStdDev:Array<Int> = Array(maxIterations) { -1 },
    val rssi: Array<Int> = Array(maxIterations) { -1 },
    val is80211azNtbMeasurement: Boolean = false,
    val is80211mcMeasurement: Boolean = false,
    val attemptedMeasurements: Array<Int> = Array(maxIterations) { -1 },
    val successfulMeasurements: Array<Int?> = Array(maxIterations) { -1 },
)
