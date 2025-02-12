package com.example.pedrov2p.data

import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.WifiAwareSession
import com.example.pedrov2p.ui.components.LocationHelper
import com.example.pedrov2p.ui.components.RttHelper
import com.google.android.gms.location.FusedLocationProviderClient

data class PedroUiState(
    // TODO Good idea to make these a list for every individual RTT pass

    // PEDRO specific
    val pedroVerified: Boolean = false,
    val timeThreshold: Long = 3 * 1000,
    // Getting granular with MM
    val distanceThreshold: Int = 2 * 1000,
    val maxIterations: Int = 5,
    val enableLogging: Boolean = true,
    /**
     * Values retrieved from Wi-Fi RTT
     *
     * Arrays will be for verifying PEDRO
     * Ints will be for updating screen
     */
    val distanceArray: Array<Int> = Array(maxIterations) { -1 },
    val distance: Int = -1,
    val distanceStdDevArray:Array<Int> = Array(maxIterations) { -1 },
    val distanceStdDev: Int = -1,
    val rssiArray: Array<Int> = Array(maxIterations) { -1 },
    val rssi: Int = -1,
    val is80211azNtbMeasurement: Boolean = false,
    val is80211mcMeasurement: Boolean = false,
    val attemptedMeasurementsArray: Array<Int> = Array(maxIterations) { -1 },
    val attemptedMeasurements: Int = -1,
    val successfulMeasurementsArray: Array<Int?> = Array(maxIterations) { -1 },
    val successfulMeasurements: Int = -1,
    val timestampArray: Array<Long?> = Array(maxIterations) { -1 },
    val timestamp: Long = -1,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    // Holds the helpers
    val wifiAwareSession: WifiAwareSession? = null,
    val publishDiscoverySession: PublishDiscoverySession? = null,
    val locationClient: FusedLocationProviderClient? = null,
    val awareHelper: RttHelper? = null,
    val rttHelper: RttHelper? = null,
    val locationHelper: LocationHelper? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PedroUiState

        if (pedroVerified != other.pedroVerified) return false
        if (timeThreshold != other.timeThreshold) return false
        if (distanceThreshold != other.distanceThreshold) return false
        if (maxIterations != other.maxIterations) return false
        if (enableLogging != other.enableLogging) return false
        if (!distanceArray.contentEquals(other.distanceArray)) return false
        if (!distanceStdDevArray.contentEquals(other.distanceStdDevArray)) return false
        if (!rssiArray.contentEquals(other.rssiArray)) return false
        if (is80211azNtbMeasurement != other.is80211azNtbMeasurement) return false
        if (is80211mcMeasurement != other.is80211mcMeasurement) return false
        if (!attemptedMeasurementsArray.contentEquals(other.attemptedMeasurementsArray)) return false
        if (!successfulMeasurementsArray.contentEquals(other.successfulMeasurementsArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pedroVerified.hashCode()
        result = 31 * result + timeThreshold.hashCode()
        result = 31 * result + distanceThreshold
        result = 31 * result + maxIterations
        result = 31 * result + enableLogging.hashCode()
        result = 31 * result + distanceArray.contentHashCode()
        result = 31 * result + distanceStdDevArray.contentHashCode()
        result = 31 * result + rssiArray.contentHashCode()
        result = 31 * result + is80211azNtbMeasurement.hashCode()
        result = 31 * result + is80211mcMeasurement.hashCode()
        result = 31 * result + attemptedMeasurementsArray.contentHashCode()
        result = 31 * result + successfulMeasurementsArray.contentHashCode()
        return result
    }
}
