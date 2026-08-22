package com.anuj.graphsonic.feature.visualization.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ListenControls(
    isPlaying: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            if (isPlaying) {
                onStop()
            } else {
                onStart()
            }
        },
        modifier = modifier
    ) {
        Text(
            text =
                if (isPlaying) {
                    "Stop"
                } else {
                    "Listen"
                }
        )
    }
}