package com.example.pedrov2p.ui

import android.content.Context
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.pedrov2p.R
import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.aware.PeerHandle

@Composable
fun PedroStandbyScreen(
    onClickAbort: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO throw Wi-Fi Aware code in here (Aware supported at this point)
    val context = LocalContext.current
    val wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager

    if (!wifiAwareManager.isAvailable) {
        // TODO Redirect user to home screen with unavailability message
        Log.e("PEDRO", "wifi aware unavailable")
    }

    wifiAwareManager.attach(object : AttachCallback() {
        override fun onAttached(session: WifiAwareSession) {
            Log.d("PEDRO", "wifi aware session attached successfully")
            startPublishing(context, session)
        }
    }, null)

    Column(
        modifier = modifier
            .padding(
                start = dimensionResource(R.dimen.padding_small),
                end = dimensionResource(R.dimen.padding_small),
                top = dimensionResource(R.dimen.padding_small),
                bottom = dimensionResource(R.dimen.padding_medium))
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Servicing ranging request."
            )
            Text(
                text = "Please wait."
            )
        }
        Button(
            onClick = { /* TODO: Implement this after viewmodel */ },
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
        ) {
            Text(
                text = "Abort"
            )
        }
    }
}

private fun startPublishing(context: Context, session: WifiAwareSession) {
    val config = PublishConfig.Builder()
        .setServiceName("PEDRO_STANDBY")
        .build()

    // Check if sufficient permissions are granted
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES)
        == PackageManager.PERMISSION_GRANTED)
    {
        session.publish(config, object: DiscoverySessionCallback() {
            override fun onPublishStarted(session: PublishDiscoverySession) {
                Log.d("PEDRO", "publishing started")
            }

            override fun onSessionConfigFailed() {
                Log.e("PEDRO", "publishing session config failed")
            }

            override fun onMessageReceived(peerHandle: PeerHandle?, message: ByteArray?) {
                Log.d("PEDRO", "received message from peer: ${message.toString()}")
            }
        }, null)
        }
}

@Preview(showBackground = true)
@Composable
fun PedroStandbyScreenPreview() {
    PedroStandbyScreen(
        onClickAbort = {}
    )
}