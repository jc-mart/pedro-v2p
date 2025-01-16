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
import com.example.pedrov2p.data.PedroUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.log

class PedroViewModel(application: Application): AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PedroUiState())
    val uiState: StateFlow<PedroUiState> = _uiState.asStateFlow()

    private var wifiAwareSession: WifiAwareSession? = null
    private var publishSession: PublishDiscoverySession? = null
    private var subscribeSession: SubscribeDiscoverySession? = null
    private var discoveredPeerHandle: PeerHandle? = null

    private val context: Context = application.applicationContext

    private fun initializeWifiAware(operation: () -> Unit) {
        val wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as
                WifiAwareManager

        if (!wifiAwareManager.isAvailable) {
            Log.e("wifi aware", "wifi aware unavailable")
            return
        }

        wifiAwareManager.attach(object : AttachCallback() {
            override fun onAttached(session: WifiAwareSession?) {
                Log.d("wifi aware", "wifi aware session attached successfully")
                wifiAwareSession = session
                operation()
            }
        }, null)
    }

    @SuppressLint("MissingPermission")
    private fun initializeWifiRtt() {
        val wifiRttManager = context.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as
                WifiRttManager
        val req: RangingRequest = RangingRequest.Builder().run {
            discoveredPeerHandle?.let { addWifiAwarePeer(it) }
            build()
        }

        if (!wifiRttManager.isAvailable) {
            Log.e("wifi rtt", "wifi rtt unavailable")
            return
        }

        wifiRttManager.startRanging(req, context.mainExecutor, object : RangingResultCallback() {
            override fun onRangingResults(results: MutableList<RangingResult>) {
                if (results.isNotEmpty()) {
                    val result: RangingResult = results[0]
                    Log.d("wifi rtt", "results: ${results[0].status}")
                }
            }

            override fun onRangingFailure(p0: Int) {
                _uiState.value = _uiState.value.copy(
                    pedroVerified = false
                )
            }
        })
    }

    fun startStandbyMode() {
        initializeWifiAware { startPublishing() }
    }

    fun startRangingMode() {
        initializeWifiAware { startSubscribing() }
    }

    fun stopStandbyMode() {
        stopPublishing()
    }

    private fun startSubscribing() {
        Log.d("wifi aware", "starting to subscribe")
        val session = wifiAwareSession ?: return
        val config = SubscribeConfig.Builder()
            .setServiceName("PEDRO_STANDBY")
            .build()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE)
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.CHANGE_WIFI_STATE)
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES)
            == PackageManager.PERMISSION_GRANTED)
        {
            Log.d("wifi rtt", "prior to subscribing")
            session.subscribe(config, object : DiscoverySessionCallback() {
                override fun onServiceDiscovered(
                    peerHandle: PeerHandle?,
                    serviceSpecificInfo: ByteArray?,
                    matchFilter: MutableList<ByteArray>?
                ) {
                    Log.d("wifi aware", "found ${serviceSpecificInfo?.decodeToString()} service")
                    discoveredPeerHandle = peerHandle
                    initializeWifiRtt()
                }
            }, null)
        }
    }

    private fun startPublishing() {
        val session = wifiAwareSession ?: return
        val config = PublishConfig.Builder()
            .setServiceName("PEDRO_STANDBY")
            .build()

        // Check if sufficient permissions are granted
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES)
            == PackageManager.PERMISSION_GRANTED)
        {
            session.publish(config, object : DiscoverySessionCallback() {
                override fun onPublishStarted(session: PublishDiscoverySession) {
                    Log.d("standby_mode", "publishing service")
                    publishSession = session
                }

                override fun onSessionConfigFailed() {
                    Log.e("standby_mode", "service publishing failed")
                }

                override fun onMessageReceived(peerHandle: PeerHandle?, message: ByteArray?) {
                    Log.d("standby_mode", "received message: ${message?.decodeToString()}")
                }

            }, null)
        } else {
            Log.e("standby_mode", "insufficient permissions granted")
        }
    }

    private fun stopPublishing() {
        publishSession?.close()
        publishSession = null
        Log.d("standby_mode", "stopped publishing service")
    }

    override fun onCleared() {
        super.onCleared()
        wifiAwareSession?.close()
        wifiAwareSession = null
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