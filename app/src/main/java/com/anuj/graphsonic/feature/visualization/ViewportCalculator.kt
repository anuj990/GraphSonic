package com.anuj.graphsonic.feature.visualization


class ViewportCalculator {

    fun calculateRange(
        viewport: GraphViewport,
        screenWidth: Float
    ): Pair<Double, Double> {

        val halfWidth =
            screenWidth.toDouble() /
                    (2.0 * viewport.scale.toDouble())

        val xMin =
            viewport.centerX.toDouble() -
                    halfWidth

        val xMax =
            viewport.centerX.toDouble() +
                    halfWidth

        return xMin to xMax
    }

    fun calculateSampleCount(
        xMin: Double,
        xMax: Double
    ): Int {

        val range =
            (xMax - xMin).coerceAtLeast(0.001)

        val count =
            (range * 100.0)
                .toInt()
                .coerceIn(
                    1000,
                    10000
                )

        return count
    }
}