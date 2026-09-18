package com.anuj.graphsonic.feature.visualization.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor =
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Listening",
                    style =
                        MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text =
                        "${(listenState.progress * 100).toInt()}%",
                    style =
                        MaterialTheme.typography.labelLarge,
                    color =
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Text(
                text =
                    "x = ${formatValue(listenState.x)}",
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme.onSecondaryContainer
            )

            listenState.voices.forEach { voice ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    Text(
                        text = voice.expression.ifBlank {
                            "Equation ${voice.equationId}"
                        },
                        modifier = Modifier.weight(1f),
                        style =
                            MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    if (voice.isDefined) {
                        Text(
                            text =
                                "${formatValue(voice.frequency)} Hz",
                            style =
                                MaterialTheme.typography.labelMedium,
                            color =
                                MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "Undefined",
                            style =
                                MaterialTheme.typography.labelMedium,
                            color =
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = {
                    listenState.progress
                        .toFloat()
                        .coerceIn(0f, 1f)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatValue(
    value: Double
): String {
    if (!value.isFinite()) {
        return "—"
    }

    return String.format(
        Locale.US,
        "%.2f",
        value
    )
}