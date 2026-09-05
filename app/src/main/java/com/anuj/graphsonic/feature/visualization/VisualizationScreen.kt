package com.anuj.graphsonic.feature.visualization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onCursorChanged: (GraphCursorState) -> Unit,
    evaluateAt: (Double) -> Double,
    onViewportChanged: (GraphViewport, Float) -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onAddExpression: (String) -> Boolean,
    onExpressionEnabledChanged: (Long, Boolean) -> Unit,
    onExpressionAudioEnabledChanged: (Long, Boolean) -> Unit,
    onRemoveExpression: (Long) -> Unit,
    modifier: Modifier = Modifier,
    frequencyMode: FrequencyMode,
    volume: Double,
    onVolumeChanged: (Double) -> Unit,
    playbackSpeed: Double,
    onFrequencyModeChanged: (FrequencyMode) -> Unit,
    waveform: Waveform,
    onWaveformChanged: (Waveform) -> Unit,
    onPlaybackSpeedChanged: (Double) -> Unit
) {

    var controlsExpanded by
    rememberSaveable {
        mutableStateOf(false)
    }

    var addDialogVisible by
    rememberSaveable {
        mutableStateOf(false)
    }

    var newExpression by
    rememberSaveable {
        mutableStateOf("")
    }

    var addError by
    rememberSaveable {
        mutableStateOf<String?>(null)
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
            cursor =
                cursor
        )

        Column(
            modifier =
                Modifier
                    .align(
                        Alignment.TopCenter
                    )
                    .fillMaxWidth()
                    .padding(12.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        "Equations"
                )

                Button(
                    onClick = {
                        if (
                            graphLayers.size < 8
                        ) {
                            newExpression =
                                ""
                            addError =
                                null
                            addDialogVisible =
                                true
                        }
                    },
                    enabled =
                        graphLayers.size < 8
                ) {
                    Text(
                        text =
                            "Add equation"
                    )
                }
            }

            if (
                graphLayers.isNotEmpty()
            ) {

                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 8.dp
                            )
                ) {

                    items(
                        items =
                            graphLayers,
                        key =
                            {
                                it.id
                            }
                    ) { layer ->

                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        vertical = 4.dp
                                    )
                        ) {

                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Column(
                                    modifier =
                                        Modifier.weight(1f)
                                ) {

                                    Text(
                                        text =
                                            layer.expression
                                    )

                                    Row(
                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        Text(
                                            text =
                                                "Graph"
                                        )

                                        Switch(
                                            checked =
                                                layer.enabled,
                                            onCheckedChange =
                                                {
                                                    onExpressionEnabledChanged(
                                                        layer.id,
                                                        it
                                                    )
                                                }
                                        )

                                        Text(
                                            text =
                                                "Audio"
                                        )

                                        Switch(
                                            checked =
                                                layer.audioEnabled,
                                            onCheckedChange =
                                                {
                                                    onExpressionAudioEnabledChanged(
                                                        layer.id,
                                                        it
                                                    )
                                                }
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        onRemoveExpression(
                                            layer.id
                                        )
                                    }
                                ) {
                                    Text(
                                        text =
                                            "Remove"
                                    )
                                }
                            }

                            HorizontalDivider()
                        }
                    }
                }
            }
        }

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
                onExpandedChanged =
                    {
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
        }
    }

    if (
        addDialogVisible
    ) {

        AlertDialog(
            onDismissRequest = {
                addDialogVisible =
                    false
            },
            title = {
                Text(
                    text =
                        "Add equation"
                )
            },
            text = {

                Column {

                    OutlinedTextField(
                        value =
                            newExpression,
                        onValueChange = {
                            newExpression =
                                it
                            addError =
                                null
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        singleLine =
                            true,
                        placeholder = {
                            Text(
                                text =
                                    "Enter equation"
                            )
                        },
                        isError =
                            addError != null
                    )

                    if (
                        addError != null
                    ) {

                        Text(
                            text =
                                addError!!,
                            modifier =
                                Modifier.padding(
                                    top = 8.dp
                                )
                        )
                    }
                }
            },
            confirmButton = {

                Button(
                    onClick = {

                        val expression =
                            newExpression.trim()

                        if (
                            expression.isEmpty()
                        ) {
                            addError =
                                "Enter an equation"
                            return@Button
                        }

                        val success =
                            onAddExpression(
                                expression
                            )

                        if (
                            success
                        ) {
                            addDialogVisible =
                                false
                            newExpression =
                                ""
                        } else {
                            addError =
                                "Equation is invalid"
                        }
                    }
                ) {
                    Text(
                        text =
                            "Add"
                    )
                }
            },
            dismissButton = {

                OutlinedButton(
                    onClick = {
                        addDialogVisible =
                            false
                    }
                ) {
                    Text(
                        text =
                            "Cancel"
                    )
                }
            }
        )
    }
}