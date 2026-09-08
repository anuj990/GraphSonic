package com.anuj.graphsonic.feature.visualization

data class GraphCursorValue(
    val equationId: Long,
    val expression: String,
    val y: Double
)

data class GraphCursorState(
    val visible: Boolean = false,
    val x: Double = 0.0,
    val y: Double = 0.0,
    val values: List<GraphCursorValue> = emptyList()
)