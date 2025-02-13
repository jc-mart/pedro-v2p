package com.example.pedrov2p.ui.screens

import android.app.Application
import android.content.Context
import android.location.Location
import android.net.wifi.aware.PeerHandle
import android.net.wifi.rtt.RangingResult
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pedrov2p.data.PedroUiState
import com.example.pedrov2p.ui.components.AwareHelper
import com.example.pedrov2p.ui.components.LocationHelper
import com.example.pedrov2p.ui.components.RttHelper
import com.example.pedrov2p.ui.components.SERVICE_NAME
import com.example.pedrov2p.ui.repositories.AwareRepository
import com.example.pedrov2p.ui.repositories.RTTRepository
import com.example.pedrov2p.ui.repositories.WifiAwareSessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

const val VM_TAG = "PedroViewModel"

class PedroViewModel(application: Application): AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PedroUiState())
    val uiState: StateFlow<PedroUiState> = _uiState.asStateFlow()
    // This will have to have methods here to update the State, followed by the UI
    private val rttHelper = RttHelper(application.applicationContext)
    private val locationHelper = LocationHelper(application.applicationContext)
    private val appContext = application

    var timeInput by mutableStateOf("13")
        private set
    var distanceInput by mutableStateOf("7.7")
        private set
    var logPrefix by mutableStateOf("rtt_")
        private set

    fun updateLogPrefix(input: String) {
        logPrefix = input
    }

    fun updateTimeThreshold(input: String) {
        timeInput = input
    }

    fun updateDistanceThreshold(input: String) {
        distanceInput = input
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
        Log.d("VM", "Address of _ui: ${System.identityHashCode(_uiState)}, ui: ${System.identityHashCode(uiState)}")
        Log.d("VM", "updated? ${_uiState.value.distance}")
    }

    fun updateFinalizedResults(finalResults: MutableList<Pair<RangingResult, Location>>) {
        TODO("Average out the values? and give a scrollable list of total passes")

    }

    fun resetForRerun() {
        TODO("Not yet implemented")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(VM_TAG, "Cleared")
    }



    suspend fun startRttRanging(iterations: Int, timeDelay: Long = 0):
        MutableList<Pair<RangingResult, Location>> = suspendCoroutine { continuation ->
        val rangingResults = mutableListOf<Pair<RangingResult, Location>>()
        updateTimeThreshold(timeInput)
        updateDistanceThreshold(distanceInput)

        viewModelScope.launch(Dispatchers.IO) {
            val locationClient = locationHelper.getLocationClient()
            val locationRequest = locationHelper.buildLocationRequest()
            val ready = locationHelper.startUpdates(
                locationClient,
                locationRequest,
                appContext.mainExecutor
            )
            val awareSession = rttHelper.startSession()
            val subscribeConfig = rttHelper.buildSubscribeConfig(SERVICE_NAME)
            val peerHandle = rttHelper.discoverPeer(awareSession, subscribeConfig)
            val rangingConfig = rttHelper.buildRangingConfig(peerHandle)

            repeat(iterations) {
                val rangingResult = rttHelper.performRtt(rangingConfig, appContext.mainExecutor)
                val currentLocation = locationHelper.location!!

                Log.d(VM_TAG, "Dist: ${rangingResult.distanceMm} Lat: ${currentLocation.latitude}")
                rangingResults.add(Pair(rangingResult, currentLocation))
                _uiState.value.distance = rangingResult.distanceMm
                delay(timeDelay)
            }

            rttHelper.stopSession(awareSession)
            _uiState.value.distanceStdDev = rangingResults[0].first.distanceStdDevMm
            // This updates the distance
            _uiState.value.distance = rangingResults[0].first.distanceMm
            updateResults(rangingResults)
            Log.d(VM_TAG, "Ranging Results: ${_uiState.value.distanceArray}")
            Log.d(VM_TAG, "State: ${_uiState}")
            Log.d(VM_TAG, "Updated UI? ${_uiState.value.distance} RSSI: ${_uiState.value.rssi}")

            continuation.resume(rangingResults)
        }

        // return rangingResults
    }

    private fun updateResults(rangingResults: MutableList<Pair<RangingResult, Location>>) {
        _uiState.value.distanceArray = rangingResults.map { (rangingResult, _) ->
            rangingResult.distanceMm
        }.toMutableList()
        _uiState.value.distanceStdDevArray = rangingResults.map { (rangingResult, _) ->
            rangingResult.distanceStdDevMm
        }.toMutableList()
        _uiState.value.rssiArray = rangingResults.map { (rangingResult, _) ->
            rangingResult.rssi
        }.toMutableList()
        _uiState.value.attemptedMeasurementsArray = rangingResults.map { (rangingResult, _) ->
            rangingResult.numAttemptedMeasurements
        }.toMutableList()
        _uiState.value.successfulMeasurementsArray = rangingResults.map { (rangingResult, _) ->
            rangingResult.numSuccessfulMeasurements
        }.toMutableList()
        _uiState.value.timestampArray = rangingResults.map { (rangingResult, _) ->
            rangingResult.rangingTimestampMillis
        }.toMutableList()
        _uiState.value.locations = rangingResults.map { (_, location) ->
            Pair(location.latitude, location.longitude)
        }.toMutableList()
    }

    fun getDistance(): Int {
        return _uiState.value.distance
    }

    fun getDistanceStdDev(): Int {
        return _uiState.value.distanceStdDev
    }
}