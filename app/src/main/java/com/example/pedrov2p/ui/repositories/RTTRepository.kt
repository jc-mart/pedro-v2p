package com.example.pedrov2p.ui.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.aware.PeerHandle
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.WifiRttManager
import android.util.Log

const val RTT_REPO: String = "RTT Repository"

class RTTRepository(private val context: Context) {
    private val wifiRttManager by lazy {
        context.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as? WifiRttManager
    }

    @SuppressLint("MissingPermission")
    fun performRttRanging(peerHandle: PeerHandle, onResult: (RangingResult) -> Unit) {
        val wifiRttManager = wifiRttManager ?: return
        val request = RangingRequest.Builder().run {
            addWifiAwarePeer(peerHandle)
            build()
        }

        wifiRttManager.startRanging(request, context.mainExecutor, object : RangingResultCallback() {
            override fun onRangingResults(p0: MutableList<RangingResult>) {
                p0.firstOrNull()?.let { onResult(it) }

                Log.d(RTT_REPO, "Ranging results ${if (p0[0].status == 0) "" else "in"}" +
                        "valid")
            }

            override fun onRangingFailure(p0: Int) {
                Log.e(RTT_REPO, "Ranging failed with error code $p0")
            }
        })
    }
}