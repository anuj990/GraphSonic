package com.anuj.graphsonic.feature.visualization.components

import com.anuj.graphsonic.feature.visualization.GraphCursorState


data class MultiGraphCursorState(
    val cursors: Map<Long, GraphCursorState> = emptyMap()
)