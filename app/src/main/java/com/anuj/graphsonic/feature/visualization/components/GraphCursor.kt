package com.anuj.graphsonic.feature.visualization.components


import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.anuj.graphsonic.feature.visualization.GraphCursorState
import com.anuj.graphsonic.feature.visualization.GraphViewport
import com.anuj.graphsonic.feature.visualization.utils.graphToScreen

fun DrawScope.drawGraphCursor(
    cursor: GraphCursorState,
    viewport: GraphViewport
) {

    if (
        !cursor.visible ||
        !cursor.x.isFinite()
    ) {
        return
    }

    cursor.values.forEachIndexed { index, value ->

        if (
            !value.y.isFinite()
        ) {
            return@forEachIndexed
        }

        val position =
            graphToScreen(
                x = cursor.x,
                y = value.y,
                screenWidth = size.width,
                screenHeight = size.height,
                viewport = viewport
            )

        if (
            position.x < 0f ||
            position.x > size.width ||
            position.y < 0f ||
            position.y > size.height
        ) {
            return@forEachIndexed
        }

        drawCircle(
            color =
                pointerColor(index),
            radius = 8f,
            center = position
        )

        drawCircle(
            color = Color.White,
            radius = 4f,
            center = position
        )
    }
}