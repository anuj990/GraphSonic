package com.anuj.graphsonic.engine

import com.anuj.graphsonic.domain.model.GraphData
import com.anuj.graphsonic.domain.model.GraphPoint

class GraphEngine(
    private val nativeBridge: NativeBridge
) {

    fun generateGraph(
        expressionHandle: Long,
        xMin: Double,
        xMax: Double,
        sampleCount: Int
    ): GraphData {

        val rawPoints = nativeBridge.generateGraph(
            handle = expressionHandle,
            xMin = xMin,
            xMax = xMax,
            sampleCount = sampleCount
        )

        val points = buildList {

            for (i in rawPoints.indices step 2) {

                val x = rawPoints[i]
                val y = rawPoints[i + 1]

                add(
                    GraphPoint(
                        x = x,
                        y = y
                    )
                )
            }
        }

        return GraphData(points)
    }
}