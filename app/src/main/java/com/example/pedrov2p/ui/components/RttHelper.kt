package com.example.pedrov2p.ui.components


import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.ServiceDiscoveryInfo
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.WifiAwareSession
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

const val RTT_TAG: String = "RTT_HELPER"

open class RttHelper(context: Context) :
    AwareHelper(context = context, rttMode = true) {
    // Wi-Fi RTT setup
    private val wifiRttManager = context.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as
            WifiRttManager

    @SuppressLint("MissingPermission")
    suspend fun discoverPeer(session: WifiAwareSession, subscribeConfig: SubscribeConfig):
        PeerHandle = suspendCancellableCoroutine { continuation ->

        session.subscribe(
        subscribeConfig,
        object : DiscoverySessionCallback() {
            override fun onServiceDiscoveredWithinRange(
                info: ServiceDiscoveryInfo,
                distanceMm: Int
            ) {
                continuation.resume(info.peerHandle)

                Log.d(
                    RTT_TAG,
                    "Discovered peer ${String.format("%.2f", distanceMm / 1000.0)}m away"
                )
            }
        },
        null
        )
    }

    fun buildRangingConfig(peerHandle: PeerHandle): RangingRequest {
        val rangingRequest = RangingRequest.Builder().apply {
            addWifiAwarePeer(peerHandle)
        }.build()

        return rangingRequest
    }

    fun buildSubscribeConfig(serviceName: String): SubscribeConfig {
        val subscribeConfig = SubscribeConfig.Builder().apply {
            setServiceName(serviceName)
            setMinDistanceMm(0)
        }.build()

        return subscribeConfig
    }

    @SuppressLint("MissingPermission")
    suspend fun performRtt(rangingRequest: RangingRequest, executor: Executor): RangingResult =
        suspendCancellableCoroutine { continuation ->

            wifiRttManager.startRanging(
                rangingRequest,
                executor,
                object : RangingResultCallback() {
                    override fun onRangingResults(results: MutableList<RangingResult>) {
                        continuation.resume(results[0])

                        Log.d(RTT_TAG, "Successfully retrieved a RangingResult")
                    }

                    override fun onRangingFailure(code: Int) {
                        Log.e(RTT_TAG, "Failed to retrieve RangingResult with error $code")
                    }
                }
            )
    }

    fun stopRanging(session: WifiAwareSession?) {
        session?.close()

        Log.d(RTT_TAG, "Aware session closed")
    }
}
