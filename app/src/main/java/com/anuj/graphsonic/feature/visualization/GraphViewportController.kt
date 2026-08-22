package com.anuj.graphsonic.feature.visualization

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GraphViewportController(
    private val scope: CoroutineScope,
    private val onViewportSettled:
        (GraphViewport, Float) -> Unit
) {

    private var samplingJob: Job? = null

    fun onViewportChanged(
        viewport: GraphViewport,
        screenWidth: Float
    ) {
        samplingJob?.cancel()

        samplingJob =
            scope.launch {
                delay(150L)

                onViewportSettled(
                    viewport,
                    screenWidth
                )
            }
    }

    fun clear() {
        samplingJob?.cancel()
        samplingJob = null
    }
}