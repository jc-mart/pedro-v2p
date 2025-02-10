package com.example.pedrov2p.ui

import android.app.Application
import android.location.Location
import android.net.wifi.rtt.RangingResult
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.pedrov2p.data.PedroUiState
import com.example.pedrov2p.ui.components.RTTResults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet


class PedroViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(PedroUiState())
    val uiState: StateFlow<PedroUiState> = _uiState.asStateFlow()

    /**
     * TODO Update DataStore structure with received values from wifi rtt
     */

    fun updateRun(results: MutableList<Pair<RangingResult, Location>>) {
        _uiState.value = _uiState.value.copy(
            distanceArray = results.map { it.first.distanceMm }.toTypedArray(),
            distanceStdDevArray = results.map { it.first.distanceStdDevMm }.toTypedArray(),
            rssiArray = results.map { it.first.rssi }.toTypedArray(),
            timestampArray = results.map { it.first.rangingTimestampMillis }.toTypedArray(),
            is80211azNtbMeasurement = results[0].first.is80211azNtbMeasurement,
            is80211mcMeasurement = results[0].first.is80211mcMeasurement,
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
                if (true)
                    TODO()
            }
        }

        return verified
    }

    fun updateIntermediateResult(result: Pair<RangingResult, Location>) {
        Log.d("VM", "updating intermediate values. sample: ${result.first.distanceMm}")
        _uiState.update { currentState ->
            currentState.copy(
                distance = result.first.distanceMm,
                distanceStdDev = result.first.distanceStdDevMm,
                rssi = result.first.rssi,
                successfulMeasurements = result.first.numSuccessfulMeasurements,
                attemptedMeasurements = result.first.numAttemptedMeasurements,
                latitude = result.second.latitude,
                longitude = result.second.longitude
            )
        }
        Log.d("VM", "updated? ${_uiState.value.distance}")
    }

    fun updateFinalizedResults(finalResults: MutableList<Pair<RangingResult, Location>>) {
        TODO("Average out the values? and give a scrollable list of total passes")

    }

    fun resetForRerun() {
        _uiState.value = _uiState.value.copy(
            distanceArray = Array(_uiState.value.maxIterations) {-1},
            distance = -1,
            distanceStdDevArray = Array(_uiState.value.maxIterations) {-1},
            distanceStdDev = -1,
            rssiArray = Array(_uiState.value.maxIterations) {-1},
            rssi = -1,
            is80211azNtbMeasurement = false,
            is80211mcMeasurement = false,
            attemptedMeasurementsArray = Array(_uiState.value.maxIterations) {-1},
            attemptedMeasurements = -1,
            successfulMeasurementsArray = Array(_uiState.value.maxIterations) {-1},
            successfulMeasurements = -1,
            timestampArray = Array(_uiState.value.maxIterations) { -1 },
            timestamp = -1,
            pedroVerified = false,
        )
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("VM", "Cleared")
    }
}