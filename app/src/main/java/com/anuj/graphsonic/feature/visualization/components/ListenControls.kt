package com.anuj.graphsonic.feature.visualization.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anuj.graphsonic.feature.audio.FrequencyMode

@Composable
fun ListenControls(
    isPlaying: Boolean,
    frequencyMode: FrequencyMode,
    playbackSpeed: Double,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onFrequencyModeChanged: (FrequencyMode) -> Unit,
    onPlaybackSpeedChanged: (Double) -> Unit,
    volume: Double,
    onVolumeChanged: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (isPlaying) {
                        onStop()
                    } else {
                        onStart()
                    }
                }
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

            Button(
                onClick = {
                    onFrequencyModeChanged(
                        if (
                            frequencyMode ==
                            FrequencyMode.Continuous
                        ) {
                            FrequencyMode.Musical
                        } else {
                            FrequencyMode.Continuous
                        }
                    )
                }
            ) {
                Text(
                    text =
                        if (
                            frequencyMode ==
                            FrequencyMode.Continuous
                        ) {
                            "Continuous"
                        } else {
                            "Musical"
                        }
                )
            }
        }

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onPlaybackSpeedChanged(
                        playbackSpeed - 0.25
                    )
                }
            ) {
                Text("-")
            }

            Text(
                text =
                    "Speed ${
                        "%.2fx".format(
                            playbackSpeed
                        )
                    }",
                modifier =
                    Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 12.dp
                    )
            )

            Button(
                onClick = {
                    onPlaybackSpeedChanged(
                        playbackSpeed + 0.25
                    )
                }
            ) {
                Text("+")
            }
        }
        Text(
            text =
                "Volume ${
                    (volume * 100.0).toInt()
                }%"
        )

        Slider(
            value = volume.toFloat(),
            onValueChange = {
                onVolumeChanged(
                    it.toDouble()
                )
            },
            valueRange = 0f..1f
        )
    }
}