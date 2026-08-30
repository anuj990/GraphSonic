package com.anuj.graphsonic.feature.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.anuj.graphsonic.feature.equation.EquationScreen
import com.anuj.graphsonic.feature.visualization.VisualizationScreen
import com.anuj.graphsonic.feature.visualization.VisualizationViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: VisualizationViewModel
) {
    val volume by
    viewModel.volume.collectAsState()
    val frequencyMode by
    viewModel.frequencyMode.collectAsState()

    val playbackSpeed by
    viewModel.playbackSpeed.collectAsState()

    val listenState by
    viewModel.listenState.collectAsState()

    val waveform by
    viewModel.waveform.collectAsState()

    NavHost(
        navController = navController,
        startDestination =
            AppDestination.Equation.route
    ) {

        composable(
            route =
                AppDestination.Equation.route
        ) {
            EquationScreen(
                onGraph = { equation ->

                    val success =
                        viewModel.loadExpression(
                            equation
                        )

                    if (success) {
                        navController.navigate(
                            AppDestination.Visualization.route
                        )
                    }

                    success
                }
            )
        }

        composable(
            route =
                AppDestination.Visualization.route
        ) {
            val uiState by
            viewModel.uiState.collectAsState()

            val cursor by
            viewModel.cursor.collectAsState()
            val waveform by
            viewModel.waveform.collectAsState()

            VisualizationScreen(
                graphLayers = uiState.graphLayers,
                cursor = cursor,
                listenState = listenState,
                onCursorChanged = viewModel::updateCursor,
                evaluateAt = viewModel::evaluateAt,
                onViewportChanged =
                    viewModel::onViewportChanged,
                onStartListening =
                    viewModel::startListening,
                onStopListening =
                    viewModel::stopListening,
                frequencyMode = frequencyMode,
                volume = viewModel.volume.collectAsState().value,
                onVolumeChanged =
                    viewModel::setVolume,
                playbackSpeed = playbackSpeed,
                onFrequencyModeChanged =
                    viewModel::setFrequencyMode,
                waveform =
                    viewModel.waveform.collectAsState().value,
                onWaveformChanged =
                    viewModel::setWaveform,
                onPlaybackSpeedChanged =
                    viewModel::setPlaybackSpeed
            )
        }
    }
}