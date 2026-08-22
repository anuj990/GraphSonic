package com.anuj.graphsonic.feature.audio



import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

class AudioEngine {

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

    fun start() {
        if (
            audioTrack.playState ==
            AudioTrack.PLAYSTATE_PLAYING
        ) {
            return
        }

        audioTrack.play()
    }

    fun stop() {
        if (
            audioTrack.playState ==
            AudioTrack.PLAYSTATE_PLAYING
        ) {
            audioTrack.pause()
        }

        audioTrack.flush()
    }

    fun setFrequency(
        frequency: Double
    ) {
        val safeFrequency =
            frequency.coerceIn(
                minFrequency,
                maxFrequency
            )

        val samples =
            ShortArray(
                sampleRate / 20
            )

        val phaseStep =
            2.0 * PI *
                    safeFrequency /
                    sampleRate.toDouble()

        for (i in samples.indices) {
            samples[i] =
                (
                        sin(phase) *
                                Short.MAX_VALUE *
                                0.15
                        ).toInt().toShort()

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

    fun release() {
        audioTrack.stop()
        audioTrack.release()
    }
}