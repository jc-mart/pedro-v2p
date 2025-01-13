package com.example.pedrov2p

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.rtt.WifiRttManager
import androidx.annotation.StringRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

enum class PedroScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Settings(title = R.string.settings),
    Ranging(title = R.string.ranging),
    Standby(title = R.string.standby),
    Complete(title = R.string.complete)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedroAppBar(
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(text = "Pedro") },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier
    )
}

@Composable
fun PedroScreen() {
TODO("Will combine all screens together using NavHost")
}

@Preview(showBackground = true)
@Composable
fun PedroAppBarPreview() {
    PedroAppBar()
}