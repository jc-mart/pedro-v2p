package com.example.pedrov2p.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.ServiceDiscoveryInfo
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.util.Log

const val AWARE_TAG: String = "AWARE_HELPER"
const val SERVICE_NAME: String = "PEDRO_STANDBY"

open class AwareHelper(context: Context, rttMode: Boolean = false) {
    protected var currentContext = context

    private val wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as
            WifiAwareManager
    /* Register the receiver in the main screen that will contain the context */

    protected var awareConfig = if (!rttMode) {
        PublishConfig.Builder()
            .setServiceName(SERVICE_NAME)
            .setRangingEnabled(true)
            .build()
    } else {
        SubscribeConfig.Builder()
            .setServiceName(SERVICE_NAME)
            .build()
    }
    var awareSession: WifiAwareSession? = null
        private set
    private var publishSession: PublishDiscoverySession? = null

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
        awareSession?.close()
        publishSession?.close()
        awareSession = null
        publishSession = null
        Log.d(AWARE_TAG, "Closed Aware session")
    }


    @SuppressLint("MissingPermission")
    fun startService() {
        awareSession?.publish(awareConfig as PublishConfig, object: DiscoverySessionCallback() {
            override fun onPublishStarted(session: PublishDiscoverySession) {
                publishSession = session
                publishSession!!.updatePublish(awareConfig as PublishConfig)
                Log.d(AWARE_TAG, "Broadcasting service")
            }

            override fun onMessageReceived(peerHandle: PeerHandle?, message: ByteArray?) {
                Log.d(AWARE_TAG, "Received message: ${java.lang.String(message)}")
            }



        }, null)
    }
}