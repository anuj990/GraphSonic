package com.anuj.graphsonic.feature.visualization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
    var controlsExpanded by rememberSaveable { mutableStateOf(false) }
    var addDialogVisible by rememberSaveable { mutableStateOf(false) }
    var addMethodDialogVisible by rememberSaveable { mutableStateOf(false) }
    var historyDialogVisible by rememberSaveable { mutableStateOf(false) }
    var newExpression by rememberSaveable { mutableStateOf("") }
    var addError by rememberSaveable { mutableStateOf<String?>(null) }
    var editDialogVisible by rememberSaveable { mutableStateOf(false) }
    var editingExpressionId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editingExpression by rememberSaveable { mutableStateOf("") }
    var editError by rememberSaveable { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        GraphCanvas(
            graphLayers = graphLayers,
            cursor = cursor,
            listenState = listenState,
            onCursorChanged = onCursorChanged,
            evaluateAt = evaluateAt,
            onViewportChanged = onViewportChanged,
            modifier = Modifier.fillMaxSize()
        )

        EquationFloatingDock(
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
                .padding(top = 16.dp)
        )

        CursorInfoCard(
            cursor = cursor,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 120.dp)
                .animateContentSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ListenInfoCard(
                listenState = listenState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            ListenPanel(
                isPlaying = listenState.isPlaying,
                frequencyMode = frequencyMode,
                playbackSpeed = playbackSpeed,
                volume = volume,
                waveform = waveform,
                expanded = controlsExpanded,
                onExpandedChanged = { controlsExpanded = it },
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
            onDismissRequest = { addMethodDialogVisible = false },
            title = {
                Text(
                    text = "Add equation",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.animateContentSize()
                ) {
                    Button(
                        onClick = {
                            addMethodDialogVisible = false
                            newExpression = ""
                            addError = null
                            addDialogVisible = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Enter manually", fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            addMethodDialogVisible = false
                            historyDialogVisible = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Choose from history", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { addMethodDialogVisible = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (historyDialogVisible) {
        AlertDialog(
            onDismissRequest = { historyDialogVisible = false },
            title = {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No history available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.animateContentSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = history, key = { it }) { expression ->
                            Card(
                                onClick = {
                                    if (onAddExpression(expression)) {
                                        historyDialogVisible = false
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                            ) {
                                Text(
                                    text = expression,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { historyDialogVisible = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (addDialogVisible) {
        AlertDialog(
            onDismissRequest = { addDialogVisible = false },
            title = {
                Text(
                    text = "New equation",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.animateContentSize()) {
                    OutlinedTextField(
                        value = newExpression,
                        onValueChange = {
                            newExpression = it
                            addError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("e.g. sin(x)") },
                        isError = addError != null,
                        shape = RoundedCornerShape(16.dp)
                    )

                    AnimatedVisibility(
                        visible = addError != null,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Text(
                            text = addError.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp, start = 16.dp)
                        )
                    }
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
                        if (onAddExpression(expression)) {
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
                TextButton(onClick = { addDialogVisible = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (editDialogVisible) {
        AlertDialog(
            onDismissRequest = { editDialogVisible = false },
            title = {
                Text(
                    text = "Edit equation",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.animateContentSize()) {
                    OutlinedTextField(
                        value = editingExpression,
                        onValueChange = {
                            editingExpression = it
                            editError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        isError = editError != null
                    )

                    AnimatedVisibility(
                        visible = editError != null,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Text(
                            text = editError.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp, start = 16.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = editingExpressionId ?: return@Button
                        val expression = editingExpression.trim()

                        if (expression.isEmpty()) {
                            editError = "Enter an equation"
                            return@Button
                        }

                        val error = onEditExpression(id, expression)
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
                TextButton(onClick = { editDialogVisible = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
private fun EquationFloatingDock(
    graphLayers: List<GraphLayer>,
    onAdd: () -> Unit,
    onEnabledChanged: (Long, Boolean) -> Unit,
    onAudioEnabledChanged: (Long, Boolean) -> Unit,
    onEdit: (GraphLayer) -> Unit,
    onRemove: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .animateContentSize(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        LazyRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            item {
                FilledIconButton(
                    onClick = onAdd,
                    enabled = graphLayers.size < 8,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add equation")
                }
            }

            items(items = graphLayers, key = { it.id }) { layer ->
                EquationChip(
                    layer = layer,
                    onEnabledChanged = onEnabledChanged,
                    onAudioEnabledChanged = onAudioEnabledChanged,
                    onEdit = onEdit,
                    onRemove = onRemove,
                    modifier = Modifier.animateItem()
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
    onRemove: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.height(56.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(graphLayerColor(layer.colorIndex))
            )

            Text(
                text = layer.expression,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            AnimatedTactileIcon(
                active = layer.enabled,
                activeIcon = Icons.Default.Visibility,
                inactiveIcon = Icons.Default.VisibilityOff,
                activeColor = MaterialTheme.colorScheme.primary,
                inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = { onEnabledChanged(layer.id, !layer.enabled) }
            )

            AnimatedTactileIcon(
                active = layer.audioEnabled,
                activeIcon = Icons.Default.VolumeUp,
                inactiveIcon = Icons.Default.VolumeOff,
                activeColor = MaterialTheme.colorScheme.secondary,
                inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = { onAudioEnabledChanged(layer.id, !layer.audioEnabled) }
            )

            AnimatedTactileIcon(
                active = false,
                activeIcon = Icons.Default.Edit,
                inactiveIcon = Icons.Default.Edit,
                activeColor = MaterialTheme.colorScheme.onSurfaceVariant,
                inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = { onEdit(layer) }
            )

            AnimatedTactileIcon(
                active = false,
                activeIcon = Icons.Default.DeleteOutline,
                inactiveIcon = Icons.Default.DeleteOutline,
                activeColor = MaterialTheme.colorScheme.error,
                inactiveColor = MaterialTheme.colorScheme.error,
                onClick = { onRemove(layer.id) }
            )
        }
    }
}

@Composable
private fun AnimatedTactileIcon(
    active: Boolean,
    activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )

    val tint by animateColorAsState(
        targetValue = if (active) activeColor else inactiveColor,
        label = "iconColor"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = Color.Transparent,
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (active) activeIcon else inactiveIcon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun graphLayerColor(index: Int): Color {
    return when (index % 8) {
        0 -> Color(0xFF4F7CFF)
        1 -> Color(0xFFFF5C7A)
        2 -> Color(0xFF43B581)
        3 -> Color(0xFFB26CFF)
        4 -> Color(0xFFFFA63D)
        5 -> Color(0xFF35C2C9)
        6 -> Color(0xFF9A7B62)
        else -> Color(0xFFE45B9A)
    }
}