package com.example.pedrov2p

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.rtt.WifiRttManager
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pedrov2p.ui.PedroRangingScreen
import com.example.pedrov2p.ui.PedroSettingsScreen
import com.example.pedrov2p.ui.PedroStandbyScreen
import com.example.pedrov2p.ui.PedroStartScreen

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
    currentScreen: PedroScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(text = "Pedro") },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (currentScreen.name == PedroScreen.Start.name) {
                IconButton(onClick = {
                    navController.navigate(PedroScreen.Settings.name)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        }
    )
}

@Composable
fun PedroApp(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = PedroScreen.valueOf(
        backStackEntry?.destination?.route ?: PedroScreen.Start.name
    )

    Scaffold (
        topBar = {
            PedroAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                navController = navController
            )
        }
    ) { innerPadding ->
        // TODO Implement ViewModel

        NavHost(
            navController = navController,
            startDestination = PedroScreen.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = PedroScreen.Start.name) {
                PedroStartScreen(
                    onClickRangeRequest = {
                        // TODO modify viewModel if needed
                        navController.navigate(PedroScreen.Ranging.name)
                    },
                    onClickStandbyMode = {
                        // TODO modify viewModel if needed
                        navController.navigate(PedroScreen.Standby.name)
                    },
                    modifier = Modifier
                )
            }
            composable(route = PedroScreen.Ranging.name) {
                // TODO once ranging is complete, go to complete screen
                PedroRangingScreen(
                    onAbortClicked = {
                        // TODO handle APIs when aborting
                        // TODO snackbar confirming aborting on back button
                        navController.navigate(PedroScreen.Start.name)
                    },
                    modifier = Modifier
                )
            }
            composable(route = PedroScreen.Standby.name) {
                PedroStandbyScreen(
                    onClickAbort = {
                        navController.navigate((PedroScreen.Start.name))
                    }
                )
            }
            composable(route = PedroScreen.Settings.name) {
                PedroSettingsScreen(

                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PedroAppPreview() {
    PedroApp()
}