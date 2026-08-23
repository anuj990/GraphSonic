package com.anuj.graphsonic.feature.visualization.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anuj.graphsonic.feature.audio.FrequencyMode
import com.anuj.graphsonic.feature.audio.Waveform
import java.util.Locale

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
        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),
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
                },
                modifier =
                    Modifier.weight(1f)
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
                    onExpandedChanged(
                        !expanded
                    )
                },
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text =
                        if (expanded) {
                            "Hide"
                        } else {
                            "Controls"
                        }
                )
            }
        }

        Text(
            text =
                "Mode: ${
                    if (
                        frequencyMode ==
                        FrequencyMode.Continuous
                    ) {
                        "Continuous"
                    } else {
                        "Musical"
                    }
                }"
        )

        Text(
            text =
                "Waveform: ${
                    waveformLabel(waveform)
                }"
        )

        if (expanded) {
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
                },
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text(
                    text =
                        if (
                            frequencyMode ==
                            FrequencyMode.Continuous
                        ) {
                            "Switch to Musical"
                        } else {
                            "Switch to Continuous"
                        }
                )
            }

            Text(
                text = "Waveform"
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onWaveformChanged(
                            previousWaveform(
                                waveform
                            )
                        )
                    },
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text("-")
                }

                Text(
                    text =
                        waveformLabel(
                            waveform
                        ),
                    modifier =
                        Modifier
                            .weight(2f)
                            .padding(
                                horizontal = 8.dp,
                                vertical = 12.dp
                            )
                )

                Button(
                    onClick = {
                        onWaveformChanged(
                            nextWaveform(
                                waveform
                            )
                        )
                    },
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text("+")
                }
            }

            Text(
                text =
                    String.format(
                        Locale.US,
                        "Speed %.2fx",
                        playbackSpeed
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onPlaybackSpeedChanged(
                            playbackSpeed - 0.25
                        )
                    },
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text("-")
                }

                Button(
                    onClick = {
                        onPlaybackSpeedChanged(
                            playbackSpeed + 0.25
                        )
                    },
                    modifier =
                        Modifier.weight(1f)
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
                value =
                    volume.toFloat(),
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

private fun nextWaveform(
    waveform: Waveform
): Waveform {
    return when (waveform) {
        Waveform.Sine ->
            Waveform.Triangle

        Waveform.Triangle ->
            Waveform.Square

        Waveform.Square ->
            Waveform.Saw

        Waveform.Saw ->
            Waveform.Sine
    }
}

private fun previousWaveform(
    waveform: Waveform
): Waveform {
    return when (waveform) {
        Waveform.Sine ->
            Waveform.Saw

        Waveform.Triangle ->
            Waveform.Sine

        Waveform.Square ->
            Waveform.Triangle

        Waveform.Saw ->
            Waveform.Square
    }
}

private fun waveformLabel(
    waveform: Waveform
): String {
    return when (waveform) {
        Waveform.Sine ->
            "Sine"

        Waveform.Triangle ->
            "Triangle"

        Waveform.Square ->
            "Square"

        Waveform.Saw ->
            "Saw"
    }
}