package com.anuj.graphsonic.feature.visualization


import androidx.compose.runtime.Immutable

@Immutable
data class GraphViewport(
    val centerX: Float = 0f,
    val centerY: Float = 0f,
    val scale: Float = 50f
) {

    fun visibleWidth(
        screenWidth: Float
    ): Float {
        return screenWidth / scale
    }

    fun visibleHeight(
        screenHeight: Float
    ): Float {
        return screenHeight / scale
    }
}