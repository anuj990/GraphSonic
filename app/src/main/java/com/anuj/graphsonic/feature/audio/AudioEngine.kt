package com.anuj.graphsonic.feature.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

class AudioEngine {
    @Volatile
    private var volume = 0.15
    private val sampleRate = 44100

    private val minFrequency = 80.0
    private val maxFrequency = 2000.0

    private val bufferSize =
        AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(
            sampleRate / 10
        )
    fun setVolume(
        value: Double
    ) {
        volume =
            value.coerceIn(
                0.0,
                1.0
            )
    }
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
                    .setSampleRate(sampleRate)
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

    private var phase = 0.0

    @Volatile
    private var targetFrequency =
        minFrequency

    private var currentFrequency =
        minFrequency

    private var audioJob:
            Thread? = null

    @Volatile
    private var running = false

    fun start() {

        if (running) {
            return
        }

        running = true

        audioTrack.play()

        audioJob =
            Thread {
                generateAudio()
            }.apply {
                start()
            }
    }

    fun setFrequency(
        frequency: Double
    ) {
        targetFrequency =
            frequency.coerceIn(
                minFrequency,
                maxFrequency
            )
    }

    private fun generateAudio() {

        val samples =
            ShortArray(
                sampleRate / 50
            )

        val smoothing =
            0.02

        while (running) {

            val target =
                targetFrequency

            currentFrequency +=
                (
                        target -
                                currentFrequency
                        ) *
                        smoothing

            val phaseStep =
                2.0 *
                        PI *
                        currentFrequency /
                        sampleRate.toDouble()

            for (index in samples.indices) {

                samples[index] =
                    (
                            sin(phase) *
                                    Short.MAX_VALUE *
                                    volume
                            )
                        .toInt()
                        .toShort()

                phase += phaseStep

                if (phase >= 2.0 * PI) {
                    phase -= 2.0 * PI
                }
            }

            audioTrack.write(
                samples,
                0,
                samples.size
            )
        }
    }

    fun stop() {

        running = false

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