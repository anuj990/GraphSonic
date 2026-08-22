package com.anuj.graphsonic.feature.visualization


data class GraphSamplingState(
    val xMin: Double = -10.0,
    val xMax: Double = 10.0,
    val sampleCount: Int = 2000
)