package com.example.pedrov2p.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.pedrov2p.R
import com.example.pedrov2p.ui.theme.Shapes

@Composable
fun PedroStartScreen(
    onClickRangeRequest: () -> Unit = {},
    onClickStandbyMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
            Text(
                text = "PEDRO Protocol Tool"
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
            OutlinedTextField(
                value = "",
                singleLine = true,
                shape = shapes.large,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    disabledContainerColor = colorScheme.surface,
                ),
                onValueChange = { /* TODO populate this field */ },
                label = {
                    Text("Time Threshold")
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
            OutlinedTextField(
                value = "",
                singleLine = true,
                shape = shapes.large,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    disabledContainerColor = colorScheme.surface,
                ),
                onValueChange = { /* TODO populate this field */ },
                label = {
                    Text("Distance Threshold")
                },
                keyboardOptions =  KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        }
        Row(
            modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_medium))
        ) {
            Button(
                onClick = { /* TODO Implement logic */ },
            ) {
                Text(text = stringResource(R.string.range_request))
            }
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_small)))
            Button(
                onClick = { /* TODO Implement logic */ },
            ) {
                Text(text = stringResource(R.string.standby_mode))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PedroStartScreenPreview() {
    PedroStartScreen()
}