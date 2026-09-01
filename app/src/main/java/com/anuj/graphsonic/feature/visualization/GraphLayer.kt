package com.anuj.graphsonic.feature.visualization

import com.anuj.graphsonic.domain.model.GraphData

data class GraphLayer(
    val id: Long,
    val expression: String,
    val graphData: GraphData,
    val enabled: Boolean = true,
    val audioEnabled: Boolean = true,
    val colorIndex: Int = 0
)