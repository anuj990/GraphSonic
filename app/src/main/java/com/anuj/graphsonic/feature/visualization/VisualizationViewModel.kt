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
    val graphLayers: List<GraphLayer> = emptyList(),
    val graphData: GraphData = GraphData(emptyList()),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isListening: Boolean = false
)

class VisualizationViewModel : ViewModel() {

    private val nativeBridge =
        NativeBridge()

    private val graphEngine =
        GraphEngine(nativeBridge)

    private val listenController =
        ListenController(
            scope = viewModelScope,
            evaluateAt = ::evaluateAt
        )

    private val viewportController =
        GraphViewportController(
            scope = viewModelScope,
            onViewportSettled = ::resampleGraphs
        )

    private val _uiState =
        MutableStateFlow(
            VisualizationUiState()
        )

    val uiState:
            StateFlow<VisualizationUiState> =
        _uiState.asStateFlow()

    private val _cursor =
        MutableStateFlow(
            GraphCursorState()
        )

    val cursor:
            StateFlow<GraphCursorState> =
        _cursor.asStateFlow()

    private val _frequencyMode =
        MutableStateFlow(
            FrequencyMode.Continuous
        )

    val frequencyMode:
            StateFlow<FrequencyMode> =
        _frequencyMode.asStateFlow()

    private val _waveform =
        MutableStateFlow(
            Waveform.Sine
        )

    val waveform:
            StateFlow<Waveform> =
        _waveform.asStateFlow()

    private val _playbackSpeed =
        MutableStateFlow(
            1.0
        )

    val playbackSpeed:
            StateFlow<Double> =
        _playbackSpeed.asStateFlow()

    private val _volume =
        MutableStateFlow(
            0.15
        )

    val volume:
            StateFlow<Double> =
        _volume.asStateFlow()

    val listenState =
        listenController.state

    private var nextLayerId =
        1L

    private val expressionHandles =
        LinkedHashMap<Long, Long>()

    private var samplingGeneration =
        0L

    fun loadExpression(
        expression: String
    ): Boolean {

        val trimmed =
            expression.trim()

        if (trimmed.isEmpty()) {
            _uiState.value =
                _uiState.value.copy(
                    errorMessage =
                        "Enter an equation"
                )

            return false
        }

        return try {

            val handle =
                nativeBridge.createExpression(
                    trimmed
                )

            if (handle == 0L) {

                _uiState.value =
                    _uiState.value.copy(
                        errorMessage =
                            "Equation is invalid"
                    )

                false

            } else {

                val id =
                    nextLayerId++

                val graph =
                    graphEngine.generateGraph(
                        expressionHandle =
                            handle,
                        xMin = -10.0,
                        xMax = 10.0,
                        sampleCount = 2000
                    )

                expressionHandles[id] =
                    handle

                listenController.setGraphData(
                    id = id,
                    graphData = graph
                )

                val layer =
                    GraphLayer(
                        id = id,
                        expression = trimmed,
                        graphData = graph,
                        enabled = true
                    )

                _uiState.value =
                    _uiState.value.copy(
                        graphLayers =
                            _uiState.value.graphLayers +
                                    layer,
                        graphData =
                            graph,
                        isLoading = false,
                        errorMessage = null
                    )

                true
            }

        } catch (
            exception: Exception
        ) {

            _uiState.value =
                _uiState.value.copy(
                    errorMessage =
                        "Equation is invalid",
                    isLoading = false
                )

            false
        }
    }

    fun replaceExpressions(
        expressions: List<String>
    ): Boolean {

        val cleaned =
            expressions
                .map {
                    it.trim()
                }
                .filter {
                    it.isNotEmpty()
                }

        if (cleaned.isEmpty()) {

            _uiState.value =
                _uiState.value.copy(
                    errorMessage =
                        "Enter at least one equation"
                )

            return false
        }

        if (cleaned.size > 16) {

            _uiState.value =
                _uiState.value.copy(
                    errorMessage =
                        "Maximum 16 equations"
                )

            return false
        }

        val newHandles =
            LinkedHashMap<Long, Long>()

        val newLayers =
            mutableListOf<GraphLayer>()

        return try {

            for (expression in cleaned) {

                val handle =
                    nativeBridge.createExpression(
                        expression
                    )

                if (handle == 0L) {
                    throw IllegalArgumentException(
                        "Invalid equation"
                    )
                }

                val id =
                    nextLayerId++

                newHandles[id] =
                    handle

                val graph =
                    graphEngine.generateGraph(
                        expressionHandle =
                            handle,
                        xMin = -10.0,
                        xMax = 10.0,
                        sampleCount = 2000
                    )

                newLayers +=
                    GraphLayer(
                        id = id,
                        expression = expression,
                        graphData = graph,
                        enabled = true
                    )
            }

            clearExpressionsInternal()

            expressionHandles.putAll(
                newHandles
            )

            newLayers.forEach { layer ->
                listenController.setGraphData(
                    id = layer.id,
                    graphData = layer.graphData
                )
            }

            _uiState.value =
                VisualizationUiState(
                    graphLayers =
                        newLayers,
                    graphData =
                        newLayers.firstOrNull()
                            ?.graphData
                            ?: GraphData(emptyList()),
                    isLoading = false,
                    errorMessage = null,
                    isListening = false
                )

            _cursor.value =
                GraphCursorState()

            true

        } catch (
            exception: Exception
        ) {

            newHandles.values.forEach { handle ->
                nativeBridge.destroyExpression(
                    handle
                )
            }

            _uiState.value =
                _uiState.value.copy(
                    errorMessage =
                        "One or more equations are invalid",
                    isLoading = false
                )

            false
        }
    }

    private fun clearExpressionsInternal() {

        samplingGeneration += 1L

        listenController.reset()

        expressionHandles.values.forEach { handle ->
            nativeBridge.destroyExpression(
                handle
            )
        }

        expressionHandles.clear()
    }

    fun clearExpressions() {

        clearExpressionsInternal()

        _uiState.value =
            VisualizationUiState()

        _cursor.value =
            GraphCursorState()
    }

    fun removeExpression(
        id: Long
    ) {

        val handle =
            expressionHandles.remove(
                id
            )

        if (handle != null) {
            nativeBridge.destroyExpression(
                handle
            )
        }

        listenController.removeGraphData(
            id
        )

        val remainingLayers =
            _uiState.value.graphLayers.filter {
                it.id != id
            }

        _uiState.value =
            _uiState.value.copy(
                graphLayers =
                    remainingLayers,
                graphData =
                    remainingLayers
                        .firstOrNull()
                        ?.graphData
                        ?: GraphData(emptyList())
            )
    }

    fun setExpressionEnabled(
        id: Long,
        enabled: Boolean
    ) {

        _uiState.value =
            _uiState.value.copy(
                graphLayers =
                    _uiState.value.graphLayers.map {
                        if (it.id == id) {
                            it.copy(
                                enabled = enabled
                            )
                        } else {
                            it
                        }
                    }
            )

        listenController.setEnabled(
            id = id,
            enabled = enabled
        )
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
        id: Long,
        x: Double
    ): Double {

        val handle =
            expressionHandles[id]
                ?: return Double.NaN

        return graphEngine.evaluate(
            expressionHandle =
                handle,
            x = x
        )
    }

    fun evaluateAt(
        x: Double
    ): Double {

        val firstLayer =
            _uiState.value.graphLayers
                .firstOrNull()
                ?: return Double.NaN

        return evaluateAt(
            firstLayer.id,
            x
        )
    }

    fun updateCursor(
        cursor: GraphCursorState
    ) {
        _cursor.value =
            cursor
    }

    fun setFrequencyMode(
        mode: FrequencyMode
    ) {

        _frequencyMode.value =
            mode

        listenController.setFrequencyMode(
            mode
        )
    }

    fun setWaveform(
        waveform: Waveform
    ) {

        _waveform.value =
            waveform

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

    fun onViewportChanged(
        viewport: GraphViewport,
        screenWidth: Float
    ) {

        updateListenRange(
            viewport,
            screenWidth
        )

        viewportController.onViewportChanged(
            viewport = viewport,
            screenWidth = screenWidth
        )
    }

    private fun updateListenRange(
        viewport: GraphViewport,
        screenWidth: Float
    ) {

        if (
            !screenWidth.isFinite() ||
            screenWidth <= 0f
        ) {
            return
        }

        if (
            !viewport.scale.isFinite() ||
            viewport.scale <= 0.0
        ) {
            return
        }

        val halfWidth =
            screenWidth.toDouble() /
                    (
                            2.0 *
                                    viewport.scale
                            )

        val xMin =
            viewport.centerX -
                    halfWidth

        val xMax =
            viewport.centerX +
                    halfWidth

        if (
            !xMin.isFinite() ||
            !xMax.isFinite() ||
            xMax <= xMin
        ) {
            return
        }

        listenController.setRange(
            start = xMin,
            end = xMax
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

    fun startListening() {

        if (
            expressionHandles.isEmpty()
        ) {
            return
        }

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

    private fun resampleGraphs(
        viewport: GraphViewport,
        screenWidth: Float
    ) {

        if (
            expressionHandles.isEmpty()
        ) {
            return
        }

        if (
            !screenWidth.isFinite() ||
            screenWidth <= 0f
        ) {
            return
        }

        if (
            !viewport.scale.isFinite() ||
            viewport.scale <= 0.0
        ) {
            return
        }

        val halfWidth =
            screenWidth.toDouble() /
                    (
                            2.0 *
                                    viewport.scale
                            )

        val xMin =
            viewport.centerX -
                    halfWidth

        val xMax =
            viewport.centerX +
                    halfWidth

        if (
            !xMin.isFinite() ||
            !xMax.isFinite() ||
            xMax <= xMin
        ) {
            return
        }

        val sampleCount =
            (
                    (xMax - xMin) *
                            100.0
                    )
                .toInt()
                .coerceIn(
                    1000,
                    10000
                )

        samplingGeneration += 1L

        val generation =
            samplingGeneration

        _uiState.value =
            _uiState.value.copy(
                isLoading = true
            )

        val handles =
            expressionHandles.toMap()

        viewModelScope.launch(
            Dispatchers.Default
        ) {

            val currentLayers =
                _uiState.value.graphLayers

            val updatedLayers =
                currentLayers.map { layer ->

                    val handle =
                        handles[layer.id]

                    if (handle == null) {
                        layer
                    } else {

                        val graph =
                            graphEngine.generateGraph(
                                expressionHandle =
                                    handle,
                                xMin = xMin,
                                xMax = xMax,
                                sampleCount =
                                    sampleCount
                            )

                        listenController.setGraphData(
                            id = layer.id,
                            graphData = graph
                        )

                        layer.copy(
                            graphData = graph
                        )
                    }
                }

            if (
                generation ==
                samplingGeneration
            ) {

                _uiState.value =
                    _uiState.value.copy(
                        graphLayers =
                            updatedLayers,
                        graphData =
                            updatedLayers
                                .firstOrNull()
                                ?.graphData
                                ?: GraphData(emptyList()),
                        isLoading = false
                    )
            }
        }
    }

    override fun onCleared() {

        samplingGeneration += 1L

        viewportController.clear()

        listenController.release()

        expressionHandles.values.forEach { handle ->
            nativeBridge.destroyExpression(
                handle
            )
        }

        expressionHandles.clear()

        super.onCleared()
    }
}