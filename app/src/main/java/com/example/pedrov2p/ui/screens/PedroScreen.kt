package com.example.pedrov2p.ui.screens


import android.annotation.SuppressLint
import android.location.Location
import android.net.wifi.rtt.RangingResult
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pedrov2p.R
import com.example.pedrov2p.ui.components.AwareHelper
import com.example.pedrov2p.ui.components.RttHelper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        title = { Text(text = currentScreen.name) },
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

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun PedroApp(
    viewModel: PedroViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = PedroScreen.valueOf(
        backStackEntry?.destination?.route ?: PedroScreen.Start.name
    )
    val currentContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val rttHelper = RttHelper(currentContext)
    val awareHelper = AwareHelper(currentContext)
    var results: MutableList<Pair<RangingResult, Location>>
    val uiState by viewModel.uiState.collectAsState()

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
                    onStartRanging = {

                        viewModel.startRttRanging(5)

                        /**
                        coroutineScope.launch {
                            with (Dispatchers.Main) {
                                rttHelper.startAwareSession()
                                val temp = async { rttHelper.discoverPeer() }

                                results = rttHelper.startRangingSession()
                                viewModel.updateIntermediateResult(results[0])
                                Log.d("MAIN", "Distance: ${uiState.distance / 1000.0}")

                                rttHelper.stopRanging()

                                // TODO after retrieving results, goto stats screen
                                navController.navigate(PedroScreen.Complete.name)
                            }
                        }
                        **/
                    },
                    onAbortClicked = {
                        // TODO handle APIs when aborting
                        // TODO snackbar confirming aborting on back button
                        rttHelper.stopRanging()
                        navController.navigate(PedroScreen.Start.name)
                    },
                    onStopRanging = {rttHelper.stopRanging()},
                    modifier = Modifier
                )
            }
            composable(route = PedroScreen.Complete.name) {
                PedroCompleteScreen(
                )
            }
            composable(route = PedroScreen.Standby.name) {
                // val awareHelper = AwareHelper(currentContext)

                PedroStandbyScreen(
                    onClickAbort = {
                        awareHelper.stopAwareSession()
                        navController.navigate((PedroScreen.Start.name))
                    },
                    onStartPublishing = {
                        coroutineScope.launch {
                            awareHelper.startAwareSession()
                            awareHelper.startService()
                        }
                        // awareHelper.startService()
                    },
                    onStopPublishing = { awareHelper.stopAwareSession() }
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