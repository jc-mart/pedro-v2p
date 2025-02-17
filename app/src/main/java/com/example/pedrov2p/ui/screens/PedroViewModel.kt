package com.example.pedrov2p.ui.screens

import android.app.Application
import android.content.ContentValues
import android.location.Location
import android.net.wifi.aware.WifiAwareSession
import android.net.wifi.rtt.RangingResult
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pedrov2p.data.PedroUiState
import com.example.pedrov2p.ui.components.LocationHelper
import com.example.pedrov2p.ui.components.PedroHelper
import com.example.pedrov2p.ui.components.RttHelper
import com.example.pedrov2p.ui.components.SERVICE_NAME
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume

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

    fun resetForRerun() {
        TODO("Not yet implemented")
    }

    fun getVerified(): Boolean {
        return _uiState.value.pedroVerified
    }

    fun getVerifiedPairs(): Pair<Int, Int> {
        return _uiState.value.verifiedPairs
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
                val verified = PedroHelper().verifyRun(
                    rangingResults,
                    distanceInput.toFloat(),
                    timeInput.toFloat()
                )
                if (verified != Pair(-1, -1) && !_uiState.value.pedroVerified) {
                    _uiState.value.pedroVerified = true
                    _uiState.value.verifiedPairs = verified
                }

                delay(timeDelay)
            }

            rttHelper.stopSession(awareSession)
            // TODO Get the callback to stop receiving updates
            locationHelper.stopUpdates(locationClientCallback.first, locationClientCallback.second)
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

    fun logResults() {
        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy-HH_mm_ss")
        val formattedTime = currentTime.format(formatter)
        val filename = "$logPrefix$formattedTime.csv"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/")
        }

        val uri = appContext.contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            contentValues
        )

        uri?.let {
            appContext.contentResolver.openOutputStream(it)?.use { outputStream ->
                val writer = OutputStreamWriter(outputStream)

                writer.append(
                    "iteration, " +
                    "distance (mm), " +
                    "distance std dev (mm), " +
                    "rssi, " +
                    "attempted measurements, " +
                    "successful measurements, " +
                    "timestamp (ms), " +
                    "location, " +
                    "80211mc measurements, " +
                    "time threshold (ms), " +
                    "distance threshold (mm), " +
                    "max iterations, " +
                    "iteration delay (ms), " +
                    "pedro verified, " +
                    "verified pairs\n"
                    )

                for (i in 0 ..< maxIterations.toInt()) {
                    writer.append(
                        "${i + 1}, " +
                        "${_uiState.value.distanceArray[i]}, " +
                        "${_uiState.value.distanceStdDevArray[i]}, " +
                        "${_uiState.value.rssiArray[i]}, " +
                        "${_uiState.value.attemptedMeasurementsArray[i]}, " +
                        "${_uiState.value.successfulMeasurementsArray[i]}, " +
                        "${_uiState.value.timestampArray[i]}, " +
                        "${_uiState.value.locations[i].first} / ${_uiState.value.locations[i].second}, " +
                        "${_uiState.value.is80211mcMeasurement}, " +
                        "${(timeInput.toFloat() * 1000).toInt()}, " +
                        "${(distanceInput.toFloat() * 1000).toInt()}, " +
                        "${maxIterations}, " +
                        "${(iterationDelay.toFloat() * 1000).toInt()}, "        +
                        "${_uiState.value.pedroVerified}, " +
                        "${_uiState.value.verifiedPairs}\n"
                    )
                }

                writer.flush()
                writer.close()
            }
        }
    }
}