package com.example.pedrov2p.ui.components


import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
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
import kotlinx.coroutines.delay
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
    private var locationHelper: LocationHelper = LocationHelper(context)
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
    suspend fun startRangingSession(): MutableList<Pair<RangingResult, Location>> = suspendCoroutine {
        continuation ->

        buildRttConfig()
        val rangingResults = mutableListOf<Pair<RangingResult, Location>>()

        job = coroutineScope.launch {
            coroutineScope.launch {
                locationHelper.startLocationUpdates()
            }
            while (!locationHelper.available)
                delay(100)

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

                            /**
                             * p0 will have only one item in its list since it's only performing
                             * a ranging request with one other device. Increases with more devices
                             */
                            override fun onRangingResults(p0: MutableList<RangingResult>) {
                                rangingResults.add(Pair(p0[0], locationHelper.location!!))

                                if (rangingResults.size >= maxIterations) {
                                    locationHelper.stopLocationUpdates()
                                    continuation.resume(rangingResults)
                                }

                                Log.d(RTT_TAG, "Distance: " +
                                        "${String.format("%.2f", p0[0].distanceMm / 1000.0)}m " +
                                        "Lat: ${locationHelper.location?.latitude}º " +
                                        "Long: ${locationHelper.location?.longitude}º ")

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
