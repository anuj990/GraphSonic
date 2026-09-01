package com.anuj.graphsonic.feature.visualization

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anuj.graphsonic.feature.audio.FrequencyMode
import com.anuj.graphsonic.feature.audio.ListenState
import com.anuj.graphsonic.feature.audio.Waveform
import com.anuj.graphsonic.feature.visualization.components.CursorInfoCard
import com.anuj.graphsonic.feature.visualization.components.GraphCanvas
import com.anuj.graphsonic.feature.visualization.components.ListenInfoCard
import com.anuj.graphsonic.feature.visualization.components.ListenPanel

@Composable
fun VisualizationScreen(
    graphLayers: List<GraphLayer>,
    cursor: GraphCursorState,
    listenState: ListenState,
    onCursorChanged:
        (GraphCursorState) -> Unit,
    evaluateAt:
        (Double) -> Double,
    onViewportChanged:
        (GraphViewport, Float) -> Unit,
    onStartListening:
        () -> Unit,
    onStopListening:
        () -> Unit,
    onExpressionEnabledChanged:
        (Long, Boolean) -> Unit,
    onExpressionAudioEnabledChanged:
        (Long, Boolean) -> Unit,
    onExpressionRemoved:
        (Long) -> Unit,
    onEditExpressions:
        () -> Unit,
    modifier: Modifier = Modifier,
    frequencyMode: FrequencyMode,
    volume: Double,
    onVolumeChanged:
        (Double) -> Unit,
    playbackSpeed: Double,
    onFrequencyModeChanged:
        (FrequencyMode) -> Unit,
    waveform: Waveform,
    onWaveformChanged:
        (Waveform) -> Unit,
    onPlaybackSpeedChanged:
        (Double) -> Unit
) {

    var controlsExpanded by
    rememberSaveable {
        mutableStateOf(false)
    }

    var layersExpanded by
    rememberSaveable {
        mutableStateOf(true)
    }

    Box(
        modifier =
            modifier.fillMaxSize()
    ) {

        GraphCanvas(
            graphLayers =
                graphLayers,
            cursor =
                cursor,
            listenState =
                listenState,
            onCursorChanged =
                onCursorChanged,
            evaluateAt =
                evaluateAt,
            onViewportChanged =
                onViewportChanged,
            modifier =
                Modifier.fillMaxSize()
        )

        CursorInfoCard(
            cursor = cursor
        )

        Column(
            modifier =
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            ListenInfoCard(
                listenState =
                    listenState,
                modifier =
                    Modifier.padding(
                        bottom = 8.dp
                    )
            )

            if (
                layersExpanded
            ) {

                LayerPanel(
                    graphLayers =
                        graphLayers,
                    onExpressionEnabledChanged =
                        onExpressionEnabledChanged,
                    onExpressionAudioEnabledChanged =
                        onExpressionAudioEnabledChanged,
                    onExpressionRemoved =
                        onExpressionRemoved,
                    onEditExpressions =
                        onEditExpressions
                )
            }

            ListenPanel(
                isPlaying =
                    listenState.isPlaying,
                frequencyMode =
                    frequencyMode,
                playbackSpeed =
                    playbackSpeed,
                volume =
                    volume,
                waveform =
                    waveform,
                expanded =
                    controlsExpanded,
                onExpandedChanged = {
                    controlsExpanded =
                        it
                },
                onStart =
                    onStartListening,
                onStop =
                    onStopListening,
                onFrequencyModeChanged =
                    onFrequencyModeChanged,
                onPlaybackSpeedChanged =
                    onPlaybackSpeedChanged,
                onVolumeChanged =
                    onVolumeChanged,
                onWaveformChanged =
                    onWaveformChanged
            )

            TextButton(
                onClick = {
                    layersExpanded =
                        !layersExpanded
                }
            ) {
                Text(
                    if (
                        layersExpanded
                    ) {
                        "Hide functions"
                    } else {
                        "Show functions"
                    }
                )
            }
        }
    }
}

@Composable
private fun LayerPanel(
    graphLayers: List<GraphLayer>,
    onExpressionEnabledChanged:
        (Long, Boolean) -> Unit,
    onExpressionAudioEnabledChanged:
        (Long, Boolean) -> Unit,
    onExpressionRemoved:
        (Long) -> Unit,
    onEditExpressions:
        () -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = "Functions",
                    modifier =
                        Modifier.weight(1f),
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )

                TextButton(
                    onClick =
                        onEditExpressions
                ) {
                    Text("Edit")
                }
            }

            if (
                graphLayers.isEmpty()
            ) {

                Text(
                    text =
                        "No functions",
                    modifier =
                        Modifier.padding(
                            vertical = 8.dp
                        )
                )

            } else {

                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 4.dp
                            ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            4.dp
                        )
                ) {

                    items(
                        items =
                            graphLayers,
                        key = {
                            it.id
                        }
                    ) { layer ->

                        LayerRow(
                            layer =
                                layer,
                            onVisibilityChanged =
                                {
                                        enabled ->
                                    onExpressionEnabledChanged(
                                        layer.id,
                                        enabled
                                    )
                                },
                            onAudioChanged =
                                {
                                        enabled ->
                                    onExpressionAudioEnabledChanged(
                                        layer.id,
                                        enabled
                                    )
                                },
                            onRemove = {
                                onExpressionRemoved(
                                    layer.id
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LayerRow(
    layer: GraphLayer,
    onVisibilityChanged:
        (Boolean) -> Unit,
    onAudioChanged:
        (Boolean) -> Unit,
    onRemove:
        () -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
                )
                .padding(
                    horizontal = 8.dp,
                    vertical = 4.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier =
                Modifier
                    .padding(
                        end = 8.dp
                    )
                    .background(
                        layerColor(
                            layer.colorIndex
                        )
                    )
        ) {
            Box(
                modifier =
                    Modifier
                        .padding(
                            horizontal = 3.dp,
                            vertical = 8.dp
                        )
            )
        }

        Text(
            text =
                layer.expression,
            modifier =
                Modifier.weight(1f),
            maxLines = 1
        )

        IconButton(
            onClick = {
                onVisibilityChanged(
                    !layer.enabled
                )
            }
        ) {
            Text(
                if (
                    layer.enabled
                ) {
                    "◉"
                } else {
                    "○"
                }
            )
        }

        IconButton(
            onClick = {
                onAudioChanged(
                    !layer.audioEnabled
                )
            }
        ) {
            Text(
                if (
                    layer.audioEnabled
                ) {
                    "♪"
                } else {
                    "×"
                }
            )
        }

        IconButton(
            onClick =
                onRemove
        ) {
            Text("×")
        }
    }
}

private fun layerColor(
    index: Int
): Color {

    return when (
        index % 8
    ) {
        0 -> Color(0xFF1565C0)
        1 -> Color(0xFFD32F2F)
        2 -> Color(0xFF2E7D32)
        3 -> Color(0xFF7B1FA2)
        4 -> Color(0xFFEF6C00)
        5 -> Color(0xFF00838F)
        6 -> Color(0xFF6D4C41)
        else -> Color(0xFFC2185B)
    }
}