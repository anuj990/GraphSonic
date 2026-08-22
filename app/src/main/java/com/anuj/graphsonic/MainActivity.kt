package com.anuj.graphsonic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.anuj.graphsonic.feature.visualization.VisualizationScreen
import com.anuj.graphsonic.feature.visualization.VisualizationViewModel
import com.anuj.graphsonic.ui.GraphSonicTheme

class MainActivity : ComponentActivity() {

    private val viewModel: VisualizationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            GraphSonicTheme {

                val uiState by
                viewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.loadExpression(
                        "1/x"
                    )
                }

                VisualizationScreen(
                    graphData = uiState.graphData
                )
            }
        }
    }
}