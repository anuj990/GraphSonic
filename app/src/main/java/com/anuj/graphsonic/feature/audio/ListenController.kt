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
    private var frequencyMode =
        FrequencyMode.Continuous
    private var playbackJob: Job? = null

    private var startX = -10.0
    private var endX = 10.0
    private var step = 0.01

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
                                frequency =
                                    frequency
                            )

                        x += step

                    } else {

                        x += step

                        _state.value =
                            ListenState(
                                isPlaying = true,
                                x = x,
                                y = Double.NaN,
                                frequency = 0.0
                            )
                    }

                    if (x > endX) {
                        x = startX
                    }

                    delay(10L)
                }
            }
    }
    fun setFrequencyMode(
        mode: FrequencyMode
    ) {
        frequencyMode = mode
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
        startX =
            minOf(
                start,
                end
            )

        endX =
            max(
                start,
                end
            )
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