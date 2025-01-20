package com.example.pedrov2p.ui.components

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.ServiceDiscoveryInfo
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.util.Log

const val AWARE_TAG: String = "AWARE_HELPER"
const val SERVICE_NAME: String = "PEDRO_STANDBY"

class AwareHelper(context: Context, rttMode: Boolean = false) {
    private var currentContext = context

    private val wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as
            WifiAwareManager
    private val awareFilter = IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED)
    private var awareAvailable: Boolean = false
    private val awareReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            awareAvailable = wifiAwareManager.isAvailable
        }
    }
    private var awareConfig = if (!rttMode) {
        PublishConfig.Builder()
            .setServiceName(SERVICE_NAME)
            .build()
    } else {
        SubscribeConfig.Builder()
            .setServiceName(SERVICE_NAME)
            .build()
    }
    private var discoveredPeer: PeerHandle? = null
    private var awareSession: WifiAwareSession? = null

    // Aware Specific functions
    fun startAwareSession() {

        if (!wifiAwareManager.isAvailable) {
            Log.e(AWARE_TAG, "Aware unavailable")
            TODO("Deal with this")
        }

        wifiAwareManager.attach(object : AttachCallback() {
            override fun onAttached(session: WifiAwareSession?) {
                Log.d(AWARE_TAG, "Attached to a session")
                awareSession = session // TODO Close session to prevent leaks
            }

            override fun onAttachFailed() {
                Log.e(AWARE_TAG, "Attaching failed")
                TODO("Deal with this")
            }
        }, null)
    }

    fun stopAwareSession() {
        Log.d(AWARE_TAG, "Closing Aware session")
        awareSession?.close()
        awareSession = null
        Log.d(AWARE_TAG, "Closed Aware session")
    }

    @SuppressLint("MissingPermission")
    private fun findPeer() {
        awareSession?.subscribe(awareConfig as SubscribeConfig, object: DiscoverySessionCallback() {
            // Only need this as the PeerHandle's needed for Wi-Fi RTT
            override fun onServiceDiscovered(info: ServiceDiscoveryInfo) {
                Log.d(AWARE_TAG, "Found a PeerHandle")
                discoveredPeer = info.peerHandle
            }
        }, null)
    }
}