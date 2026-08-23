package com.anuj.graphsonic.feature.visualization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anuj.graphsonic.domain.model.GraphData
import com.anuj.graphsonic.engine.GraphEngine
import com.anuj.graphsonic.engine.NativeBridge
import com.anuj.graphsonic.feature.audio.FrequencyMode
import com.anuj.graphsonic.feature.audio.ListenController
import com.anuj.graphsonic.feature.audio.Waveform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VisualizationUiState(
    val graphData: GraphData = GraphData(emptyList()),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isListening: Boolean = false
)
class VisualizationViewModel : ViewModel() {
    private val listenController =
        ListenController(
            scope = viewModelScope,
            evaluateAt = ::evaluateAt
        )
    private val _frequencyMode =
        MutableStateFlow(
            FrequencyMode.Continuous
        )
    private val _volume =
        MutableStateFlow(0.15)

    val volume: StateFlow<Double> =
        _volume.asStateFlow()
    val frequencyMode:
            StateFlow<FrequencyMode> =
        _frequencyMode.asStateFlow()

    private val _playbackSpeed =
        MutableStateFlow(1.0)

    val playbackSpeed:
            StateFlow<Double> =
        _playbackSpeed.asStateFlow()
    val listenState =
        listenController.state
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
    private val _waveform =
        MutableStateFlow(
            Waveform.Sine
        )

    val waveform: StateFlow<Waveform> =
        _waveform.asStateFlow()
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
    fun setFrequencyMode(
        mode: FrequencyMode
    ) {
        _frequencyMode.value = mode

        listenController.setFrequencyMode(
            mode
        )
    }
    fun setWaveform(
        waveform: Waveform
    ) {
        _waveform.value = waveform

        listenController.setWaveform(
            waveform
        )
    }
    fun setPlaybackSpeed(
        speed: Double
    ) {
        val safeSpeed =
            speed.coerceIn(
                0.25,
                4.0
            )

        _playbackSpeed.value =
            safeSpeed

        listenController.setPlaybackSpeed(
            safeSpeed
        )
    }
    fun setVolume(
        value: Double
    ) {
        val safeVolume =
            value.coerceIn(
                0.0,
                1.0
            )

        _volume.value =
            safeVolume

        listenController.setVolume(
            safeVolume
        )
    }
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
                listenController.reset()
                _uiState.value =
                    VisualizationUiState(
                        graphData = graph,
                        isLoading = false,
                        errorMessage = null,
                        isListening = false
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
        val halfWidth =
            screenWidth.toDouble() /
                    (
                            2.0 *
                                    viewport.scale.toDouble()
                            )

        val startX =
            viewport.centerX.toDouble() -
                    halfWidth

        val endX =
            viewport.centerX.toDouble() +
                    halfWidth

        listenController.setRange(
            start = startX,
            end = endX
        )

        viewportController.onViewportChanged(
            viewport = viewport,
            screenWidth = screenWidth
        )
    }
    fun startListening() {
        listenController.start()

        _uiState.value =
            _uiState.value.copy(
                isListening = true
            )
    }

    fun stopListening() {
        listenController.stop()

        _uiState.value =
            _uiState.value.copy(
                isListening = false
            )
    }
    fun updateListenRange(
        startX: Double,
        endX: Double
    ) {
        listenController.setRange(
            start = startX,
            end = endX
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

        listenController.release()

        if (expressionHandle != 0L) {
            nativeBridge.destroyExpression(
                expressionHandle
            )

            expressionHandle = 0L
        }

        super.onCleared()
    }

}