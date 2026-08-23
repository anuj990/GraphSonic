package com.anuj.graphsonic.feature.visualization

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anuj.graphsonic.domain.model.GraphData
import com.anuj.graphsonic.feature.audio.ListenState
import com.anuj.graphsonic.feature.visualization.components.CursorInfoCard
import com.anuj.graphsonic.feature.visualization.components.GraphCanvas
import com.anuj.graphsonic.feature.visualization.components.ListenControls
import com.anuj.graphsonic.feature.visualization.components.ListenInfoCard

@Composable
fun VisualizationScreen(
    graphData: GraphData,
    cursor: GraphCursorState,
    listenState: ListenState,
    onCursorChanged: (GraphCursorState) -> Unit,
    evaluateAt: (Double) -> Double,
    onViewportChanged: (GraphViewport, Float) -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        GraphCanvas(
            graphData = graphData,
            cursor = cursor,
            listenState = listenState,
            onCursorChanged = onCursorChanged,
            evaluateAt = evaluateAt,
            onViewportChanged = onViewportChanged,
            modifier = Modifier.fillMaxSize()
        )

        CursorInfoCard(
            cursor = cursor
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            ListenInfoCard(
                listenState = listenState,
                modifier = Modifier.padding(
                    bottom = 8.dp
                )
            )

            ListenControls(
                isPlaying = listenState.isPlaying,
                onStart = onStartListening,
                onStop = onStopListening
            )
        }
    }
}