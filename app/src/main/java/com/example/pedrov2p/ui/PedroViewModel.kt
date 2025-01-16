package com.example.pedrov2p.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.net.wifi.rtt.WifiRttManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import com.example.pedrov2p.data.PedroUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PedroViewModel(application: Application): AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PedroUiState())
    private val app = application
    val uiState: StateFlow<PedroUiState> = _uiState.asStateFlow()

    private val wifiAwareManager = application.getSystemService(Context.WIFI_AWARE_SERVICE) as
            WifiAwareManager
    private val wifiRttManager = application.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as
            WifiRttManager

    private var publishSession: PublishDiscoverySession? = null


    private fun awareAvailable(): Boolean {
        return wifiAwareManager.isAvailable
    }

    private fun rttAvailable(): Boolean {
        return wifiRttManager.isAvailable
    }

    fun startPublishing() {
        val config = PublishConfig.Builder()
            .setServiceName("PEDRO_STANDBY")
            .build()

        // Check if sufficient permissions are granted
        if (awareAvailable() &&
            ActivityCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(app, Manifest.permission.NEARBY_WIFI_DEVICES)
            == PackageManager.PERMISSION_GRANTED)
        {
            session.publish(config, object: DiscoverySessionCallback() {
                override fun onPublishStarted(session: PublishDiscoverySession) {
                    Log.d("PEDRO", "publishing started")
                }

                override fun onSessionConfigFailed() {
                    Log.e("PEDRO", "publishing session config failed")
                }

                override fun onMessageReceived(peerHandle: PeerHandle?, message: ByteArray?) {
                    Log.d("PEDRO", "received message from peer: ${message.toString()}")
                }
            }, null)
        }
    }

    fun stopPublishing() {
        publishSession?.close()
        publishSession = null
        Log.d("standby_mode", "stopped publishing service")
    }


    fun resetForRerun() {
        _uiState.value = _uiState.value.copy(
            distance = Array(_uiState.value.maxIterations) {-1},
            distanceStdDev = Array(_uiState.value.maxIterations) {-1},
            rssi = Array(_uiState.value.maxIterations) {-1},
            is80211azNtbMeasurement = Array(_uiState.value.maxIterations) {-1},
            is80211mcMeasurement = Array(_uiState.value.maxIterations) {-1},
            attemptedMeasurements = Array(_uiState.value.maxIterations) {-1},
            successfulMeasurements = Array(_uiState.value.maxIterations) {-1},
            pedroVerified = false,
        )
    }
}