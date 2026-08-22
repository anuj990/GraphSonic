package com.anuj.graphsonic.feature.visualization

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anuj.graphsonic.domain.model.GraphData
import com.anuj.graphsonic.feature.visualization.components.CursorInfoCard
import com.anuj.graphsonic.feature.visualization.components.GraphCanvas

@Composable
fun VisualizationScreen(
    graphData: GraphData,
    cursor: GraphCursorState,
    onCursorChanged: (GraphCursorState) -> Unit,
    evaluateAt: (Double) -> Double,
    onViewportChanged: (GraphViewport, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        GraphCanvas(
            graphData = graphData,
            cursor = cursor,
            onCursorChanged = onCursorChanged,
            evaluateAt = evaluateAt,
            onViewportChanged = onViewportChanged,
            modifier = Modifier.fillMaxSize()
        )

        CursorInfoCard(
            cursor = cursor
        )
    }
}