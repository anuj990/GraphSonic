package com.anuj.graphsonic.feature.visualization

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anuj.graphsonic.data.history.EquationHistoryRepository
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VisualizationUiState(
    val graphLayers: List<GraphLayer> = emptyList(),
    val graphData: GraphData = GraphData(emptyList()),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isListening: Boolean = false
)

class VisualizationViewModel(
    application: android.app.Application
) : AndroidViewModel(application) {

    companion object {
        private const val MAX_EQUATIONS = 8

        private const val INITIAL_SAMPLE_COUNT = 3000

        private const val SAMPLES_PER_PIXEL = 3.0

        private const val MIN_VIEWPORT_SAMPLES = 1000

        private const val MAX_VIEWPORT_SAMPLES = 20000
    }

    private val nativeBridge =
        NativeBridge()

    private val historyRepository =
        EquationHistoryRepository(
            application
        )

    private val _history =
        MutableStateFlow(
            historyRepository.getHistory()
        )

    val history:
            StateFlow<List<String>> =
        _history.asStateFlow()

    private val graphEngine =
        GraphEngine(
            nativeBridge
        )

    private val listenController =
        ListenController(
            scope = viewModelScope,
            evaluateAt = ::evaluateAt
        )

    private val viewportController =
        GraphViewportController(
            scope = viewModelScope,
            onViewportSettled =
                ::resampleGraphs
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

        val success =
            replaceExpressions(
                listOf(expression)
            )

        if (success) {
            historyRepository.addEquation(
                expression
            )

            _history.value =
                historyRepository.getHistory()
        }

        return success
    }

    fun addExpression(
        expression: String
    ): Boolean {

        val cleaned =
            expression.trim()

        if (cleaned.isEmpty()) {
            _uiState.update {
                it.copy(
                    errorMessage =
                        "Enter an equation"
                )
            }

            return false
        }

        if (
            _uiState.value.graphLayers.size >=
            MAX_EQUATIONS
        ) {
            _uiState.update {
                it.copy(
                    errorMessage =
                        "Maximum 8 equations"
                )
            }

            return false
        }

        return try {

            val handle =
                nativeBridge.createExpression(
                    cleaned
                )

            if (
                handle == 0L
            ) {
                throw IllegalArgumentException()
            }

            val canonicalExpression =
                nativeBridge.getCanonicalExpression(
                    handle
                )

            if (
                _uiState.value.graphLayers.any {
                    it.canonicalExpression ==
                            canonicalExpression
                }
            ) {

                nativeBridge.destroyExpression(
                    handle
                )

                _uiState.update {
                    it.copy(
                        errorMessage =
                            "Equation already added"
                    )
                }

                return false
            }

            val id =
                nextLayerId++

            val graph =
                graphEngine.generateGraph(
                    expressionHandle =
                        handle,
                    xMin = -10.0,
                    xMax = 10.0,
                    sampleCount =
                        INITIAL_SAMPLE_COUNT
                )

            val colorIndex =
                _uiState.value.graphLayers.size % 8

            val layer =
                GraphLayer(
                    id = id,
                    expression = cleaned,
                    canonicalExpression =
                        canonicalExpression,
                    graphData = graph,
                    enabled = true,
                    audioEnabled = true,
                    colorIndex = colorIndex
                )

            expressionHandles[id] =
                handle

            listenController.setGraphData(
                id = id,
                graphData = graph
            )

            listenController.setExpression(
                id = id,
                expression = cleaned
            )

            _uiState.update {
                val layers =
                    it.graphLayers + layer

                it.copy(
                    graphLayers =
                        layers,
                    graphData =
                        layers
                            .firstOrNull()
                            ?.graphData
                            ?: GraphData(
                                emptyList()
                            ),
                    errorMessage = null
                )
            }

            historyRepository.addEquation(
                cleaned
            )

            _history.value =
                historyRepository.getHistory()

            true

        } catch (
            exception: Exception
        ) {

            _uiState.update {
                it.copy(
                    errorMessage =
                        "Equation is invalid"
                )
            }

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

        if (
            cleaned.isEmpty()
        ) {
            _uiState.update {
                it.copy(
                    errorMessage =
                        "Enter at least one equation"
                )
            }

            return false
        }

        if (
            cleaned.size > MAX_EQUATIONS
        ) {
            _uiState.update {
                it.copy(
                    errorMessage =
                        "Maximum 8 equations"
                )
            }

            return false
        }

        val newHandles =
            LinkedHashMap<Long, Long>()

        val newLayers =
            mutableListOf<GraphLayer>()

        return try {

            cleaned.forEachIndexed {
                    index,
                    expression ->

                val handle =
                    nativeBridge.createExpression(
                        expression
                    )

                if (
                    handle == 0L
                ) {
                    throw IllegalArgumentException()
                }

                val id =
                    nextLayerId++

                val graph =
                    graphEngine.generateGraph(
                        expressionHandle =
                            handle,
                        xMin = -10.0,
                        xMax = 10.0,
                        sampleCount =
                            INITIAL_SAMPLE_COUNT
                    )

                newHandles[id] =
                    handle

                val canonicalExpression =
                    nativeBridge.getCanonicalExpression(
                        handle
                    )

                newLayers +=
                    GraphLayer(
                        id = id,
                        expression =
                            expression,
                        canonicalExpression =
                            canonicalExpression,
                        graphData =
                            graph,
                        enabled = true,
                        audioEnabled = true,
                        colorIndex =
                            index % 8
                    )
            }

            clearExpressionsInternal()

            expressionHandles.putAll(
                newHandles
            )

            newLayers.forEach {
                    layer ->

                listenController.setGraphData(
                    id =
                        layer.id,
                    graphData =
                        layer.graphData
                )

                listenController.setExpression(
                    id =
                        layer.id,
                    expression =
                        layer.expression
                )
            }

            _uiState.value =
                VisualizationUiState(
                    graphLayers =
                        newLayers,
                    graphData =
                        newLayers
                            .firstOrNull()
                            ?.graphData
                            ?: GraphData(
                                emptyList()
                            ),
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

            newHandles.values.forEach {
                    handle ->

                nativeBridge.destroyExpression(
                    handle
                )
            }

            _uiState.update {
                it.copy(
                    errorMessage =
                        "One or more equations are invalid",
                    isLoading = false
                )
            }

            false
        }
    }

    private fun clearExpressionsInternal() {

        samplingGeneration +=
            1L

        listenController.reset()

        expressionHandles.values.forEach {
                handle ->

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

        if (
            handle != null
        ) {
            nativeBridge.destroyExpression(
                handle
            )
        }

        listenController.removeGraphData(
            id
        )

        val remaining =
            _uiState.value.graphLayers
                .filter {
                    it.id != id
                }
                .mapIndexed {
                        index,
                        layer ->

                    layer.copy(
                        colorIndex =
                            index % 8
                    )
                }

        _uiState.update {
            it.copy(
                graphLayers =
                    remaining,
                graphData =
                    remaining
                        .firstOrNull()
                        ?.graphData
                        ?: GraphData(
                            emptyList()
                        )
            )
        }
    }

    fun editExpression(
        id: Long,
        expression: String
    ): String? {

        val cleaned =
            expression.trim()

        if (cleaned.isEmpty()) {

            _uiState.update {
                it.copy(
                    errorMessage =
                        "Enter an equation"
                )
            }

            return "Enter an equation"
        }

        val oldHandle =
            expressionHandles[id]
                ?: return "Equation not found"

        _uiState.value.graphLayers
            .firstOrNull {
                it.id == id
            }
            ?: return "Equation not found"

        val newHandle =
            try {

                nativeBridge.createExpression(
                    cleaned
                )

            } catch (
                exception: Exception
            ) {

                _uiState.update {
                    it.copy(
                        errorMessage =
                            "Equation is invalid"
                    )
                }

                return "Equation is invalid"
            }

        if (
            newHandle == 0L
        ) {

            _uiState.update {
                it.copy(
                    errorMessage =
                        "Equation is invalid"
                )
            }

            return "Equation is invalid"
        }

        val canonicalExpression =
            try {

                nativeBridge.getCanonicalExpression(
                    newHandle
                )

            } catch (
                exception: Exception
            ) {

                nativeBridge.destroyExpression(
                    newHandle
                )

                _uiState.update {
                    it.copy(
                        errorMessage =
                            "Equation is invalid"
                    )
                }

                return "Equation is invalid"
            }

        val duplicate =
            _uiState.value.graphLayers.any {
                it.id != id &&
                        it.canonicalExpression ==
                        canonicalExpression
            }

        if (duplicate) {

            nativeBridge.destroyExpression(
                newHandle
            )

            _uiState.update {
                it.copy(
                    errorMessage =
                        "Equation already added"
                )
            }

            return "Equation already added"
        }

        return try {

            val graph =
                graphEngine.generateGraph(
                    expressionHandle =
                        newHandle,
                    xMin = -10.0,
                    xMax = 10.0,
                    sampleCount =
                        INITIAL_SAMPLE_COUNT
                )

            expressionHandles[id] =
                newHandle

            nativeBridge.destroyExpression(
                oldHandle
            )

            listenController.setGraphData(
                id = id,
                graphData = graph
            )

            listenController.setExpression(
                id = id,
                expression = cleaned
            )

            val updatedLayers =
                _uiState.value.graphLayers.map {
                        currentLayer ->

                    if (
                        currentLayer.id == id
                    ) {
                        currentLayer.copy(
                            expression =
                                cleaned,
                            canonicalExpression =
                                canonicalExpression,
                            graphData =
                                graph
                        )
                    } else {
                        currentLayer
                    }
                }

            val firstLayerId =
                updatedLayers
                    .firstOrNull()
                    ?.id

            _uiState.update {
                it.copy(
                    graphLayers =
                        updatedLayers,
                    graphData =
                        updatedLayers
                            .firstOrNull {
                                it.id ==
                                        firstLayerId
                            }
                            ?.graphData
                            ?: GraphData(
                                emptyList()
                            ),
                    errorMessage = null
                )
            }

            historyRepository.addEquation(
                cleaned
            )

            _history.value =
                historyRepository.getHistory()

            null

        } catch (
            exception: Exception
        ) {

            nativeBridge.destroyExpression(
                newHandle
            )

            _uiState.update {
                it.copy(
                    errorMessage =
                        "Equation is invalid"
                )
            }

            "Equation is invalid"
        }
    }

    fun setExpressionEnabled(
        id: Long,
        enabled: Boolean
    ) {

        _uiState.update {
                state ->

            state.copy(
                graphLayers =
                    state.graphLayers.map {
                            layer ->

                        if (
                            layer.id == id
                        ) {
                            layer.copy(
                                enabled =
                                    enabled
                            )
                        } else {
                            layer
                        }
                    }
            )
        }

        if (!enabled) {

            listenController.setEnabled(
                id,
                false
            )

        } else {

            val layer =
                _uiState.value.graphLayers
                    .firstOrNull {
                        it.id == id
                    }

            listenController.setEnabled(
                id,
                layer?.audioEnabled == true
            )
        }
    }

    fun setExpressionAudioEnabled(
        id: Long,
        enabled: Boolean
    ) {

        _uiState.update {
                state ->

            state.copy(
                graphLayers =
                    state.graphLayers.map {
                            layer ->

                        if (
                            layer.id == id
                        ) {
                            layer.copy(
                                audioEnabled =
                                    enabled
                            )
                        } else {
                            layer
                        }
                    }
            )
        }

        val layer =
            _uiState.value.graphLayers
                .firstOrNull {
                    it.id == id
                }

        listenController.setAudioEnabled(
            id = id,
            enabled =
                enabled &&
                        layer?.enabled == true
        )
    }

    fun clearError() {

        _uiState.update {
            it.copy(
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
                .firstOrNull {
                    it.enabled
                }
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
            viewport.scale <= 0f
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

        _uiState.update {
            it.copy(
                isListening = true
            )
        }
    }

    fun stopListening() {

        listenController.stop()

        _uiState.update {
            it.copy(
                isListening = false
            )
        }
    }

    private fun calculateViewportSampleCount(
        screenWidth: Float
    ): Int {

        if (
            !screenWidth.isFinite() ||
            screenWidth <= 0f
        ) {
            return MIN_VIEWPORT_SAMPLES
        }

        return (
                screenWidth.toDouble() *
                        SAMPLES_PER_PIXEL
                )
            .toInt()
            .coerceIn(
                MIN_VIEWPORT_SAMPLES,
                MAX_VIEWPORT_SAMPLES
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
            viewport.scale <= 0f
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
            calculateViewportSampleCount(
                screenWidth
            )

        samplingGeneration +=
            1L

        val generation =
            samplingGeneration

        _uiState.update {
            it.copy(
                isLoading = true
            )
        }

        val handles =
            expressionHandles.toMap()

        val layers =
            _uiState.value.graphLayers.toList()

        viewModelScope.launch(
            Dispatchers.Default
        ) {

            val updatedLayers =
                layers.map {
                        layer ->

                    val handle =
                        handles[layer.id]

                    if (
                        handle == null
                    ) {
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
                            id =
                                layer.id,
                            graphData =
                                graph
                        )

                        listenController.setExpression(
                            id =
                                layer.id,
                            expression =
                                layer.expression
                        )

                        layer.copy(
                            graphData =
                                graph
                        )
                    }
                }

            if (
                generation ==
                samplingGeneration
            ) {

                _uiState.update {
                        state ->

                    state.copy(
                        graphLayers =
                            updatedLayers,
                        graphData =
                            updatedLayers
                                .firstOrNull()
                                ?.graphData
                                ?: GraphData(
                                    emptyList()
                                ),
                        isLoading = false
                    )
                }
            }
        }
    }

    override fun onCleared() {

        samplingGeneration +=
            1L

        viewportController.clear()

        listenController.release()

        expressionHandles.values.forEach {
                handle ->

            nativeBridge.destroyExpression(
                handle
            )
        }

        expressionHandles.clear()

        super.onCleared()
    }
}