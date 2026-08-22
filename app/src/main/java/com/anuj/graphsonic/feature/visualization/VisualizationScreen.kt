package com.anuj.graphsonic.feature.visualization

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import com.anuj.graphsonic.domain.model.GraphData
import com.anuj.graphsonic.domain.model.GraphPoint
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer

@Composable
fun VisualizationScreen(
    graphData: GraphData,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var viewport by remember {
        mutableStateOf(GraphViewport())
    }

    var cursor by remember {
        mutableStateOf(GraphCursorState())
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val oldScale = viewport.scale

                    val newScale =
                        (oldScale * zoom)
                            .coerceIn(10f, 500f)

                    val beforeZoomX =
                        viewport.centerX +
                                (
                                        centroid.x -
                                                size.width / 2f
                                        ) / oldScale

                    val beforeZoomY =
                        viewport.centerY -
                                (
                                        centroid.y -
                                                size.height / 2f
                                        ) / oldScale

                    val afterZoomX =
                        viewport.centerX +
                                (
                                        centroid.x -
                                                size.width / 2f
                                        ) / newScale

                    val afterZoomY =
                        viewport.centerY -
                                (
                                        centroid.y -
                                                size.height / 2f
                                        ) / newScale

                    viewport =
                        viewport.copy(
                            centerX =
                                viewport.centerX +
                                        (beforeZoomX - afterZoomX) -
                                        pan.x / newScale,
                            centerY =
                                viewport.centerY +
                                        (beforeZoomY - afterZoomY) +
                                        pan.y / newScale,
                            scale = newScale
                        )
                }
            }
            .pointerInput(graphData) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { position ->
                        updateCursor(
                            position = position,
                            graphData = graphData,
                            viewport = viewport,
                            screenWidth = size.width.toFloat()
                        ) { newCursor ->
                            cursor = newCursor
                        }
                    },

                    onDrag = { change, _ ->
                        updateCursor(
                            position = change.position,
                            graphData = graphData,
                            viewport = viewport,
                            screenWidth = size.width.toFloat()
                        ) { newCursor ->
                            cursor = newCursor
                        }

                        change.consume()
                    },
                    onDragEnd = {
                        cursor =
                            cursor.copy(
                                visible = false
                            )
                    },
                    onDragCancel = {
                        cursor =
                            cursor.copy(
                                visible = false
                            )
                    }
                )
            }
    ) {
        drawGrid(
            viewport = viewport
        )

        drawAxes(
            viewport = viewport
        )

        drawGraph(
            graphData = graphData,
            viewport = viewport
        )

        drawCursor(
            cursor = cursor,
            viewport = viewport
        )

        drawAxisLabels(
            viewport = viewport
            ,textMeasurer = textMeasurer
        )
    }
}

private fun DrawScope.drawGrid(
    viewport: GraphViewport
) {
    val width = size.width
    val height = size.height

    val screenCenterX = width / 2f
    val screenCenterY = height / 2f

    val scale = viewport.scale

    val left =
        viewport.centerX -
                screenCenterX / scale

    val right =
        viewport.centerX +
                screenCenterX / scale

    val bottom =
        viewport.centerY -
                screenCenterY / scale

    val top =
        viewport.centerY +
                screenCenterY / scale

    val step =
        chooseGridStep(scale)

    var x =
        floor(left / step) * step

    while (x <= right) {
        val screenX =
            screenCenterX +
                    (
                            (x - viewport.centerX) *
                                    scale
                            ).toFloat()

        drawLine(
            color = Color.LightGray,
            start = Offset(
                screenX,
                0f
            ),
            end = Offset(
                screenX,
                height
            ),
            strokeWidth = 1f
        )

        x += step
    }

    var y =
        floor(bottom / step) * step

    while (y <= top) {
        val screenY =
            screenCenterY -
                    (
                            (y - viewport.centerY) *
                                    scale
                            ).toFloat()

        drawLine(
            color = Color.LightGray,
            start = Offset(
                0f,
                screenY
            ),
            end = Offset(
                width,
                screenY
            ),
            strokeWidth = 1f
        )

        y += step
    }
}

private fun DrawScope.drawAxes(
    viewport: GraphViewport
) {
    val width = size.width
    val height = size.height

    val centerX = width / 2f
    val centerY = height / 2f

    val xAxis =
        centerX -
                (
                        viewport.centerX *
                                viewport.scale
                        ).toFloat()

    val yAxis =
        centerY +
                (
                        viewport.centerY *
                                viewport.scale
                        ).toFloat()

    if (xAxis in 0f..width) {
        drawLine(
            color = Color.Black,
            start = Offset(
                xAxis,
                0f
            ),
            end = Offset(
                xAxis,
                height
            ),
            strokeWidth = 2f
        )
    }

    if (yAxis in 0f..height) {
        drawLine(
            color = Color.Black,
            start = Offset(
                0f,
                yAxis
            ),
            end = Offset(
                width,
                yAxis
            ),
            strokeWidth = 2f
        )
    }
}

private fun DrawScope.drawGraph(
    graphData: GraphData,
    viewport: GraphViewport
) {
    val width = size.width
    val height = size.height

    val centerX = width / 2f
    val centerY = height / 2f

    val path = Path()

    var pathStarted = false

    for (point in graphData.points) {
        if (
            !point.x.isFinite() ||
            !point.y.isFinite()
        ) {
            pathStarted = false
            continue
        }

        val screenX =
            centerX +
                    (
                            (point.x - viewport.centerX) *
                                    viewport.scale
                            ).toFloat()

        val screenY =
            centerY -
                    (
                            (point.y - viewport.centerY) *
                                    viewport.scale
                            ).toFloat()

        if (
            screenX < -10000f ||
            screenX > width + 10000f ||
            screenY < -10000f ||
            screenY > height + 10000f
        ) {
            pathStarted = false
            continue
        }

        if (!pathStarted) {
            path.moveTo(
                screenX,
                screenY
            )

            pathStarted = true
        } else {
            path.lineTo(
                screenX,
                screenY
            )
        }
    }

    drawPath(
        path = path,
        color = Color.Black,
        style = Stroke(
            width = 4f,
            cap = StrokeCap.Round
        )
    )
}

private fun DrawScope.drawCursor(
    cursor: GraphCursorState,
    viewport: GraphViewport
) {
    if (!cursor.visible) {
        return
    }

    val centerX =
        size.width / 2f

    val centerY =
        size.height / 2f

    val screenX =
        centerX +
                (
                        (cursor.x - viewport.centerX) *
                                viewport.scale
                        ).toFloat()

    val screenY =
        centerY -
                (
                        (cursor.y - viewport.centerY) *
                                viewport.scale
                        ).toFloat()

    drawLine(
        color = Color.DarkGray,
        start = Offset(
            screenX,
            0f
        ),
        end = Offset(
            screenX,
            size.height
        ),
        strokeWidth = 1.5f
    )

    drawLine(
        color = Color.DarkGray,
        start = Offset(
            0f,
            screenY
        ),
        end = Offset(
            size.width,
            screenY
        ),
        strokeWidth = 1.5f
    )

    drawCircle(
        color = Color.Black,
        radius = 8f,
        center = Offset(
            screenX,
            screenY
        )
    )
}

private fun DrawScope.drawAxisLabels(
    viewport: GraphViewport,
    textMeasurer: TextMeasurer
) {
    val width = size.width
    val height = size.height

    val centerX = width / 2f
    val centerY = height / 2f

    val scale = viewport.scale

    val left =
        viewport.centerX - centerX / scale

    val right =
        viewport.centerX + centerX / scale

    val bottom =
        viewport.centerY - centerY / scale

    val top =
        viewport.centerY + centerY / scale

    val step =
        chooseGridStep(scale)

    val axisX =
        centerX -
                (viewport.centerX * scale).toFloat()

    val axisY =
        centerY +
                (viewport.centerY * scale).toFloat()

    val textStyle =
        TextStyle(
            color = Color.DarkGray
        )

    val firstX =
        ceil(left / step) * step

    var x = firstX

    while (x <= right) {

        if (abs(x) > step / 100.0) {

            val screenX =
                centerX +
                        ((x - viewport.centerX) * scale)
                            .toFloat()

            drawText(
                textMeasurer = textMeasurer,
                text = formatAxisValue(x),
                topLeft = Offset(
                    screenX + 4f,
                    (axisY + 4f)
                        .coerceIn(
                            0f,
                            height - 24f
                        )
                ),
                style = textStyle
            )
        }

        x += step
    }

    val firstY =
        ceil(bottom / step) * step

    var y = firstY

    while (y <= top) {

        if (abs(y) > step / 100.0) {

            val screenY =
                centerY -
                        ((y - viewport.centerY) * scale)
                            .toFloat()

            drawText(
                textMeasurer = textMeasurer,
                text = formatAxisValue(y),
                topLeft = Offset(
                    (axisX + 8f)
                        .coerceIn(
                            0f,
                            width - 40f
                        ),
                    screenY - 20f
                ),
                style = textStyle
            )
        }

        y += step
    }
}

private fun chooseGridStep(
    scale: Float
): Double {
    val targetPixels = 80.0

    val rawStep =
        targetPixels / scale.toDouble()

    val exponent =
        floor(log10(rawStep))

    val base =
        10.0.pow(exponent)

    val normalized =
        rawStep / base

    return when {
        normalized <= 1.0 -> base
        normalized <= 2.0 -> 2.0 * base
        normalized <= 5.0 -> 5.0 * base
        else -> 10.0 * base
    }
}

private fun formatAxisValue(
    value: Double
): String {
    val rounded = round(value)

    return if (
        abs(value - rounded) < 1e-9
    ) {
        rounded.toLong().toString()
    } else {
        "%.2f".format(value)
    }
}

private fun updateCursor(
    position: Offset,
    graphData: GraphData,
    viewport: GraphViewport,
    screenWidth: Float,
    onCursorChanged: (GraphCursorState) -> Unit
) {
    val mathematicalX =
        viewport.centerX +
                (
                        position.x.toDouble() -
                                screenWidth.toDouble() / 2.0
                        ) /
                viewport.scale.toDouble()

    val nearest =
        findNearestPoint(
            graphData = graphData,
            x = mathematicalX
        )

    if (nearest != null) {
        onCursorChanged(
            GraphCursorState(
                visible = true,
                x = nearest.x,
                y = nearest.y
            )
        )
    }
}

private fun findNearestPoint(
    graphData: GraphData,
    x: Double
): GraphPoint? {
    var nearest: GraphPoint? = null

    var smallestDistance =
        Double.POSITIVE_INFINITY

    for (point in graphData.points) {
        if (
            !point.x.isFinite() ||
            !point.y.isFinite()
        ) {
            continue
        }

        val distance =
            abs(point.x - x)

        if (
            distance < smallestDistance
        ) {
            smallestDistance =
                distance

            nearest =
                point
        }
    }

    return nearest
}