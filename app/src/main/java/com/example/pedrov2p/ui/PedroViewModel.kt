package com.example.pedrov2p.ui

import android.app.Application
import android.location.Location
import android.net.wifi.aware.PeerHandle
import android.net.wifi.rtt.RangingResult
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pedrov2p.data.PedroUiState
import com.example.pedrov2p.ui.components.LocationHelper
import com.example.pedrov2p.ui.components.RttHelper
import com.example.pedrov2p.ui.repositories.AwareRepository
import com.example.pedrov2p.ui.repositories.RTTRepository
import com.example.pedrov2p.ui.repositories.WifiAwareSessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val VM_TAG = "PedroViewModel"

class PedroViewModel(application: Application): AndroidViewModel(application) {
    private val rttHelper = RttHelper(application.applicationContext)
    private val _uiState = MutableStateFlow(PedroUiState())
    val uiState: StateFlow<PedroUiState> = _uiState.asStateFlow()
    init {
        Log.d("VVMM", "ViewModel initialized ui: ${System.identityHashCode(uiState)}")
    }

    /**
     * TODO Update DataStore structure with received values from wifi rtt
     */

    fun getDistance(): Int {
        return uiState.value.distance
    }

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

    private fun updateIntermediateResult(result: Pair<RangingResult, Location>) {
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
        Log.d("VM", "Address of _ui: ${System.identityHashCode(_uiState)}, ui: ${System.identityHashCode(uiState)}")
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
        Log.d(VM_TAG, "Cleared")
    }


    suspend fun startRanging() {
        viewModelScope.launch {
            Log.d(VM_TAG, "Starting aware session")
            rttHelper.startAwareSession()
            rttHelper.findPeer()
            Log.d(VM_TAG, "Performing ranging session")
            val rangingResults = rttHelper.startRangingSession()
            rttHelper.stopRanging()
            Log.d(VM_TAG, "Updating results.")
            _uiState.update { currentState ->
                currentState.copy(
                    distance = rangingResults[0].first.distanceMm
                )
            }
            Log.d(VM_TAG, "UI hash: ${System.identityHashCode(uiState)}\n_ui:${System.identityHashCode(_uiState)}")
            Log.d(VM_TAG, "NEW DISTANCE: ${uiState.value.distance} should be ${rangingResults[0].first.distanceMm}")
            Log.d(VM_TAG, "Other: ${_uiState.value.distance}")
        }
    }

}

class PedroViewModelNew(
    private val awareRepository: AwareRepository,
    private val rttRepository: RTTRepository
) : ViewModel() {
    private val _sessionStatus = MutableStateFlow<WifiAwareSessionStatus>(
        WifiAwareSessionStatus.Idle
    )
    val sessionStatus: StateFlow<WifiAwareSessionStatus> get() = _sessionStatus

    private val _rangingResults = MutableStateFlow<List<RangingResult>>(emptyList())
    val rangingResults: StateFlow<List<RangingResult>> get() = _rangingResults

    fun initializeSession() {
        awareRepository.initializeSession()
    }

    fun publishService(serviceName: String) {
        awareRepository.publishService(serviceName)
    }

    fun subscribeAndPerformRtt(serviceName: String, iterations: Int = 5) {
        awareRepository.subscribeToService(serviceName) { peerHandle ->
            repeat(iterations) {
                performRttRanging(peerHandle)
            }
        }
    }

    private fun performRttRanging(peerHandle: PeerHandle) {
        viewModelScope.launch {
            rttRepository.performRttRanging(peerHandle) { result ->
                _rangingResults.value = _rangingResults.value.plus(result)
            }
        }
    }

    fun cleanup() {
        awareRepository.cleanup()
        _sessionStatus.value = WifiAwareSessionStatus.Idle
        _rangingResults.value = emptyList()
    }
}