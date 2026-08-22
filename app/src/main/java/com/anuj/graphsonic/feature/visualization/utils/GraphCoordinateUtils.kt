package com.anuj.graphsonic.feature.visualization.utils


import androidx.compose.ui.geometry.Offset
import com.anuj.graphsonic.feature.visualization.GraphViewport

fun screenToGraphX(
    screenX: Float,
    screenWidth: Float,
    viewport: GraphViewport
): Double {
    return viewport.centerX +
            (
                    screenX.toDouble() -
                            screenWidth.toDouble() / 2.0
                    ) /
            viewport.scale.toDouble()
}

fun graphToScreenX(
    x: Double,
    screenWidth: Float,
    viewport: GraphViewport
): Float {
    return screenWidth / 2f +
            (
                    (x - viewport.centerX) *
                            viewport.scale
                    ).toFloat()
}

fun graphToScreenY(
    y: Double,
    screenHeight: Float,
    viewport: GraphViewport
): Float {
    return screenHeight / 2f -
            (
                    (y - viewport.centerY) *
                            viewport.scale
                    ).toFloat()
}

fun graphToScreen(
    x: Double,
    y: Double,
    screenWidth: Float,
    screenHeight: Float,
    viewport: GraphViewport
): Offset {
    return Offset(
        graphToScreenX(
            x = x,
            screenWidth = screenWidth,
            viewport = viewport
        ),
        graphToScreenY(
            y = y,
            screenHeight = screenHeight,
            viewport = viewport
        )
    )
}