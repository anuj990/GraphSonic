package com.anuj.graphsonic.feature.audio

import com.anuj.graphsonic.domain.model.GraphData
import com.anuj.graphsonic.domain.model.GraphPoint

object GraphSegmentExtractor {

    fun extract(
        graphData: GraphData
    ): List<GraphSegment> {

        if (graphData.points.isEmpty()) {
            return emptyList()
        }

        val segments =
            mutableListOf<GraphSegment>()

        val current =
            mutableListOf<GraphPoint>()

        fun finishSegment() {

            if (current.size >= 2) {

                segments.add(
                    GraphSegment(
                        points =
                            current.toList()
                    )
                )
            }

            current.clear()
        }

        for (point in graphData.points) {

            if (
                !point.x.isFinite() ||
                !point.y.isFinite()
            ) {
                finishSegment()
                continue
            }

            current.add(point)
        }

        finishSegment()

        return segments
    }
}