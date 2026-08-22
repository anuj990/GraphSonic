package com.anuj.graphsonic.feature.visualization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anuj.graphsonic.domain.model.GraphData
import com.anuj.graphsonic.engine.GraphEngine
import com.anuj.graphsonic.engine.NativeBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VisualizationUiState(
    val graphData: GraphData = GraphData(emptyList()),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class VisualizationViewModel : ViewModel() {

    private val nativeBridge =
        NativeBridge()
    private var samplingGeneration = 0L
    private val graphEngine =
        GraphEngine(nativeBridge)

    private val viewportController =
        GraphViewportController(
            scope = viewModelScope,
            onViewportSettled = ::resampleGraph
        )

    private var expressionHandle =
        0L

    private val _uiState =
        MutableStateFlow(
            VisualizationUiState()
        )

    val uiState: StateFlow<VisualizationUiState> =
        _uiState.asStateFlow()

    private val _cursor =
        MutableStateFlow(
            GraphCursorState()
        )

    val cursor: StateFlow<GraphCursorState> =
        _cursor.asStateFlow()

    fun loadExpression(
        expression: String
    ): Boolean {
        samplingGeneration += 1L
        return try {

            val newHandle =
                nativeBridge.createExpression(
                    expression
                )

            if (newHandle == 0L) {

                _uiState.value =
                    _uiState.value.copy(
                        errorMessage =
                            "Equation is invalid",
                        isLoading = false
                    )

                false

            } else {

                val graph =
                    graphEngine.generateGraph(
                        expressionHandle =
                            newHandle,
                        xMin = -10.0,
                        xMax = 10.0,
                        sampleCount = 2000
                    )

                if (expressionHandle != 0L) {
                    nativeBridge.destroyExpression(
                        expressionHandle
                    )
                }

                expressionHandle =
                    newHandle

                _uiState.value =
                    VisualizationUiState(
                        graphData = graph,
                        isLoading = false,
                        errorMessage = null
                    )

                _cursor.value =
                    GraphCursorState()

                true
            }

        } catch (
            exception: IllegalArgumentException
        ) {

            _uiState.value =
                _uiState.value.copy(
                    isLoading = false,
                    errorMessage =
                        "Equation is invalid"
                )

            false

        } catch (
            exception: Exception
        ) {

            _uiState.value =
                _uiState.value.copy(
                    isLoading = false,
                    errorMessage =
                        "Equation is invalid"
                )

            false
        }
    }

    fun clearError() {
        if (
            _uiState.value.errorMessage != null
        ) {
            _uiState.value =
                _uiState.value.copy(
                    errorMessage = null
                )
        }
    }

    fun evaluateAt(
        x: Double
    ): Double {
        if (expressionHandle == 0L) {
            return Double.NaN
        }

        return graphEngine.evaluate(
            expressionHandle = expressionHandle,
            x = x
        )
    }

    fun updateCursor(
        cursor: GraphCursorState
    ) {
        _cursor.value = cursor
    }

    fun onViewportChanged(
        viewport: GraphViewport,
        screenWidth: Float
    ) {
        viewportController.onViewportChanged(
            viewport = viewport,
            screenWidth = screenWidth
        )
    }

    private fun resampleGraph(
        viewport: GraphViewport,
        screenWidth: Float
    ) {
        if (expressionHandle == 0L) {
            return
        }

        val handle = expressionHandle

        samplingGeneration += 1L

        val generation =
            samplingGeneration

        val halfWidth =
            screenWidth.toDouble() /
                    (
                            2.0 *
                                    viewport.scale.toDouble()
                            )

        val xMin =
            viewport.centerX.toDouble() -
                    halfWidth

        val xMax =
            viewport.centerX.toDouble() +
                    halfWidth

        val sampleCount =
            ((xMax - xMin) * 100.0)
                .toInt()
                .coerceIn(
                    1000,
                    10000
                )

        _uiState.value =
            _uiState.value.copy(
                isLoading = true
            )

        viewModelScope.launch(
            Dispatchers.Default
        ) {
            val graph =
                graphEngine.generateGraph(
                    expressionHandle = handle,
                    xMin = xMin,
                    xMax = xMax,
                    sampleCount = sampleCount
                )

            if (
                generation ==
                samplingGeneration &&
                handle ==
                expressionHandle
            ) {
                _uiState.value =
                    _uiState.value.copy(
                        graphData = graph,
                        isLoading = false
                    )
            }
        }
    }

    override fun onCleared() {

        samplingGeneration += 1L

        viewportController.clear()

        if (expressionHandle != 0L) {
            nativeBridge.destroyExpression(
                expressionHandle
            )

            expressionHandle = 0L
        }

        super.onCleared()
    }
}