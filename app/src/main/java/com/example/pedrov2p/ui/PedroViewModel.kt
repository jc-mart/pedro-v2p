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

    private var wifiAwareSession: WifiAwareSession? = null
    private var publishSession: PublishDiscoverySession? = null
    private var subscribeSession: SubscribeDiscoverySession? = null
    private var discoveredPeerHandle: PeerHandle? = null
    private val mainExecutor = application.mainExecutor
    private val context: Context = application.applicationContext

    /**
     * Operation agnostic function that allows the setup of either Wi-Fi Aware's service publisher
     * or Wi-Fi RTT
     */
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
    private suspend fun performMultipleRanging(repeatCount: Int = 5): List<List<RangingResult>> = withContext(Dispatchers.IO) {
        val wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager
        val wifiRttManager = context.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as WifiRttManager

        // Attach to Wi-Fi Aware
        val session = suspendCancellableCoroutine<WifiAwareSession> { continuation ->
            wifiAwareManager.attach(object : AttachCallback() {
                override fun onAttached(session: WifiAwareSession) {
                    continuation.resume(session)
                }

                override fun onAttachFailed() {
                    continuation.resumeWithException(Exception("Wi-Fi Aware attach failed"))
                }
            }, null)
        }

        // Subscribe to find the service
        val peerHandle = suspendCancellableCoroutine<PeerHandle> { continuation ->
            val config = SubscribeConfig.Builder()
                .setServiceName("PEDRO_STANDBY")
                .build()

            session.subscribe(config, object : DiscoverySessionCallback() {
                override fun onServiceDiscovered(
                    peerHandle: PeerHandle?,
                    serviceSpecificInfo: ByteArray?,
                    matchFilter: MutableList<ByteArray>?
                ) {
                    if (peerHandle != null) {
                        continuation.resume(peerHandle)
                        Log.d("wifi aware", "Discovered service with PeerHandle: ${peerHandle.toString()}")
                    } else {
                        continuation.resumeWithException(Exception("No PeerHandle found during discovery"))
                    }
                }

                override fun onSessionConfigFailed() {
                    continuation.resumeWithException(Exception("Wi-Fi Aware discovery session config failed"))
                }

                override fun onSessionTerminated() {
                    continuation.resumeWithException(Exception("Wi-Fi Aware discovery session terminated"))
                }
            }, null)
        }

        // Perform multiple RTT measurements
        val allResults = mutableListOf<List<RangingResult>>()

        repeat(repeatCount) { attempt ->

            val request = RangingRequest.Builder()
                .addWifiAwarePeer(peerHandle)
                .build()

            if (!wifiRttManager.isAvailable) {
                Log.e("wifi rtt", "Wi-Fi RTT is unavailable")
                throw IllegalStateException("Wi-Fi RTT is unavailable")
            }

            val results = suspendCancellableCoroutine<List<RangingResult>> { continuation ->
                wifiRttManager.startRanging(
                    request,
                    context.mainExecutor,
                    object : RangingResultCallback() {
                        override fun onRangingFailure(code: Int) {
                            Log.e("wifi rtt", "Ranging failed on attempt ${attempt + 1} with code: $code")
                            continuation.resumeWithException(Exception("Ranging failed on attempt ${attempt + 1} with code: $code"))
                        }

                        override fun onRangingResults(results: MutableList<RangingResult>) {
                            if (results.isNotEmpty()) {
                                Log.d("wifi rtt", "Ranging results received on attempt ${attempt + 1}")
                                Log.d("rtt results", "$results")
                                continuation.resume(results)
                            } else {
                                continuation.resumeWithException(Exception("No RTT results received on attempt ${attempt + 1}"))
                            }
                        }
                    }
                )
            }

            allResults.add(results)
            delay(1000) // Optional: Add delay between attempts to ensure proper handling
        }

        return@withContext allResults
    }



    fun startRangingCoroutine() {
        viewModelScope.launch {
            try {
                val results = performMultipleRanging()
                val distances = 0 // results.map { it.distanceMm / 1000 }
                Log.d("wifi rtt", "results: $results")

                _uiState.value = _uiState.value.copy(
                    // distance = distances.toTypedArray(),
                    pedroVerified = true
                )
            } catch (e: Exception) {
                Log.e("wifi rtt", "ranging operation failed: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    pedroVerified = false
                )
            }
        }
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

        wifiRttManager.startRanging(req, mainExecutor, object : RangingResultCallback() {
            override fun onRangingResults(results: MutableList<RangingResult>) {
                if (results.isNotEmpty()) {
                    val result: RangingResult = results[0]
                    Log.d("wifi rtt", "result status: ${results[0].status}")
                }
            }

            override fun onRangingFailure(code: Int) {
                _uiState.value = _uiState.value.copy(
                    pedroVerified = false
                )
                Log.e("wifi rtt", "rtt ranging failed with code $code")
            }
        })
    }

    fun startStandbyMode() {
        initializeWifiAware { startPublishing() }
    }

    fun startRangingMode() {
        startRangingCoroutine()
    }

    fun stopStandbyMode() {
        stopPublishing()
    }

    private fun startSubscribing() {
        Log.d("wifi aware", "starting to subscribe")
        val session = wifiAwareSession ?: return // TODO code must be stalling here
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
                    startRangingCoroutine()
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