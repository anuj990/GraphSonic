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
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class VisualizationViewModel : ViewModel() {

    private val nativeBridge =
        NativeBridge()

    private val graphEngine =
        GraphEngine(nativeBridge)

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