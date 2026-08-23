package com.anuj.graphsonic.feature.visualization

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anuj.graphsonic.domain.model.GraphData
import com.anuj.graphsonic.feature.audio.FrequencyMode
import com.anuj.graphsonic.feature.audio.ListenState
import com.anuj.graphsonic.feature.audio.Waveform
import com.anuj.graphsonic.feature.visualization.components.CursorInfoCard
import com.anuj.graphsonic.feature.visualization.components.GraphCanvas
import com.anuj.graphsonic.feature.visualization.components.ListenControls
import com.anuj.graphsonic.feature.visualization.components.ListenInfoCard
import com.anuj.graphsonic.feature.visualization.components.ListenPanel

@Composable
fun VisualizationScreen(
    graphData: GraphData,
    cursor: GraphCursorState,
    listenState: ListenState,
    onCursorChanged: (GraphCursorState) -> Unit,
    evaluateAt: (Double) -> Double,
    onViewportChanged: (GraphViewport, Float) -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier,
    frequencyMode: FrequencyMode,
    volume: Double,
    onVolumeChanged: (Double) -> Unit,
    playbackSpeed: Double,
    onFrequencyModeChanged: (FrequencyMode) -> Unit,
    waveform: Waveform,
    onWaveformChanged: (Waveform) -> Unit,
    onPlaybackSpeedChanged: (Double) -> Unit,
) {
    var controlsExpanded by
    rememberSaveable {
        mutableStateOf(false)
    }
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        GraphCanvas(
            graphData = graphData,
            cursor = cursor,
            listenState = listenState,
            onCursorChanged = onCursorChanged,
            evaluateAt = evaluateAt,
            onViewportChanged = onViewportChanged,
            modifier = Modifier.fillMaxSize()
        )

        CursorInfoCard(
            cursor = cursor
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            ListenInfoCard(
                listenState = listenState,
                modifier = Modifier.padding(
                    bottom = 8.dp
                )
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
}