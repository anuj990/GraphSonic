package com.anuj.graphsonic.engine

import com.anuj.graphsonic.domain.model.GraphData
import com.anuj.graphsonic.domain.model.GraphPoint

class GraphEngine(
    private val nativeBridge: NativeBridge
) {

    fun generateRawGraph(
        expressionHandle: Long,
        xMin: Double,
        xMax: Double,
        sampleCount: Int
    ): DoubleArray {
        return nativeBridge.generateGraph(
            handle = expressionHandle,
            xMin = xMin,
            xMax = xMax,
            sampleCount = sampleCount
        )
    }

    fun generateGraph(
        expressionHandle: Long,
        xMin: Double,
        xMax: Double,
        sampleCount: Int
    ): GraphData {

        val rawPoints =
            generateRawGraph(
                expressionHandle = expressionHandle,
                xMin = xMin,
                xMax = xMax,
                sampleCount = sampleCount
            )

        val points =
            buildList(
                rawPoints.size / 2
            ) {

                var index = 0

                while (
                    index + 1 <
                    rawPoints.size
                ) {

                    add(
                        GraphPoint(
                            x = rawPoints[index],
                            y = rawPoints[index + 1]
                        )
                    )

                    index += 2
                }
            }

        return GraphData(points)
    }

    fun evaluate(
        expressionHandle: Long,
        x: Double
    ): Double {
        return nativeBridge.evaluateExpression(
            handle = expressionHandle,
            x = x
        )
    }

    fun isDefined(
        expressionHandle: Long,
        x: Double
    ): Boolean {
        return nativeBridge.isDefined(
            handle = expressionHandle,
            x = x
        )
    }
}