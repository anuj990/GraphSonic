package com.anuj.graphsonic.feature.visualization.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anuj.graphsonic.feature.audio.ListenState
import java.util.Locale

@Composable
fun ListenInfoCard(
    listenState: ListenState,
    modifier: Modifier = Modifier
) {
    if (!listenState.isPlaying) {
        return
    }

    Card(
        modifier = modifier
    ) {
        Column(
            modifier =
                Modifier.padding(12.dp),
            verticalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text =
                    "x = ${
                        formatValue(
                            listenState.x
                        )
                    }"
            )

            listenState.voices.forEach { voice ->

                Column(
                    modifier =
                        Modifier.padding(
                            vertical = 2.dp
                        )
                ) {

                    Text(
                        text =
                            voice.expression.ifBlank {
                                "Equation ${voice.equationId}"
                            }
                    )

                    if (
                        voice.isDefined
                    ) {

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    12.dp
                                )
                        ) {

                            Text(
                                text =
                                    "y = ${
                                        formatValue(
                                            voice.y
                                        )
                                    }"
                            )

                            Text(
                                text =
                                    "${
                                        formatValue(
                                            voice.frequency
                                        )
                                    } Hz"
                            )
                        }

                        voice.note?.let { note ->

                            Text(
                                text =
                                    "Note = ${note.name}${note.octave}"
                            )
                        }

                    } else {

                        Text(
                            text =
                                "Undefined"
                        )
                    }
                }
            }

            Text(
                text =
                    "Progress = ${
                        (
                                listenState.progress *
                                        100.0
                                ).toInt()
                    }%"
            )

            LinearProgressIndicator(
                progress = {
                    listenState.progress
                        .toFloat()
                        .coerceIn(
                            0f,
                            1f
                        )
                }
            )
        }
    }
}

private fun formatValue(
    value: Double
): String {

    if (
        !value.isFinite()
    ) {
        return "—"
    }

    return String.format(
        Locale.US,
        "%.2f",
        value
    )
}