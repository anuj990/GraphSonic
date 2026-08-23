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

    private var segments =
        emptyList<GraphSegment>()

    init {
        audioEngine.setVolume(
            volume
        )

        audioEngine.setWaveform(
            waveform
        )
    }

    fun setGraphData(
        graphData: GraphData
    ) {
        segments =
            GraphSegmentExtractor.extract(
                graphData
            )
    }

    fun start() {

        if (
            playbackJob?.isActive == true
        ) {
            return
        }

        val usableSegments =
            getUsableSegments()

        if (
            usableSegments.isEmpty()
        ) {
            _state.value =
                ListenState()

            return
        }

        val firstSegmentIndex =
            findFirstSegmentIndex(
                usableSegments
            )

        if (
            firstSegmentIndex < 0
        ) {
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

                var segmentIndex =
                    firstSegmentIndex

                var segment =
                    usableSegments[
                        segmentIndex
                    ]

                var x =
                    maxOf(
                        startX,
                        segment.startX
                    )

                while (isActive) {


                    if (
                        x > segment.endX
                    ) {

                        segmentIndex =
                            nextSegmentIndex(
                                currentIndex =
                                    segmentIndex,
                                size =
                                    usableSegments.size
                            )

                        segment =
                            usableSegments[
                                segmentIndex
                            ]

                        x =
                            maxOf(
                                startX,
                                segment.startX
                            )

                        continue
                    }

                    val y =
                        evaluateAt(x)

                    if (
                        !y.isFinite()
                    ) {

                        val nextX =
                            moveForward(
                                x
                            )

                        if (
                            nextX != null &&
                            nextX <= segment.endX
                        ) {

                            x =
                                nextX

                        } else {

                            segmentIndex =
                                nextSegmentIndex(
                                    currentIndex =
                                        segmentIndex,
                                    size =
                                        usableSegments.size
                                )

                            segment =
                                usableSegments[
                                    segmentIndex
                                ]

                            x =
                                maxOf(
                                    startX,
                                    segment.startX
                                )
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

                    val nextX =
                        moveForward(
                            x
                        )

                    if (
                        nextX != null &&
                        nextX <= segment.endX
                    ) {

                        x =
                            nextX

                    } else {


                        segmentIndex =
                            nextSegmentIndex(
                                currentIndex =
                                    segmentIndex,
                                size =
                                    usableSegments.size
                            )

                        segment =
                            usableSegments[
                                segmentIndex
                            ]

                        x =
                            maxOf(
                                startX,
                                segment.startX
                            )
                    }

                    delay(10L)
                }
            }
    }

    private fun getUsableSegments():
            List<GraphSegment> {

        return segments.filter { segment ->

            segment.points.size >= 2 &&
                    segment.startX.isFinite() &&
                    segment.endX.isFinite() &&
                    segment.endX >= segment.startX
        }
    }


    private fun findFirstSegmentIndex(
        segments: List<GraphSegment>
    ): Int {

        if (
            segments.isEmpty()
        ) {
            return -1
        }

        for (
        index in segments.indices
        ) {

            val segment =
                segments[index]

            val intersectsRange =
                segment.endX >= startX &&
                        segment.startX <= endX

            if (
                intersectsRange
            ) {
                return index
            }
        }

        return -1
    }

    private fun nextSegmentIndex(
        currentIndex: Int,
        size: Int
    ): Int {

        if (
            size <= 1
        ) {
            return 0
        }

        return (
                currentIndex + 1
                ) % size
    }

    private fun moveForward(
        x: Double
    ): Double? {

        val next =
            x +
                    step *
                    playbackSpeed

        if (
            !next.isFinite()
        ) {
            return null
        }

        return next
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

    private fun finishPlayback() {

        audioEngine.stop()

        _state.value =
            ListenState(
                isPlaying = false
            )

        playbackJob =
            null
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

        if (
            max <= min
        ) {
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

        segments =
            emptyList()

        _state.value =
            ListenState()
    }

    fun release() {

        stop()

        audioEngine.release()
    }
}