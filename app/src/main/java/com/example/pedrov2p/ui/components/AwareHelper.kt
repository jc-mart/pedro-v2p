package com.example.pedrov2p.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

const val AWARE_TAG: String = "AWARE_HELPER"
const val SERVICE_NAME: String = "PEDRO_STANDBY"

open class AwareHelper(context: Context, rttMode: Boolean = false) {
    protected var currentContext = context
    protected var job: Job? = null
    protected val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as
            WifiAwareManager
    /* Register the receiver in the main screen that will contain the context */

    protected var awareConfig: Any = if (!rttMode) {
        PublishConfig.Builder()
            .setServiceName(SERVICE_NAME)
            .setRangingEnabled(true)
            .build()
    } else {
        SubscribeConfig.Builder()
            .setServiceName(SERVICE_NAME)
            .setMinDistanceMm(0)
            .build()
    }
    var awareSession: WifiAwareSession? = null
        private set
    var publishSession: PublishDiscoverySession? = null
        private set

    // Aware Specific functions
    suspend fun startAwareSession(): Boolean = suspendCoroutine { continuation ->

        if (!wifiAwareManager.isAvailable) {
            Log.e(AWARE_TAG, "Aware unavailable")
            // TODO("Deal with this")
        }

        wifiAwareManager.attach(object : AttachCallback() {
            override fun onAttached(session: WifiAwareSession?) {
                Log.d(AWARE_TAG, "Attached to a session")
                awareSession = session // TODO Close session to prevent leaks
                continuation.resume(true)
            }

            override fun onAttachFailed() {
                Log.e(AWARE_TAG, "Attaching failed")
                // continuation.resumeWithException(RuntimeException("Failed to attach to a session"))
            }
        }, null)
    }

    open fun stopAwareSession() {
        job?.cancel()
        job = null

        awareSession?.close()
        // publishSession?.close()
        awareSession = null
        publishSession = null
        Log.d(AWARE_TAG, "Closed Aware session")
    }


    @SuppressLint("MissingPermission")
    fun startService() {
        job = coroutineScope.launch {
            delay(500)

            awareSession?.publish(
                awareConfig as PublishConfig,
                object : DiscoverySessionCallback() {
                    override fun onPublishStarted(session: PublishDiscoverySession) {
                        publishSession = session
                        publishSession!!.updatePublish(awareConfig as PublishConfig)
                        Log.d(AWARE_TAG, "Broadcasting service")
                    }
                },
                null
            )
        }
    }

    suspend fun startSession(): WifiAwareSession = suspendCancellableCoroutine { continuation ->
        wifiAwareManager.attach(object : AttachCallback() {
            override fun onAttached(session: WifiAwareSession) {
                continuation.resume(session)

                Log.d(AWARE_TAG, "Successfully attached to aware session")
            }

            override fun onAttachFailed() {
                Log.e(AWARE_TAG, "Attaching failed")
            }
        },
        null)
    }

    fun stopSession(session: WifiAwareSession) {
        session.close()

        Log.d(AWARE_TAG, "Closed aware session")
    }

    fun buildPublishConfig(serviceName: String): PublishConfig {
        val publishConfig = PublishConfig.Builder().apply {
            setServiceName(serviceName)
            setRangingEnabled(true)
        }.build()

        return publishConfig
    }

    @SuppressLint("MissingPermission")
    suspend fun publishSession(session: WifiAwareSession, publishConfig: PublishConfig):
        PublishDiscoverySession = suspendCancellableCoroutine { continuation ->

        session.publish(
            publishConfig,
            object : DiscoverySessionCallback() {
                override fun onPublishStarted(session: PublishDiscoverySession) {
                    continuation.resume(session)

                    Log.d(AWARE_TAG, "Broadcasting service")
                }
            },
            null
        )
    }
}