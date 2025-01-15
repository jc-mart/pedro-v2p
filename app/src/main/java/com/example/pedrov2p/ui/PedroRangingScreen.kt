package com.example.pedrov2p.ui

import android.content.Context
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.SubscribeDiscoverySession
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.net.wifi.rtt.WifiRttManager
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.MacAddress
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.ServiceDiscoveryInfo
import android.net.wifi.rtt.RangingRequest
import android.net.wifi.rtt.RangingResult
import android.net.wifi.rtt.RangingResultCallback
import android.net.wifi.rtt.ResponderConfig
import androidx.core.content.ContextCompat

@Composable
fun PedroRangingScreen(
    onAbortClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO Place WIFI RTT function here and ensure that sufficient permissions have been granted
    val context = LocalContext.current
    val wifiRttManager = context.getSystemService(Context.WIFI_RTT_RANGING_SERVICE) as
            WifiRttManager
    val wifiAwareManager = context.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager

    if (!wifiAwareManager.isAvailable) {
        // TODO Redirect user to home screen with unavailability message
        Log.e("PEDRO", "wifi aware unavailable")
    }

    wifiAwareManager.attach(object: AttachCallback() {
        override fun onAttached(session: WifiAwareSession?) {
            Log.d("PEDRO", "wifi aware session attached successfully")

        }
    }, null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = dimensionResource(R.dimen.padding_small),
                end = dimensionResource(R.dimen.padding_small),
                top = dimensionResource(R.dimen.padding_small),
                bottom = dimensionResource(R.dimen.padding_medium)
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column (
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Distance"
            )
            Spacer(modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)))
            Text(
                text = "Distance Std Dev"
            )
            Spacer(modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)))
            Text(
                text = "RSSI"
            )
            Spacer(modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)))
            Text(
                text = "Successful Measurements"
            )
            Spacer(modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)))
            Text(
                text = "Attempted Measurements"
            )
        }
        Button(
            onClick = onAbortClicked,
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
        ) {
            Text(
                text = "Abort"
            )
        }
    }
}

private fun startSubscribing(context: Context, session: WifiAwareSession, wifiRttManager: WifiRttManager) {
    val config = SubscribeConfig.Builder()
        .setServiceName("PEDRO_STANDBY")
        .build()

    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE)
        == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.CHANGE_WIFI_STATE)
        == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES)
        == PackageManager.PERMISSION_GRANTED)
    {
        session.subscribe(config, object : DiscoverySessionCallback() {
            override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                Log.d("PEDRO", "subscribing started")
            }

            override fun onServiceDiscovered(info: ServiceDiscoveryInfo) {
                Log.d("PEDRO", "discovered ${info.serviceSpecificInfo.toString()}")
                val peerMacAddress = info.peerHandle
                startRanging(wifiRttManager, context, peerMacAddress)
            }
        }, null)
    }
}

private fun startRanging(wifiRttManager: WifiRttManager, context: Context, peerHandle: PeerHandle) {
    val rangingRequest = RangingRequest.Builder()
        .run {
            addWifiAwarePeer(peerHandle)
            build()
        }

    performRanging(wifiRttManager, context, rangingRequest)
}

@SuppressLint("MissingPermission")
private fun performRanging(
    wifiRttManager: WifiRttManager,
    context: Context,
    rangingRequest: RangingRequest
) {
    val executor = ContextCompat.getMainExecutor(context)
    wifiRttManager.startRanging(rangingRequest, executor, object : RangingResultCallback() {
        override fun onRangingResults(results: MutableList<RangingResult>) {
            for (result in results) {
                if (result.status == RangingResult.STATUS_SUCCESS)
                    Log.d("PEDRO", "Distance: ${result.distanceMm/1000}m")
                else
                    Log.e("PEDRO", "Ranging failed with status ${result.status}")
            }
        }

        override fun onRangingFailure(code: Int) {
            Log.e("PEDRO", "Ranging failed with error code: $code")
        }
    })
}

@Preview(showBackground = true)
@Composable
fun PedroRangingScreenPreview() {
    PedroRangingScreen(
        onAbortClicked = {}
    )
}