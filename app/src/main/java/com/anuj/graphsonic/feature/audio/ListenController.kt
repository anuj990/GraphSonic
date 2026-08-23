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

class ListenController(
    private val scope: CoroutineScope,
    private val evaluateAt: (Double) -> Double,
    private val isDefinedAt: (Double) -> Boolean
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

    private var volume =
        0.15

    private var frequencyMode =
        FrequencyMode.Continuous

    private var playbackJob: Job? =
        null

    private var startX =
        -10.0

    private var endX =
        10.0

    private var step =
        0.01

    private var playbackSpeed =
        1.0

    init {
        audioEngine.setVolume(
            volume
        )

        audioEngine.setWaveform(
            waveform
        )
    }

    fun start() {

        if (
            playbackJob?.isActive == true
        ) {
            return
        }

        val firstPoint =
            findFirstDefinedPoint(
                startX,
                endX
            )

        if (firstPoint == null) {

            _state.value =
                ListenState()

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

                var x: Double =
                    firstPoint

                while (isActive) {

                    if (!isDefinedAt(x)) {

                        val next =
                            findNextDefinedPoint(
                                x
                            )

                        if (next == null) {

                            x =
                                findFirstDefinedPoint(
                                    startX,
                                    endX
                                ) ?: break

                        } else {

                            x =
                                next
                        }

                        continue
                    }

                    val y =
                        evaluateAt(x)

                    if (!y.isFinite()) {

                        val next =
                            findNextDefinedPoint(
                                x
                            )

                        if (next == null) {

                            x =
                                findFirstDefinedPoint(
                                    startX,
                                    endX
                                ) ?: break

                        } else {

                            x =
                                next
                        }

                        continue
                    }

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
                            progress =
                                calculateProgress(
                                    x
                                )
                        )

                    val next =
                        moveForward(x)

                    if (next == null) {

                        x =
                            findFirstDefinedPoint(
                                startX,
                                endX
                            ) ?: break

                    } else {

                        x =
                            next
                    }

                    delay(10L)
                }

                if (isActive) {

                    audioEngine.stop()

                    _state.value =
                        ListenState(
                            isPlaying = false
                        )

                    playbackJob =
                        null
                }
            }
    }

    private fun moveForward(
        x: Double
    ): Double? {

        val next =
            x +
                    step *
                    playbackSpeed

        if (
            !next.isFinite() ||
            next > endX
        ) {
            return null
        }

        return next
    }

    private fun findFirstDefinedPoint(
        from: Double,
        to: Double
    ): Double? {

        if (
            !from.isFinite() ||
            !to.isFinite() ||
            from > to
        ) {
            return null
        }

        var x =
            from

        while (x <= to) {

            if (isDefinedAt(x)) {
                return x
            }

            x += step
        }

        return null
    }

    private fun findNextDefinedPoint(
        currentX: Double
    ): Double? {

        var x =
            currentX +
                    step *
                    playbackSpeed

        while (x <= endX) {

            if (isDefinedAt(x)) {
                return x
            }

            x +=
                step *
                        playbackSpeed
        }

        return null
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

        waveform =
            value

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

        playbackJob?.cancel()

        playbackJob =
            null

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
            minOf(
                start,
                end
            )

        val max =
            maxOf(
                start,
                end
            )

        if (max <= min) {
            return
        }

        startX =
            min

        endX =
            max
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

        _state.value =
            ListenState()
    }

    fun release() {

        stop()

        audioEngine.release()
    }
}