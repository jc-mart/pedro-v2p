package com.example.pedrov2p.ui.components


import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.ServiceDiscoveryInfo
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.SubscribeDiscoverySession
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.util.Log
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

const val RTT_TAG: String = "RTT_HELPER"

open class RttHelper(context: Context, iterations: Int = 5) :
    AwareHelper(context = context, rttMode = true) {
    // Wi-Fi RTT setup
    private val wifiRttManager = context.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as
            WifiRttManager
    private var rttConfig: RangingRequest? = null // Will get updated once PeerHandle is found
    private var maxIterations: Int = iterations
    private var terminated = true
    var discoveredPeer: PeerHandle? = null
    protected var subscribeSession: SubscribeDiscoverySession? = null

    /* To be run when PeerHandle's found */
    private fun buildRttConfig() {
        if (discoveredPeer != null)
            rttConfig = RangingRequest.Builder().run {
                addWifiAwarePeer(discoveredPeer as PeerHandle)
                build()
            }
        else {
            Log.e(RTT_TAG, "Failed to build a ranging request due to missing PeerHandle")
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun startRangingSession(): MutableList<RangingResult> = suspendCoroutine { continuation ->
        buildRttConfig()
        val rangingResults = mutableListOf<RangingResult>()

        job = coroutineScope.launch {
            repeat(maxIterations) {
                if (!wifiRttManager.isAvailable) {
                    Log.e(RTT_TAG, "RTT unavailable")
                    TODO("Error out gracefully")
                }

                if (!terminated) {
                    wifiRttManager.startRanging(
                        rttConfig as RangingRequest,
                        currentContext.mainExecutor,
                        object : RangingResultCallback() {
                            override fun onRangingResults(p0: MutableList<RangingResult>) {
                                rangingResults.add(p0[0])
                                if (rangingResults.size >= maxIterations)
                                    continuation.resume(rangingResults)
                                Log.d(RTT_TAG, "Ranging results: ${p0[0]}")
                            }

                            override fun onRangingFailure(p0: Int) {
                                Log.e(RTT_TAG, "Ranging failed with error code $p0")
                            }
                        }
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun findPeer(): Boolean = suspendCoroutine { continuation ->
        awareSession?.subscribe(
            awareConfig as SubscribeConfig,
            object : DiscoverySessionCallback() {
                override fun onServiceDiscoveredWithinRange(
                    info: ServiceDiscoveryInfo,
                    distanceMm: Int
                ) {
                    Log.d(RTT_TAG, "Found a device ${distanceMm / 1000.0}m away.")
                    discoveredPeer = info.peerHandle
                    terminated = false
                    continuation.resume(true)
                }

                override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                    subscribeSession = session
                    subscribeSession!!.updateSubscribe(awareConfig as SubscribeConfig)
                    Log.d(RTT_TAG, "Subscribed to a service")
                }

                override fun onSessionTerminated() {
                    terminated = true
                }
            },
            null
        )
    }

    fun stopRanging() {
        stopAwareSession()
        // subscribeSession?.close()
        discoveredPeer = null
        Log.d(RTT_TAG, "Subscription session closed, discoveredPeer reset")
    }
}
