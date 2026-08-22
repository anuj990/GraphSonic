package com.anuj.graphsonic.feature.visualization


import androidx.lifecycle.ViewModel
import com.anuj.graphsonic.domain.model.GraphData
import com.anuj.graphsonic.engine.GraphEngine
import com.anuj.graphsonic.engine.NativeBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VisualizationUiState(
    val graphData: GraphData = GraphData(emptyList()),
    val isLoading: Boolean = false
)

class VisualizationViewModel : ViewModel() {

    private val nativeBridge = NativeBridge()

    private val graphEngine =
        GraphEngine(nativeBridge)

    private var expressionHandle: Long = 0L

    private val _uiState =
        MutableStateFlow(
            VisualizationUiState()
        )

    val uiState: StateFlow<VisualizationUiState> =
        _uiState.asStateFlow()

    fun loadExpression(expression: String) {

        if (expressionHandle != 0L) {
            nativeBridge.destroyExpression(
                expressionHandle
            )
        }

        expressionHandle =
            nativeBridge.createExpression(expression)

        val graph =
            graphEngine.generateGraph(
                expressionHandle = expressionHandle,
                xMin = -10.0,
                xMax = 10.0,
                sampleCount = 2000
            )

        _uiState.value =
            VisualizationUiState(
                graphData = graph
            )
    }

    override fun onCleared() {
        if (expressionHandle != 0L) {
            nativeBridge.destroyExpression(
                expressionHandle
            )

            expressionHandle = 0L
        }

        super.onCleared()
    }
}