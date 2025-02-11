package com.example.pedrov2p

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pedrov2p.ui.theme.PEDROV2PTheme
import android.Manifest
import android.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.pedrov2p.ui.screens.PedroApp
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.NEARBY_WIFI_DEVICES
    )

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->
            val allPermissionsGranted = permissions.all { it.value }

            if (!allPermissionsGranted)
                showPermissionDeniedMessage()

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            checkAndRequestPermissions()
            PEDROV2PTheme {
                PedroApp()
            }
        }
    }

    private fun showPermissionDeniedMessage() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Permissions Required")
            .setMessage("Wi-Fi Aware and Wi-Fi RTT require location and Wi-Fi permissions to function" +
                    " properly")
            .setPositiveButton("OK") {_, _ -> }
            .setNegativeButton("Cancel") {_, _ -> exitProcess(-1) }
    }

    private fun checkAndRequestPermissions() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
}