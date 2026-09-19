package com.anuj.graphsonic.feature.visualization.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anuj.graphsonic.feature.audio.FrequencyMode
import com.anuj.graphsonic.feature.audio.Waveform

@Composable
fun ListenPanel(
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
    ) {
        ListenControls(
            isPlaying = isPlaying,
            frequencyMode = frequencyMode,
            playbackSpeed = playbackSpeed,
            volume = volume,
            waveform = waveform,
            expanded = expanded,
            onExpandedChanged = onExpandedChanged,
            onStart = onStart,
            onStop = onStop,
            onFrequencyModeChanged = onFrequencyModeChanged,
            onPlaybackSpeedChanged = onPlaybackSpeedChanged,
            onVolumeChanged = onVolumeChanged,
            onWaveformChanged = onWaveformChanged,
            modifier = Modifier.fillMaxWidth()
        )
    }
}