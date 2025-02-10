package com.example.pedrov2p.ui.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySession
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.ServiceDiscoveryInfo
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

const val AWARE_REPO: String = "Aware Repository"

class AwareRepository(private val context: Context) {
    private val wifiAwareManager by lazy {
        context.getSystemService(Context.WIFI_AWARE_SERVICE) as? WifiAwareManager
    }
    private var wifiAwareSession: WifiAwareSession? = null
    private val _sessionStatus = MutableStateFlow<WifiAwareSessionStatus>(
        WifiAwareSessionStatus.Idle
    )
    private val sessionStatus: StateFlow<WifiAwareSessionStatus> get() = _sessionStatus

    fun initializeSession() {
        wifiAwareManager.let { manager ->
            manager?.attach(object : AttachCallback() {
                override fun onAttached(session: WifiAwareSession) {
                    wifiAwareSession = session
                    _sessionStatus.value = WifiAwareSessionStatus.Connected(session)

                    Log.d(AWARE_REPO, "Attached to a session")
                }

                override fun onAttachFailed() {
                    _sessionStatus.value = WifiAwareSessionStatus.Failed

                    Log.e(AWARE_REPO, "Failed to attach to a session")
                }
            }, null)
        } ?: run {
            _sessionStatus.value = WifiAwareSessionStatus.Failed
        }
    }

    @SuppressLint("MissingPermission")
    fun publishService(serviceName: String) {
        val session = wifiAwareSession ?: return
        val publishConfig = PublishConfig.Builder()
            .setServiceName(serviceName)
            .setRangingEnabled(true)
            .build()

        session.publish(publishConfig, object : DiscoverySessionCallback() {
            override fun onPublishStarted(session: PublishDiscoverySession) {
                _sessionStatus.value = WifiAwareSessionStatus.Publishing(session)

                Log.d(AWARE_REPO, "Service published and broadcasting")
            }

            override fun onSessionConfigFailed() {
                Log.e(AWARE_REPO, "Publish config failed")
            }
        }, null)
    }

    @SuppressLint("MissingPermission")
    fun subscribeToService(serviceName: String, onPeerDiscovered: (PeerHandle) -> Unit) {
        val session = wifiAwareSession ?: return
        val subscribeConfig = SubscribeConfig.Builder()
            .setServiceName(serviceName)
            .setMinDistanceMm(0)
            .build()

        session.subscribe(subscribeConfig, object : DiscoverySessionCallback() {
            override fun onServiceDiscoveredWithinRange(
                info: ServiceDiscoveryInfo,
                distanceMm: Int
            ) {
                onPeerDiscovered(info.peerHandle)

                Log.d(AWARE_REPO, "Found peer ${String.format("%.2f", distanceMm / 1000.0)}" +
                        " meters away")
            }

            override fun onSessionConfigFailed() {
                Log.e(AWARE_REPO, "Subscribe config failed")
            }
        }, null)
    }

    fun cleanup() {
        wifiAwareSession?.close()
        _sessionStatus.value = WifiAwareSessionStatus.Idle

        Log.d(AWARE_REPO, "Closed Aware session")
    }
}

sealed class WifiAwareSessionStatus {
    data object Idle: WifiAwareSessionStatus()
    data class Connected(val session: WifiAwareSession) : WifiAwareSessionStatus()
    data class Publishing(val session: DiscoverySession) : WifiAwareSessionStatus()
    data class Subscribing(val session: DiscoverySession) : WifiAwareSessionStatus()
    data object Failed : WifiAwareSessionStatus()
}