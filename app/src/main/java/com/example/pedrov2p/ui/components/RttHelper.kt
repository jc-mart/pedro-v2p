package com.example.pedrov2p.ui.components

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.ServiceDiscoveryInfo
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.WifiRttManager
import android.util.Log

const val RTT_TAG: String = "RTT_HELPER"

class RttHelper(context: Context) {
    // Wi-Fi Aware setup
    private val wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as
            WifiAwareManager
    private val awareFilter = IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED)
    private val awareReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (wifiAwareManager.isAvailable) {
                TODO("Implement what needs to run when wifiManager is available")
            } else {
                TODO("Maybe throw an error screen here or that takes the user home")
            }
        }
    }
    private val awareConfig: SubscribeConfig = SubscribeConfig.Builder()
        .setServiceName("PEDRO_STANDBY") // We already know which service we wanna look for
        .build()
    private var discoveredPeer: PeerHandle? = null
    private var awareSession: WifiAwareSession? = null

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
    private val rttConfig: RangingRequest? = null // Will get updated once PeerHandle is found


}
