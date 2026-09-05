package com.anuj.graphsonic.feature.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.atomic.AtomicReferenceArray
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

class AudioEngine {

    data class Voice(
        val frequency: Double,
        val volume: Double,
        val waveform: Waveform,
        val active: Boolean
    )

    @Volatile
    private var masterVolume =
        0.15

    @Volatile
    private var defaultWaveform =
        Waveform.Sine

    private val sampleRate =
        44100

    private val minFrequency =
        80.0

    private val maxFrequency =
        2000.0

    private val maxVoices =
        16

    private val bufferSize =
        AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(
            sampleRate / 10
        )

    private val audioTrack =
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(
                        AudioAttributes.USAGE_MEDIA
                    )
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_MUSIC
                    )
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(
                        sampleRate
                    )
                    .setEncoding(
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    .setChannelMask(
                        AudioFormat.CHANNEL_OUT_MONO
                    )
                    .build()
            )
            .setBufferSizeInBytes(
                bufferSize
            )
            .setTransferMode(
                AudioTrack.MODE_STREAM
            )
            .build()

    private val voices =
        AtomicReferenceArray<Voice?>(
            maxVoices
        )

    private val phases =
        DoubleArray(
            maxVoices
        )

    private val currentFrequencies =
        DoubleArray(
            maxVoices
        ) {
            minFrequency
        }

    private var audioJob:
            Thread? = null

    @Volatile
    private var running =
        false

    fun setVolume(
        value: Double
    ) {
        masterVolume =
            value.coerceIn(
                0.0,
                1.0
            )
    }

    fun setWaveform(
        value: Waveform
    ) {
        defaultWaveform =
            value

        for (
        index in 0 until maxVoices
        ) {
            val voice =
                voices.get(index)

            if (voice != null) {
                voices.set(
                    index,
                    voice.copy(
                        waveform = value
                    )
                )
            }
        }
    }

    fun setFrequency(
        frequency: Double
    ) {
        setVoice(
            index = 0,
            frequency = frequency,
            waveform =
                defaultWaveform,
            active = true
        )
    }

    fun setVoice(
        index: Int,
        frequency: Double,
        waveform: Waveform,
        active: Boolean,
        volume: Double = 1.0
    ) {
        if (
            index !in 0 until maxVoices
        ) {
            return
        }

        voices.set(
            index,
            Voice(
                frequency =
                    frequency.coerceIn(
                        minFrequency,
                        maxFrequency
                    ),
                volume =
                    volume.coerceIn(
                        0.0,
                        1.0
                    ),
                waveform =
                    waveform,
                active =
                    active
            )
        )
    }

    fun clearVoice(
        index: Int
    ) {
        if (
            index !in 0 until maxVoices
        ) {
            return
        }

        voices.set(
            index,
            null
        )

        phases[index] =
            0.0

        currentFrequencies[index] =
            minFrequency
    }

    fun clearVoices() {
        for (
        index in 0 until maxVoices
        ) {
            clearVoice(
                index
            )
        }
    }

    fun start() {

        if (running) {
            return
        }

        running =
            true

        audioTrack.play()

        audioJob =
            Thread {
                generateAudio()
            }.apply {
                start()
            }
    }

    private fun generateSample(
        phase: Double,
        waveform: Waveform
    ): Double {
        return when (waveform) {
            Waveform.Sine ->
                sin(phase)

            Waveform.Triangle ->
                1.0 -
                        4.0 *
                        abs(
                            phase /
                                    (2.0 * PI) -
                                    floor(
                                        phase /
                                                (2.0 * PI) +
                                                0.5
                                    )
                        )

            Waveform.Square ->
                if (
                    phase < PI
                ) {
                    1.0
                } else {
                    -1.0
                }

            Waveform.Saw ->
                2.0 *
                        (
                                phase /
                                        (2.0 * PI) -
                                        floor(
                                            phase /
                                                    (2.0 * PI) +
                                                    0.5
                                        )
                                )
        }
    }

    private fun generateAudio() {

        val samples =
            ShortArray(
                sampleRate / 50
            )

        val smoothing =
            0.02

        while (running) {

            for (
            index in samples.indices
            ) {

                var mixedSample =
                    0.0

                var activeVoices =
                    0

                for (
                voiceIndex in 0 until maxVoices
                ) {

                    val voice =
                        voices.get(
                            voiceIndex
                        )
                            ?: continue

                    if (
                        !voice.active
                    ) {
                        continue
                    }

                    currentFrequencies[
                        voiceIndex
                    ] +=
                        (
                                voice.frequency -
                                        currentFrequencies[
                                            voiceIndex
                                        ]
                                ) *
                                smoothing

                    mixedSample +=
                        generateSample(
                            phases[
                                voiceIndex
                            ],
                            voice.waveform
                        ) *
                                voice.volume

                    val phaseStep =
                        2.0 *
                                PI *
                                currentFrequencies[
                                    voiceIndex
                                ] /
                                sampleRate.toDouble()

                    phases[
                        voiceIndex
                    ] +=
                        phaseStep

                    if (
                        phases[
                            voiceIndex
                        ] >=
                        2.0 * PI
                    ) {
                        phases[
                            voiceIndex
                        ] -=
                            2.0 * PI
                    }

                    activeVoices++
                }

                if (
                    activeVoices > 1
                ) {
                    mixedSample /=
                        sqrt(
                            activeVoices.toDouble()
                        )
                }

                val output =
                    (
                            mixedSample *
                                    Short.MAX_VALUE *
                                    masterVolume
                            )
                        .coerceIn(
                            Short.MIN_VALUE.toDouble(),
                            Short.MAX_VALUE.toDouble()
                        )
                        .toInt()
                        .toShort()

                samples[index] =
                    output
            }

            if (running) {
                audioTrack.write(
                    samples,
                    0,
                    samples.size
                )
            }
        }
    }

    fun stop() {

        running =
            false

        audioJob?.interrupt()
        audioJob = null

        if (
            audioTrack.playState ==
            AudioTrack.PLAYSTATE_PLAYING
        ) {
            audioTrack.pause()
        }

        audioTrack.flush()
    }

    fun release() {

        stop()

        audioTrack.release()
    }
}