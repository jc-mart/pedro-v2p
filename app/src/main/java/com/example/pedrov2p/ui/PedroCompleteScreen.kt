package com.example.pedrov2p.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PedroCompleteScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
    ) {
        PedroRangingScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PedroCompleteScreenPreview() {
    PedroCompleteScreen()
}