package com.anuj.graphsonic.feature.visualization.components


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
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