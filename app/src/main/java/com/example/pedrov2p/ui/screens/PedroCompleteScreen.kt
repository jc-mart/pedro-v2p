package com.example.pedrov2p.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.lifecycle.viewmodel.compose.viewModel


@SuppressLint("DefaultLocale")
@Composable
fun PedroCompleteScreen(
    viewModel: PedroViewModel,
    modifier: Modifier = Modifier
) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_small))
                .weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Distance ${String.format("%.2f", viewModel.getDistance() / 1000.0)}m"
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
        Row(
            modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_medium))
        ) {
            Button(
                onClick = { /* TODO Implement logic */ },
            ) {
                Text(
                    text = "Rerun"
                )
            }
            Spacer(modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)))
            Button(
                onClick = { /* TODO Implement logic */ },
            ) {
                Text(
                    text = "Save log"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PedroCompleteScreenPreview() {
    // PedroCompleteScreen()
}