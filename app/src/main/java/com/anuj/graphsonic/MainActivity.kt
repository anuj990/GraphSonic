package com.anuj.graphsonic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.anuj.graphsonic.feature.navigation.AppNavigation
import com.anuj.graphsonic.feature.visualization.VisualizationViewModel
import com.anuj.graphsonic.ui.GraphSonicTheme

class MainActivity : ComponentActivity() {

    private val viewModel:
            VisualizationViewModel by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {
            GraphSonicTheme {

                val navController =
                    rememberNavController()

                AppNavigation(
                    navController =
                        navController,
                    viewModel =
                        viewModel
                )
            }
        }
    }
}