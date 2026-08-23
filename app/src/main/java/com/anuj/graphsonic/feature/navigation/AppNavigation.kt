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

            VisualizationScreen(
                graphData = uiState.graphData,
                cursor = cursor,
                listenState = listenState,
                frequencyMode = frequencyMode,
                playbackSpeed = playbackSpeed,
                volume = volume,
                onCursorChanged =
                    viewModel::updateCursor,
                evaluateAt =
                    viewModel::evaluateAt,
                onViewportChanged =
                    viewModel::onViewportChanged,
                onStartListening =
                    viewModel::startListening,
                onStopListening =
                    viewModel::stopListening,
                onFrequencyModeChanged =
                    viewModel::setFrequencyMode,
                onPlaybackSpeedChanged =
                    viewModel::setPlaybackSpeed,
                onVolumeChanged =
                    viewModel::setVolume
            )
        }
    }
}