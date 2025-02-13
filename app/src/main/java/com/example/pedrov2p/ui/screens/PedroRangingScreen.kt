package com.example.pedrov2p.ui.screens

import android.annotation.SuppressLint
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pedrov2p.R
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel

@SuppressLint("DefaultLocale")
@Composable
fun PedroRangingScreen(
    onAbortClicked: () -> Unit,
    onStartRanging: () -> Unit,
    onStopRanging: () -> Unit,
    viewModel: PedroViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    // TODO Place WIFI RTT function here and ensure that sufficient permissions have been granted
    LaunchedEffect(Unit) {
        onStartRanging()
    }

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

    DisposableEffect(Unit) {
        onDispose { onStopRanging() }
    }
}

@Preview(showBackground = true)
@Composable
fun PedroRangingScreenPreview() {
    PedroRangingScreen(
        onStartRanging = {},
        onAbortClicked = {},
        onStopRanging = {}
    )
}