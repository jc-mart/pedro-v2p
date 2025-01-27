package com.example.pedrov2p.ui.components


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.ServiceDiscoveryInfo
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.SubscribeDiscoverySession
import android.net.wifi.aware.WifiAwareSession
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val RTT_TAG: String = "RTT_HELPER"

class RttHelper(context: Context): AwareHelper(context = context, rttMode = true) {
    // Wi-Fi RTT setup TODO: Relies on Wi-Fi Aware to find PeerHandle
    private val wifiRttManager = context.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as
            WifiRttManager
    private val rttFilter = IntentFilter(WifiRttManager.ACTION_WIFI_RTT_STATE_CHANGED)
    private val rttReceiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (wifiRttManager.isAvailable) {
                TODO("Implement this")
            } else {
                TODO("Deal with error")
            }
        }
    }
    private var rttConfig: RangingRequest? = null // Will get updated once PeerHandle is found
    internal var maxIterations: Int = 5 // TODO see about modifying these values after the fact
    internal var distanceThreshold: Int = 0 // In millimeters
    internal var timeThreshold: Int = 0 // TODO Check if ms or us
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
    fun startRangingSession() {
        buildRttConfig()

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
                                Log.d(RTT_TAG, "Ranging results: ${p0[0]}")
                            }

                            override fun onRangingFailure(p0: Int) {
                                Log.e(RTT_TAG, "Ranging failed with error code $p0")
                            }
                        })
                }

                // delay(1000)
            }
        }
    }


    @SuppressLint("MissingPermission")
    fun findPeer() {
        awareSession?.subscribe(awareConfig as SubscribeConfig, object: DiscoverySessionCallback() {
            // Only need this as the PeerHandle's needed for Wi-Fi RTT

            override fun onServiceDiscoveredWithinRange(
                info: ServiceDiscoveryInfo,
                distanceMm: Int
            ) {
                Log.d(RTT_TAG, "Found a device ${distanceMm / 1000.0}m away.")
                discoveredPeer = info.peerHandle
                terminated = false
            }

            override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                subscribeSession = session
                subscribeSession!!.updateSubscribe(awareConfig as SubscribeConfig)
                Log.d(RTT_TAG, "Subscribed to a service")
            }

            override fun onSessionTerminated() {
                terminated = true
            }

        }, null)
    }

    fun stopRanging() {
        stopAwareSession()
        // subscribeSession?.close()
        discoveredPeer = null
        Log.d(RTT_TAG, "Subscription session closed, discoveredPeer reset")
    }
}
