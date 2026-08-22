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
    if (!cursor.visible) {
        return
    }

    val position = graphToScreen(
        x = cursor.x,
        y = cursor.y,
        screenWidth = size.width,
        screenHeight = size.height,
        viewport = viewport
    )

    drawLine(
        color = Color.DarkGray,
        start = Offset(
            position.x,
            0f
        ),
        end = Offset(
            position.x,
            size.height
        ),
        strokeWidth = 1.5f
    )

    drawLine(
        color = Color.DarkGray,
        start = Offset(
            0f,
            position.y
        ),
        end = Offset(
            size.width,
            position.y
        ),
        strokeWidth = 1.5f
    )

    drawCircle(
        color = Color.Black,
        radius = 8f,
        center = position
    )
}