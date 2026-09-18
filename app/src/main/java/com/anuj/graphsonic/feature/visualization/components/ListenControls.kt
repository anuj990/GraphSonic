package com.anuj.graphsonic.feature.visualization.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale
import com.anuj.graphsonic.feature.audio.FrequencyMode
import com.anuj.graphsonic.feature.audio.Waveform

@Composable
fun ListenControls(
    isPlaying: Boolean,
    frequencyMode: FrequencyMode,
    playbackSpeed: Double,
    volume: Double,
    waveform: Waveform,
    expanded: Boolean,
    onExpandedChanged: (Boolean) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onFrequencyModeChanged: (FrequencyMode) -> Unit,
    onPlaybackSpeedChanged: (Double) -> Unit,
    onVolumeChanged: (Double) -> Unit,
    onWaveformChanged: (Waveform) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    if (isPlaying) {
                        onStop()
                    } else {
                        onStart()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector =
                        if (isPlaying) {
                            Icons.Default.Pause
                        } else {
                            Icons.Default.PlayArrow
                        },
                    contentDescription = null
                )

                Text(
                    text =
                        if (isPlaying) {
                            "Stop"
                        } else {
                            "Listen"
                        },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            OutlinedButton(
                onClick = {
                    onExpandedChanged(!expanded)
                }
            ) {
                Text(
                    text =
                        if (expanded) {
                            "Hide"
                        } else {
                            "Controls"
                        }
                )

                Icon(
                    imageVector =
                        if (expanded) {
                            Icons.Default.ExpandLess
                        } else {
                            Icons.Default.ExpandMore
                        },
                    contentDescription = null,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected =
                    frequencyMode ==
                            FrequencyMode.Continuous,
                onClick = {
                    onFrequencyModeChanged(
                        FrequencyMode.Continuous
                    )
                },
                label = {
                    Text("Continuous")
                },
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected =
                    frequencyMode ==
                            FrequencyMode.Musical,
                onClick = {
                    onFrequencyModeChanged(
                        FrequencyMode.Musical
                    )
                },
                label = {
                    Text("Musical")
                },
                modifier = Modifier.weight(1f)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter =
                expandVertically() +
                        fadeIn(),
            exit =
                shrinkVertically() +
                        fadeOut()
        ) {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Waveform",
                    style =
                        MaterialTheme.typography.labelLarge
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Waveform.entries.forEach { item ->
                        FilterChip(
                            selected = waveform == item,
                            onClick = {
                                onWaveformChanged(item)
                            },
                            label = {
                                Text(
                                    waveformLabel(item)
                                )
                            },
                            modifier =
                                Modifier.weight(1f)
                        )
                    }
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Playback speed",
                            style =
                                MaterialTheme.typography.labelLarge
                        )

                        Text(
                            text = String.format(
                                Locale.US,
                                "%.2fx",
                                playbackSpeed
                            ),
                            style =
                                MaterialTheme.typography.labelLarge,
                            color =
                                MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = playbackSpeed.toFloat(),
                        onValueChange = {
                            onPlaybackSpeedChanged(
                                it.toDouble()
                            )
                        },
                        valueRange = 0.25f..3f
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Volume",
                            style =
                                MaterialTheme.typography.labelLarge
                        )

                        Text(
                            text =
                                "${(volume * 100).toInt()}%",
                            style =
                                MaterialTheme.typography.labelLarge,
                            color =
                                MaterialTheme.colorScheme.primary
                        )
                    }

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
        }
    }
}

private fun waveformLabel(
    waveform: Waveform
): String {
    return when (waveform) {
        Waveform.Sine -> "Sine"
        Waveform.Triangle -> "Triangle"
        Waveform.Square -> "Square"
        Waveform.Saw -> "Saw"
    }
}