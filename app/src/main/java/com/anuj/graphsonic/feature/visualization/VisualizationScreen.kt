package com.anuj.graphsonic.feature.visualization

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    evaluateAt: (Long, Double) -> Double,
    onViewportChanged: (GraphViewport, Float) -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onAddExpression: (String) -> Boolean,
    history: List<String>,
    onExpressionEnabledChanged: (Long, Boolean) -> Unit,
    onExpressionAudioEnabledChanged: (Long, Boolean) -> Unit,
    onRemoveExpression: (Long) -> Unit,
    onEditExpression: (Long, String) -> String?,
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
    var controlsExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    var addDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var addMethodDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var historyDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var newExpression by rememberSaveable {
        mutableStateOf("")
    }

    var addError by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var editDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var editingExpressionId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    var editingExpression by rememberSaveable {
        mutableStateOf("")
    }

    var editError by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        GraphCanvas(
            graphLayers = graphLayers,
            cursor = cursor,
            listenState = listenState,
            onCursorChanged = onCursorChanged,
            evaluateAt = evaluateAt,
            onViewportChanged = onViewportChanged,
            modifier = Modifier.fillMaxSize()
        )

        CursorInfoCard(
            cursor = cursor,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 132.dp)
        )

        EquationOverlay(
            graphLayers = graphLayers,
            onAdd = {
                if (graphLayers.size < 8) {
                    addMethodDialogVisible = true
                }
            },
            onEnabledChanged = onExpressionEnabledChanged,
            onAudioEnabledChanged = onExpressionAudioEnabledChanged,
            onEdit = { layer ->
                editingExpressionId = layer.id
                editingExpression = layer.expression
                editError = null
                editDialogVisible = true
            },
            onRemove = onRemoveExpression,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 12.dp
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 12.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ListenInfoCard(
                listenState = listenState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            ListenPanel(
                isPlaying = listenState.isPlaying,
                frequencyMode = frequencyMode,
                playbackSpeed = playbackSpeed,
                volume = volume,
                waveform = waveform,
                expanded = controlsExpanded,
                onExpandedChanged = {
                    controlsExpanded = it
                },
                onStart = onStartListening,
                onStop = onStopListening,
                onFrequencyModeChanged = onFrequencyModeChanged,
                onPlaybackSpeedChanged = onPlaybackSpeedChanged,
                onVolumeChanged = onVolumeChanged,
                onWaveformChanged = onWaveformChanged
            )
        }
    }

    if (addMethodDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                addMethodDialogVisible = false
            },
            title = {
                Text(
                    text = "Add equation",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            addMethodDialogVisible = false
                            newExpression = ""
                            addError = null
                            addDialogVisible = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text("Enter manually")
                    }

                    OutlinedButton(
                        onClick = {
                            addMethodDialogVisible = false
                            historyDialogVisible = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text("From history")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        addMethodDialogVisible = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (historyDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                historyDialogVisible = false
            },
            title = {
                Text(
                    text = "Choose from history",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                if (history.isEmpty()) {
                    Text(
                        text = "No equation history yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn {
                        items(
                            items = history,
                            key = { it }
                        ) { expression ->
                            TextButton(
                                onClick = {
                                    val success =
                                        onAddExpression(expression)

                                    if (success) {
                                        historyDialogVisible = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = expression,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        historyDialogVisible = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (addDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                addDialogVisible = false
            },
            title = {
                Text(
                    text = "Add equation",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newExpression,
                        onValueChange = {
                            newExpression = it
                            addError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text("Enter equation")
                        },
                        isError = addError != null,
                        supportingText = {
                            addError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val expression = newExpression.trim()

                        if (expression.isEmpty()) {
                            addError = "Enter an equation"
                            return@Button
                        }

                        val success = onAddExpression(expression)

                        if (success) {
                            addDialogVisible = false
                            newExpression = ""
                        } else {
                            addError = "Equation is invalid"
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        addDialogVisible = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (editDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                editDialogVisible = false
            },
            title = {
                Text(
                    text = "Edit equation",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = editingExpression,
                    onValueChange = {
                        editingExpression = it
                        editError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text("Enter equation")
                    },
                    isError = editError != null,
                    supportingText = {
                        editError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = editingExpressionId

                        if (id == null) {
                            editError = "Equation not found"
                            return@Button
                        }

                        val expression = editingExpression.trim()

                        if (expression.isEmpty()) {
                            editError = "Enter an equation"
                            return@Button
                        }

                        val error = onEditExpression(
                            id,
                            expression
                        )

                        if (error == null) {
                            editDialogVisible = false
                            editingExpressionId = null
                            editingExpression = ""
                        } else {
                            editError = error
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        editDialogVisible = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EquationOverlay(
    graphLayers: List<GraphLayer>,
    onAdd: () -> Unit,
    onEnabledChanged: (Long, Boolean) -> Unit,
    onAudioEnabledChanged: (Long, Boolean) -> Unit,
    onEdit: (GraphLayer) -> Unit,
    onRemove: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 10.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Equations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${graphLayers.size}/8",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AssistChip(
                    onClick = onAdd,
                    enabled = graphLayers.size < 8,
                    label = {
                        Text("Add")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                )
            }

            if (graphLayers.isNotEmpty()) {
                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(
                            rememberScrollState()
                        ),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    graphLayers.forEach { layer ->
                        EquationChip(
                            layer = layer,
                            onEnabledChanged = onEnabledChanged,
                            onAudioEnabledChanged = onAudioEnabledChanged,
                            onEdit = onEdit,
                            onRemove = onRemove
                        )
                    }
                }
            } else {
                Text(
                    text = "Add an equation to start graphing.",
                    modifier = Modifier.padding(
                        top = 8.dp,
                        bottom = 2.dp
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EquationChip(
    layer: GraphLayer,
    onEnabledChanged: (Long, Boolean) -> Unit,
    onAudioEnabledChanged: (Long, Boolean) -> Unit,
    onEdit: (GraphLayer) -> Unit,
    onRemove: (Long) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                start = 10.dp,
                end = 4.dp
            )
        ) {
            Surface(
                modifier = Modifier.size(10.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = graphLayerColor(layer.colorIndex)
            ) {}

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = layer.expression,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(
                onClick = {
                    onEnabledChanged(
                        layer.id,
                        !layer.enabled
                    )
                }
            ) {
                Icon(
                    imageVector =
                        if (layer.enabled) {
                            Icons.Default.Visibility
                        } else {
                            Icons.Default.VisibilityOff
                        },
                    contentDescription =
                        if (layer.enabled) {
                            "Hide graph"
                        } else {
                            "Show graph"
                        },
                    tint =
                        if (layer.enabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                )
            }

            IconButton(
                onClick = {
                    onAudioEnabledChanged(
                        layer.id,
                        !layer.audioEnabled
                    )
                }
            ) {
                Icon(
                    imageVector =
                        if (layer.audioEnabled) {
                            Icons.Default.VolumeUp
                        } else {
                            Icons.Default.VolumeOff
                        },
                    contentDescription =
                        if (layer.audioEnabled) {
                            "Mute audio"
                        } else {
                            "Enable audio"
                        },
                    tint =
                        if (layer.audioEnabled) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                )
            }

            IconButton(
                onClick = {
                    onEdit(layer)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit equation"
                )
            }

            IconButton(
                onClick = {
                    onRemove(layer.id)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove equation"
                )
            }
        }
    }
}

private fun graphLayerColor(
    index: Int
): androidx.compose.ui.graphics.Color {
    return when (index % 8) {
        0 -> androidx.compose.ui.graphics.Color(0xFF4F7CFF)
        1 -> androidx.compose.ui.graphics.Color(0xFFFF5C7A)
        2 -> androidx.compose.ui.graphics.Color(0xFF43B581)
        3 -> androidx.compose.ui.graphics.Color(0xFFB26CFF)
        4 -> androidx.compose.ui.graphics.Color(0xFFFFA63D)
        5 -> androidx.compose.ui.graphics.Color(0xFF35C2C9)
        6 -> androidx.compose.ui.graphics.Color(0xFF9A7B62)
        else -> androidx.compose.ui.graphics.Color(0xFFE45B9A)
    }
}