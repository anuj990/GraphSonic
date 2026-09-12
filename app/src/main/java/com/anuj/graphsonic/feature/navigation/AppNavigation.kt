package com.anuj.graphsonic.feature.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.anuj.graphsonic.feature.equation.EquationScreen
import com.anuj.graphsonic.feature.history.HistoryScreen
import com.anuj.graphsonic.feature.visualization.VisualizationScreen
import com.anuj.graphsonic.feature.visualization.VisualizationViewModel
import com.anuj.graphsonic.ui.components.AppBottomBar

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: VisualizationViewModel
) {
    val backStackEntry by
    navController.currentBackStackEntryAsState()

    val currentRoute =
        backStackEntry?.destination?.route

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

    val uiState by
    viewModel.uiState.collectAsState()

    val history by
    viewModel.history.collectAsState()

    val cursor by
    viewModel.cursor.collectAsState()

    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentRoute = currentRoute,
                onDestinationSelected = { destination ->

                    if (currentRoute != destination.route) {
                        val popped =
                            navController.popBackStack(
                                destination.route,
                                false
                            )

                        if (!popped) {
                            navController.navigate(
                                destination.route
                            ) {
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        NavHost(
            navController =
                navController,
            startDestination =
                AppDestination.Equation.route,
            modifier =
                Modifier.padding(
                    innerPadding
                )
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
                            ) {
                                launchSingleTop = true
                            }
                        }

                        success
                    },
                    onHistory = {
                        navController.navigate(
                            AppDestination.History.route
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route =
                    AppDestination.History.route
            ) {

                HistoryScreen(
                    history =
                        history,
                    onEquationSelected = { expression ->

                        val success =
                            viewModel.loadExpression(
                                expression
                            )

                        if (success) {
                            navController.navigate(
                                AppDestination.Visualization.route
                            ) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            composable(
                route =
                    AppDestination.Visualization.route
            ) {

                VisualizationScreen(
                    graphLayers =
                        uiState.graphLayers,
                    cursor =
                        cursor,
                    listenState =
                        listenState,
                    onCursorChanged =
                        viewModel::updateCursor,
                    evaluateAt = { id, x ->
                        viewModel.evaluateAt(
                            id,
                            x
                        )
                    },
                    onViewportChanged =
                        viewModel::onViewportChanged,
                    onStartListening =
                        viewModel::startListening,
                    onStopListening =
                        viewModel::stopListening,
                    onAddExpression =
                        viewModel::addExpression,
                    history =
                        history,
                    onExpressionEnabledChanged =
                        viewModel::setExpressionEnabled,
                    onExpressionAudioEnabledChanged =
                        viewModel::setExpressionAudioEnabled,
                    onRemoveExpression =
                        viewModel::removeExpression,
                    onEditExpression = { id, expression ->
                        viewModel.editExpression(
                            id,
                            expression
                        )
                    },
                    frequencyMode =
                        frequencyMode,
                    volume =
                        volume,
                    onVolumeChanged =
                        viewModel::setVolume,
                    playbackSpeed =
                        playbackSpeed,
                    onFrequencyModeChanged =
                        viewModel::setFrequencyMode,
                    waveform =
                        waveform,
                    onWaveformChanged =
                        viewModel::setWaveform,
                    onPlaybackSpeedChanged =
                        viewModel::setPlaybackSpeed
                )
            }
        }
    }
}