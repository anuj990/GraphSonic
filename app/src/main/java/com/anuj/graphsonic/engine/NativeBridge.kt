package com.anuj.graphsonic.engine

class NativeBridge {

    companion object {
        init {
            System.loadLibrary("graphsonic")
        }
    }

    external fun createExpression(
        expression: String
    ): Long

    external fun evaluateExpression(
        handle: Long,
        x: Double
    ): Double

    external fun isDefined(
        handle: Long,
        x: Double
    ): Boolean

    external fun generateGraph(
        handle: Long,
        xMin: Double,
        xMax: Double,
        sampleCount: Int
    ): DoubleArray

    external fun destroyExpression(
        handle: Long
    )
}