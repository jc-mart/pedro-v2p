package com.example.pedrov2p.ui

import android.app.Application
import android.net.wifi.rtt.RangingResult
import androidx.lifecycle.AndroidViewModel
import com.example.pedrov2p.data.PedroUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class PedroViewModel(application: Application): AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PedroUiState())
    val uiState: StateFlow<PedroUiState> = _uiState.asStateFlow()

    /**
     * TODO Update DataStore structure with received values from wifi rtt
     */

    fun updateRun(results: MutableList<RangingResult>) {
        _uiState.value = _uiState.value.copy(
            distance = results.map { it.distanceMm }.toTypedArray(),
            distanceStdDev = results.map { it.distanceStdDevMm }.toTypedArray(),
            timestamps = results.map { it.rangingTimestampMillis }.toTypedArray(),
            rssi = results.map { it.rssi }.toTypedArray(),
            is80211azNtbMeasurement = results[0].is80211azNtbMeasurement,
            is80211mcMeasurement = results[0].is80211mcMeasurement,
            pedroVerified = false // TODO update this based on verify run function
        )
    }

    /**
     * TODO make sure to take timestamps of when the rtt request is done
     *
     * Iterate through all range results to find if something meets both
     * the time and distance threshold
     *
     * begin with i = 1
     * and j starts at i to avoid repeating sequences
     */
    fun verifyRun(): Boolean {
        var verified = false

        for (i in 0..uiState.value.maxIterations) {
            var j = i + 1

            for (j in i..uiState.value.maxIterations) {
                if ()
            }
        }

        return verified
    }

    fun resetForRerun() {
        _uiState.value = _uiState.value.copy(
            distance = Array(_uiState.value.maxIterations) {-1},
            distanceStdDev = Array(_uiState.value.maxIterations) {-1},
            rssi = Array(_uiState.value.maxIterations) {-1},
            is80211azNtbMeasurement = false,
            is80211mcMeasurement = false,
            attemptedMeasurements = Array(_uiState.value.maxIterations) {-1},
            successfulMeasurements = Array(_uiState.value.maxIterations) {-1},
            pedroVerified = false,
        )
    }
}