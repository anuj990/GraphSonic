package com.anuj.graphsonic.feature.visualization.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.anuj.graphsonic.feature.visualization.GraphCursorState
import com.anuj.graphsonic.feature.visualization.GraphViewport
import com.anuj.graphsonic.feature.visualization.utils.graphToScreen

fun DrawScope.drawGraphCursor(
    cursor: GraphCursorState,
    viewport: GraphViewport,
    color: Color = Color.White
) {
    if (
        !cursor.visible ||
        !cursor.x.isFinite()
    ) {
        return
    }

    cursor.values.forEachIndexed { index, value ->
        if (!value.y.isFinite()) {
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
            color = pointerColor(index),
            radius = 9f,
            center = position
        )

        drawCircle(
            color = color,
            radius = 4f,
            center = position
        )
    }
}

fun pointerColor(
    index: Int
): Color {
    return when (index % 8) {
        0 -> Color(0xFF4F7CFF)
        1 -> Color(0xFFFF5C7A)
        2 -> Color(0xFF43B581)
        3 -> Color(0xFFB26CFF)
        4 -> Color(0xFFFFA63D)
        5 -> Color(0xFF35C2C9)
        6 -> Color(0xFF9A7B62)
        else -> Color(0xFFE45B9A)
    }
}