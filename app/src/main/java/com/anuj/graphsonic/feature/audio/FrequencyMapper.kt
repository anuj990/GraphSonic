package com.anuj.graphsonic.feature.audio


import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max

class FrequencyMapper {

    private val minFrequency = 80.0
    private val maxFrequency = 2000.0

    fun map(
        value: Double
    ): Double {
        if (!value.isFinite()) {
            return minFrequency
        }

        val magnitude =
            max(
                abs(value),
                0.000001
            )

        val normalized =
            ln(1.0 + magnitude) /
                    ln(1.0 + 100.0)

        return minFrequency +
                normalized.coerceIn(
                    0.0,
                    1.0
                ) *
                (
                        maxFrequency -
                                minFrequency
                        )
    }
}