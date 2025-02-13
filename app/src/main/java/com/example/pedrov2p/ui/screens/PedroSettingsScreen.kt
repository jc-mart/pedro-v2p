package com.example.pedrov2p.ui.screens

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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pedrov2p.R
import com.example.pedrov2p.ui.theme.Shapes

@Composable
fun PedroSettingsScreen(
    timeInput: String,
    onTimeInputChanged: (String) -> Unit,
    distanceInput: String,
    onDistanceInputChanged: (String) -> Unit,
    logInput: String,
    onLogInputChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_small)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_small))
        ) {
            Switch(
                checked = false, // todo fix this with viewmodel
                onCheckedChange = {},
                Modifier.weight(1f, fill = true)
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.padding_medium)))
            Text(
                text = "Enable logging",
                Modifier.weight(8f, fill = true)
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        OutlinedTextField(
            value = logInput,
            singleLine = true,
            shape = Shapes.large,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colorScheme.surface,
                unfocusedContainerColor = colorScheme.surface,
                disabledContainerColor = colorScheme.surface
            ),
            onValueChange = onLogInputChanged,
            label = {
                Text("Log prefix")
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        OutlinedTextField(
            value = timeInput,
            singleLine = true,
            shape = shapes.large,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colorScheme.surface,
                unfocusedContainerColor = colorScheme.surface,
                disabledContainerColor = colorScheme.surface,
            ),
            onValueChange = onTimeInputChanged,
            label = {
                Text("Time Threshold (seconds)")
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        OutlinedTextField(
            value = distanceInput,
            singleLine = true,
            shape = shapes.large,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colorScheme.surface,
                unfocusedContainerColor = colorScheme.surface,
                disabledContainerColor = colorScheme.surface,
            ),
            onValueChange = onDistanceInputChanged,
            label = {
                Text("Distance Threshold (meters)")
            },
            keyboardOptions =  KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        )

    }
}

@Preview(showBackground = true)
@Composable
fun PedroSettingsScreenPreview() {
    PedroSettingsScreen(
        timeInput = "",
        distanceInput = "",
        onDistanceInputChanged = {},
        onTimeInputChanged = {},
        logInput = "",
        onLogInputChanged = {}
    )
}