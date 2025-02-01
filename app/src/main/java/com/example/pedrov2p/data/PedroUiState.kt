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
    val timestamps: Array<Long?> = Array(maxIterations) { -1 }
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
        if (!distance.contentEquals(other.distance)) return false
        if (!distanceStdDev.contentEquals(other.distanceStdDev)) return false
        if (!rssi.contentEquals(other.rssi)) return false
        if (is80211azNtbMeasurement != other.is80211azNtbMeasurement) return false
        if (is80211mcMeasurement != other.is80211mcMeasurement) return false
        if (!attemptedMeasurements.contentEquals(other.attemptedMeasurements)) return false
        if (!successfulMeasurements.contentEquals(other.successfulMeasurements)) return false
        if (!timestamps.contentEquals(other.timestamps)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pedroVerified.hashCode()
        result = 31 * result + timeThreshold.hashCode()
        result = 31 * result + distanceThreshold
        result = 31 * result + maxIterations
        result = 31 * result + enableLogging.hashCode()
        result = 31 * result + distance.contentHashCode()
        result = 31 * result + distanceStdDev.contentHashCode()
        result = 31 * result + rssi.contentHashCode()
        result = 31 * result + is80211azNtbMeasurement.hashCode()
        result = 31 * result + is80211mcMeasurement.hashCode()
        result = 31 * result + attemptedMeasurements.contentHashCode()
        result = 31 * result + successfulMeasurements.contentHashCode()
        result = 31 * result + timestamps.contentHashCode()
        return result
    }
}
