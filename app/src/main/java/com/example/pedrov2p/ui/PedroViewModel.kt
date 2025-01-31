package com.example.pedrov2p.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.SubscribeDiscoverySession
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pedrov2p.data.PedroUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.log

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
            rssi = results.map { it.rssi }.toTypedArray(),

        )
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