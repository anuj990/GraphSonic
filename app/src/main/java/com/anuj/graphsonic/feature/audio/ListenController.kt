package com.anuj.graphsonic.feature.audio

import com.anuj.graphsonic.domain.model.GraphData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

class ListenController(
    private val scope: CoroutineScope,
    private val evaluateAt: (Long, Double) -> Double
) {

    private data class EquationVoice(
        val id: Long,
        var expression: String = "",
        var waveform: Waveform,
        var segments: List<GraphSegment> = emptyList(),
        var audioEnabled: Boolean = true
    )

    private val audioEngine =
        AudioEngine()

    private val frequencyMapper =
        FrequencyMapper()

    private val noteMapper =
        NoteMapper()

    private val _state =
        MutableStateFlow(
            ListenState()
        )

    val state: StateFlow<ListenState> =
        _state.asStateFlow()

    @Volatile
    private var frequencyMode =
        FrequencyMode.Continuous

    @Volatile
    private var volume =
        0.15

    @Volatile
    private var playbackSpeed =
        1.0

    @Volatile
    private var startX =
        -10.0

    @Volatile
    private var endX =
        10.0

    @Volatile
    private var step =
        0.01

    private var playbackJob: Job? =
        null

    private val playbackGeneration =
        AtomicLong(0L)

    private val equations =
        LinkedHashMap<Long, EquationVoice>()

    @Volatile
    private var defaultWaveform =
        Waveform.Sine

    init {
        audioEngine.setVolume(
            volume
        )

        audioEngine.setWaveform(
            defaultWaveform
        )
    }

    @Synchronized
    fun setGraphData(
        id: Long,
        graphData: GraphData
    ) {
        val voice =
            equations[id]
                ?: EquationVoice(
                    id = id,
                    waveform = defaultWaveform
                )

        voice.segments =
            GraphSegmentExtractor.extract(
                graphData
            )

        equations[id] =
            voice
    }

    @Synchronized
    fun setExpression(
        id: Long,
        expression: String
    ) {
        val voice =
            equations[id]
                ?: EquationVoice(
                    id = id,
                    waveform = defaultWaveform
                )

        voice.expression =
            expression

        equations[id] =
            voice
    }

    @Synchronized
    fun removeGraphData(
        id: Long
    ) {
        equations.remove(id)

        audioEngine.clearVoices()

        _state.value =
            _state.value.copy(
                voices =
                    _state.value.voices.filter {
                        it.equationId != id
                    }
            )
    }

    @Synchronized
    fun setAudioEnabled(
        id: Long,
        enabled: Boolean
    ) {
        val equation =
            equations[id]
                ?: return

        equation.audioEnabled =
            enabled

        if (!enabled) {
            audioEngine.clearVoice(
                voiceIndex(id)
            )
        }
    }

    @Synchronized
    fun setEnabled(
        id: Long,
        enabled: Boolean
    ) {
        val equation =
            equations[id]
                ?: return

        equation.audioEnabled =
            enabled

        if (!enabled) {
            audioEngine.clearVoice(
                voiceIndex(id)
            )
        }
    }

    fun start() {

        if (
            playbackJob?.isActive == true
        ) {
            return
        }

        val generation =
            playbackGeneration.incrementAndGet()

        val hasUsableVoice =
            synchronized(this) {
                equations.values.any {
                    it.audioEnabled &&
                            it.segments.any { segment ->
                                segment.points.size >= 2
                            }
                }
            }

        if (!hasUsableVoice) {
            _state.value =
                ListenState()

            return
        }

        _state.value =
            ListenState(
                isPlaying = true
            )

        audioEngine.start()

        playbackJob =
            scope.launch(
                Dispatchers.Default
            ) {

                var x =
                    startX

                while (
                    isActive &&
                    playbackGeneration.get() ==
                    generation
                ) {

                    if (x > endX) {
                        x = startX
                    }

                    val activeEquations =
                        synchronized(this@ListenController) {
                            equations.values
                                .filter {
                                    it.audioEnabled &&
                                            it.segments.any { segment ->
                                                segment.points.size >= 2
                                            }
                                }
                                .toList()
                        }

                    if (activeEquations.isEmpty()) {
                        audioEngine.clearVoices()

                        _state.value =
                            ListenState(
                                isPlaying = true,
                                progress =
                                    calculateProgress(x)
                            )

                        x +=
                            step *
                                    playbackSpeed

                        delay(10L)

                        continue
                    }

                    val states =
                        mutableListOf<ListenVoiceState>()

                    activeEquations.forEachIndexed {
                            index,
                            equation ->

                        if (
                            playbackGeneration.get() !=
                            generation
                        ) {
                            return@launch
                        }

                        val y =
                            evaluateAt(
                                equation.id,
                                x
                            )

                        val defined =
                            y.isFinite()

                        if (defined) {

                            val frequency =
                                frequencyMapper.map(
                                    value = y,
                                    mode = frequencyMode
                                )

                            audioEngine.setVoice(
                                index = index,
                                frequency = frequency,
                                waveform =
                                    equation.waveform,
                                active = true,
                                volume =
                                    voiceVolume(
                                        activeEquations.size
                                    )
                            )

                            states +=
                                ListenVoiceState(
                                    equationId =
                                        equation.id,
                                    expression =
                                        equation.expression,
                                    isDefined = true,
                                    x = x,
                                    y = y,
                                    frequency =
                                        frequency,
                                    note =
                                        if (
                                            frequencyMode ==
                                            FrequencyMode.Musical
                                        ) {
                                            noteMapper.map(
                                                frequency
                                            )
                                        } else {
                                            null
                                        }
                                )

                        } else {

                            audioEngine.setVoice(
                                index = index,
                                frequency = 0.0,
                                waveform =
                                    equation.waveform,
                                active = false
                            )

                            states +=
                                ListenVoiceState(
                                    equationId =
                                        equation.id,
                                    expression =
                                        equation.expression,
                                    isDefined = false,
                                    x = x,
                                    y = y
                                )
                        }
                    }

                    for (
                    index in activeEquations.size until 16
                    ) {
                        audioEngine.clearVoice(
                            index
                        )
                    }

                    _state.value =
                        ListenState(
                            isPlaying = true,
                            progress =
                                calculateProgress(
                                    x
                                ),
                            voices =
                                states
                        )

                    x +=
                        step *
                                playbackSpeed

                    delay(10L)
                }
            }
    }

    private fun voiceVolume(
        count: Int
    ): Double {
        return 1.0
    }

    @Synchronized
    private fun voiceIndex(
        id: Long
    ): Int {

        val index =
            equations.keys.indexOf(id)

        if (index < 0) {
            return 0
        }

        return index.coerceIn(
            0,
            15
        )
    }

    private fun calculateProgress(
        x: Double
    ): Double {

        val range =
            endX -
                    startX

        if (
            !range.isFinite() ||
            range <= 0.0
        ) {
            return 0.0
        }

        return (
                (x - startX) /
                        range
                ).coerceIn(
                0.0,
                1.0
            )
    }

    @Synchronized
    fun setWaveform(
        value: Waveform
    ) {
        defaultWaveform =
            value

        equations.values.forEach {
            it.waveform =
                value
        }

        audioEngine.setWaveform(
            value
        )
    }

    fun setFrequencyMode(
        mode: FrequencyMode
    ) {
        frequencyMode =
            mode
    }

    fun setPlaybackSpeed(
        speed: Double
    ) {
        playbackSpeed =
            speed.coerceIn(
                0.25,
                4.0
            )
    }

    fun setVolume(
        value: Double
    ) {
        volume =
            value.coerceIn(
                0.0,
                1.0
            )

        audioEngine.setVolume(
            volume
        )
    }

    fun stop() {

        playbackGeneration.incrementAndGet()

        playbackJob?.cancel()
        playbackJob = null

        audioEngine.clearVoices()
        audioEngine.stop()

        _state.value =
            ListenState(
                isPlaying = false
            )
    }

    fun setRange(
        start: Double,
        end: Double
    ) {

        if (
            !start.isFinite() ||
            !end.isFinite()
        ) {
            return
        }

        startX =
            minOf(
                start,
                end
            )

        endX =
            maxOf(
                start,
                end
            )
    }

    fun setStep(
        value: Double
    ) {

        if (
            value > 0.0 &&
            value.isFinite()
        ) {
            step =
                value
        }
    }

    fun reset() {

        stop()

        synchronized(this) {
            equations.clear()
        }

        _state.value =
            ListenState()
    }

    fun release() {

        stop()

        audioEngine.release()
    }
}