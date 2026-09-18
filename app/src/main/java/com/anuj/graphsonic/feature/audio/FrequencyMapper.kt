package com.anuj.graphsonic.feature.audio

import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.round

enum class FrequencyMode {
    Continuous,
    Musical
}

class FrequencyMapper {

    private val minFrequency = 80.0
    private val maxFrequency = 2000.0

    private val referenceFrequency = 440.0

    fun map(
        value: Double,
        mode: FrequencyMode = FrequencyMode.Continuous
    ): Double {
        if (!value.isFinite()) {
            return 0.0
        }

        return when (mode) {
            FrequencyMode.Continuous ->
                mapContinuous(value)

            FrequencyMode.Musical ->
                mapMusical(value)
        }
    }

    private fun mapContinuous(
        value: Double
    ): Double {
        val normalized =
            (
                    value.coerceIn(
                        -100.0,
                        100.0
                    ) + 100.0
                    ) / 200.0

        return minFrequency +
                normalized *
                (
                        maxFrequency -
                                minFrequency
                        )
    }

    private fun mapMusical(
        value: Double
    ): Double {
        val continuous =
            mapContinuous(value)

        val semitones =
            12.0 *
                    (
                            ln(
                                continuous /
                                        referenceFrequency
                            ) /
                                    ln(2.0)
                            )

        val roundedSemitones =
            round(semitones)

        return (
                referenceFrequency *
                        2.0.pow(
                            roundedSemitones /
                                    12.0
                        )
                ).coerceIn(
                minFrequency,
                maxFrequency
            )
    }
}