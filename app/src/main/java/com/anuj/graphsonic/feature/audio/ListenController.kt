package com.anuj.graphsonic.feature.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

class ListenController(
    private val scope: CoroutineScope,
    private val evaluateAt: (Double) -> Double
) {

    private var waveform =
        Waveform.Sine

    private val noteMapper =
        NoteMapper()

    private val audioEngine =
        AudioEngine()

    private val frequencyMapper =
        FrequencyMapper()

    private val _state =
        MutableStateFlow(
            ListenState()
        )

    val state: StateFlow<ListenState> =
        _state.asStateFlow()

    private var volume = 0.15

    private var frequencyMode =
        FrequencyMode.Continuous

    private var playbackJob: Job? = null

    private var startX = -10.0

    private var endX = 10.0

    private var step = 0.01

    private var playbackSpeed = 1.0

    init {
        audioEngine.setVolume(
            volume
        )

        audioEngine.setWaveform(
            waveform
        )
    }

    fun start() {

        if (playbackJob?.isActive == true) {
            return
        }

        _state.value =
            _state.value.copy(
                isPlaying = true
            )

        audioEngine.start()

        playbackJob =
            scope.launch(
                Dispatchers.Default
            ) {

                var x = startX

                while (isActive) {

                    val y =
                        evaluateAt(x)

                    val progress =
                        calculateProgress(x)

                    if (y.isFinite()) {

                        val frequency =
                            frequencyMapper.map(
                                value = y,
                                mode = frequencyMode
                            )

                        audioEngine.setFrequency(
                            frequency
                        )

                        _state.value =
                            ListenState(
                                isPlaying = true,
                                x = x,
                                y = y,
                                frequency = frequency,
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
                                    },
                                progress = progress
                            )

                    } else {

                        _state.value =
                            ListenState(
                                isPlaying = true,
                                x = x,
                                y = Double.NaN,
                                frequency = 0.0,
                                note = null,
                                progress = progress
                            )
                    }

                    x +=
                        step *
                                playbackSpeed

                    if (x > endX) {
                        x = startX
                    }

                    delay(10L)
                }
            }
    }

    private fun calculateProgress(
        x: Double
    ): Double {

        val range =
            endX - startX

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

    fun setWaveform(
        value: Waveform
    ) {
        waveform = value

        audioEngine.setWaveform(
            value
        )
    }

    fun setFrequencyMode(
        mode: FrequencyMode
    ) {
        frequencyMode = mode
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

        playbackJob?.cancel()

        playbackJob = null

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

        val min =
            minOf(start, end)

        val max =
            maxOf(start, end)

        if (max <= min) {
            return
        }

        startX = min
        endX = max

        if (
            _state.value.isPlaying &&
            (
                    _state.value.x < startX ||
                            _state.value.x > endX
                    )
        ) {
            stop()
        }
    }

    fun setStep(
        value: Double
    ) {
        if (value > 0.0) {
            step = value
        }
    }

    fun reset() {
        stop()

        _state.value =
            ListenState()
    }

    fun release() {
        stop()
        audioEngine.release()
    }

}