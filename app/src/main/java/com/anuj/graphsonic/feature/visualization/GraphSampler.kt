package com.anuj.graphsonic.feature.visualization


import com.anuj.graphsonic.domain.model.GraphData
import com.anuj.graphsonic.domain.model.GraphPoint
import com.anuj.graphsonic.engine.GraphEngine

class GraphSampler(
    private val graphEngine: GraphEngine
) {

    fun sample(
        expressionHandle: Long,
        xMin: Double,
        xMax: Double,
        sampleCount: Int
    ): GraphData {

        val safeSampleCount =
            sampleCount.coerceIn(
                500,
                10000
            )

        val rawPoints =
            graphEngine.generateRawGraph(
                expressionHandle = expressionHandle,
                xMin = xMin,
                xMax = xMax,
                sampleCount = safeSampleCount
            )

        val points =
            buildList(
                rawPoints.size / 2
            ) {

                var index = 0

                while (index + 1 < rawPoints.size) {

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
}