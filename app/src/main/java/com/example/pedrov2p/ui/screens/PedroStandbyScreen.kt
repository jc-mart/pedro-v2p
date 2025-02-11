package com.example.pedrov2p.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

@Composable
fun PedroStandbyScreen(
    onClickAbort: () -> Unit,
    onStartPublishing: () -> Unit,
    onStopPublishing: () -> Unit,
    modifier: Modifier = Modifier
) {

    LaunchedEffect(Unit) {
        onStartPublishing()
    }

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
            onClick = onClickAbort,
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
        ) {
            Text(
                text = "Abort"
            )
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            onStopPublishing()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PedroStandbyScreenPreview() {
    PedroStandbyScreen(
        onClickAbort = {},
        onStartPublishing = {},
        onStopPublishing = {}
    )
}