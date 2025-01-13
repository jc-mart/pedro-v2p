package com.example.pedrov2p

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.pedrov2p.ui.theme.PEDROV2PTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val wifiAwareSupported = hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)
            val wifiRttSupported = hasSystemFeature(PackageManager.FEATURE_WIFI_RTT)

            if (wifiAwareSupported && wifiRttSupported) {
                PEDROV2PTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
            /* TODO: Have error screen as Application relies on these APIs */
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PEDROV2PTheme {
        Greeting("Android")
    }
}

@Composable
fun hasSystemFeature(feature: String): Boolean {
    return LocalContext.current.packageManager.hasSystemFeature(feature)
}