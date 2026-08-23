package com.anuj.graphsonic.feature.audio


import com.anuj.graphsonic.domain.model.GraphPoint

data class GraphSegment(
    val points: List<GraphPoint>
) {
    val startX: Double
        get() = points.first().x

    val endX: Double
        get() = points.last().x
}