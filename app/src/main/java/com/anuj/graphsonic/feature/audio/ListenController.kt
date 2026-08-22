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

    private var playbackJob: Job? = null

    private var startX = -10.0
    private var endX = 10.0
    private var step = 0.02

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

                    val frequency =
                        if (y.isFinite()) {
                            frequencyMapper.map(y)
                        } else {
                            0.0
                        }

                    if (frequency > 0.0) {
                        audioEngine.setFrequency(
                            frequency
                        )
                    }

                    _state.value =
                        ListenState(
                            isPlaying = true,
                            x = x,
                            y = y,
                            frequency = frequency
                        )

                    x += step

                    if (x > endX) {
                        x = startX
                    }

                    delay(20L)
                }
            }
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
            maxOf(
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