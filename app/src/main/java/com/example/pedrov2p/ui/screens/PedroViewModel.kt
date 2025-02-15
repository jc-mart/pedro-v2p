package com.example.pedrov2p.ui.screens

import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.net.wifi.aware.WifiAwareSession
import android.net.wifi.rtt.RangingResult
import android.provider.DocumentsContract
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pedrov2p.data.PedroUiState
import com.example.pedrov2p.ui.components.LocationHelper
import com.example.pedrov2p.ui.components.RttHelper
import com.example.pedrov2p.ui.components.SERVICE_NAME
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
    private var wifiAwareSession: WifiAwareSession? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    var timeInput by mutableStateOf("13")
        private set
    var distanceInput by mutableStateOf("7.7")
        private set
    var logPrefix by mutableStateOf("rtt_")
        private set
    var maxIterations by mutableStateOf("5")
        private set
    var iterationDelay by mutableStateOf("0.0")
        private set

    fun updateIterationDelay(input: String) {
        iterationDelay = input
    }

    fun updateLogPrefix(input: String) {
        logPrefix = input
    }

    fun updateTimeThreshold(input: String) {
        timeInput = input
    }

    fun updateDistanceThreshold(input: String) {
        distanceInput = input
    }

    fun updateIterations(input: String) {
        maxIterations = input
    }

    fun isLocationUp(): Boolean {
        return fusedLocationProviderClient != null
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

        for (i in 0..uiState.value.maxIterations.toInt()) {
            var j = i + 1

            for (j in i..uiState.value.maxIterations.toInt()) {
                if (true)
                    TODO()
            }
        }

        return verified
    }

    fun resetForRerun() {
        TODO("Not yet implemented")
    }

    fun logToFile() {
        TODO("Not yet implemented")
    }

    private suspend fun startLocationServices(): Pair<FusedLocationProviderClient, LocationCallback> = suspendCancellableCoroutine { continuation ->

        viewModelScope.launch {
            val locationClient = locationHelper.getLocationClient()
            val locationRequest = locationHelper.buildLocationRequest()
            val callback: LocationCallback = locationHelper.startUpdates(
                locationClient,
                locationRequest,
                appContext.mainExecutor
            )

            fusedLocationProviderClient = locationClient
            locationCallback = callback

            continuation.resume(Pair(locationClient, callback))
        }
    }

    suspend fun startRttRanging(
        iterations: Int = maxIterations.toInt(),
        timeDelay: Long = (iterationDelay.toFloat() * 1000).toLong()
    ):
        MutableList<Pair<RangingResult, Location>> = suspendCancellableCoroutine { continuation ->
        val rangingResults = mutableListOf<Pair<RangingResult, Location>>()
        updateTimeThreshold(timeInput)
        updateDistanceThreshold(distanceInput)

        viewModelScope.launch(Dispatchers.IO) {
            val locationClientCallback = startLocationServices()

            while (locationHelper.location == null) {
                delay(1000) // Wait for Location Services to come online
            }

            val awareSession = rttHelper.startSession()
            wifiAwareSession = awareSession
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
            // TODO Get the callback to stop receiving updates
            locationHelper.stopUpdates(locationClientCallback.first, locationClientCallback.second)
            _uiState.value.distanceStdDev = rangingResults[0].first.distanceStdDevMm
            // This updates the distance
            _uiState.value.distance = rangingResults[0].first.distanceMm
            updateResults(rangingResults)
            Log.d(VM_TAG, "Ranging Results: ${_uiState.value.distanceArray}")

            continuation.resume(rangingResults)
        }

        continuation.invokeOnCancellation {
            rttHelper.stopRanging(wifiAwareSession)
            locationHelper.stopUpdates(fusedLocationProviderClient, locationCallback)
        }
        // return rangingResults
    }

    fun stopRanging() {
        rttHelper.stopRanging(wifiAwareSession)
        locationHelper.stopUpdates(fusedLocationProviderClient, locationCallback)
        locationHelper.resetLocation()

        Log.d(VM_TAG, "Aware session and child operations closed")
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

    fun logResults(uiState: PedroUiState) {
        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy-HH_mm_ss")
        val formattedTime = currentTime.format(formatter)

        appContext.openFileOutput(
            "/Documents/$logPrefix$formattedTime.csv",
            Context.MODE_PRIVATE
        ).use {
            // TODO write a header to describe the fields
            it.write(("distance, distance std dev, rssi, attempted measurements, " +
                    "successful measurements, timestamp, location, 80211mc measurements, " +
                    "time threshold, distance threshold, max iterations, pedro verified").toByteArray())
            for (i in 0 .. maxIterations.toInt()) {
                it.write(("${uiState.distanceArray[i]}, " +
                        "${uiState.distanceStdDevArray[i]}, " +
                        "${uiState.rssiArray[i]}, " +
                        "${uiState.attemptedMeasurementsArray[i]}, " +
                        "${uiState.successfulMeasurementsArray[i]}, " +
                        "${uiState.timestampArray[i]}, " +
                        "${uiState.locations[i]}").toByteArray())
            }

            it.write(("${uiState.is80211mcMeasurement}, " +
                    "${uiState.timeThreshold}, " +
                    "${uiState.distanceThreshold}, " +
                    "${uiState.maxIterations}, " +
                    "${uiState.pedroVerified}\n").toByteArray())
        }
    }
}