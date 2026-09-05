package com.anuj.graphsonic.feature.visualization.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.anuj.graphsonic.feature.audio.ListenState
import com.anuj.graphsonic.feature.visualization.GraphCursorState
import com.anuj.graphsonic.feature.visualization.GraphLayer
import com.anuj.graphsonic.feature.visualization.GraphViewport
import com.anuj.graphsonic.feature.visualization.utils.graphToScreen
import com.anuj.graphsonic.feature.visualization.utils.screenToGraphX
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun GraphCanvas(
    graphLayers: List<GraphLayer>,
    cursor: GraphCursorState,
    listenState: ListenState,
    onCursorChanged: (GraphCursorState) -> Unit,
    evaluateAt: (Double) -> Double,
    onViewportChanged: (GraphViewport, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasWidth by remember {
        mutableStateOf(0f)
    }

    var viewport by remember {
        mutableStateOf(GraphViewport())
    }

    val textMeasurer =
        rememberTextMeasurer()

    LaunchedEffect(
        viewport,
        canvasWidth
    ) {
        if (canvasWidth > 0f) {
            onViewportChanged(
                viewport,
                canvasWidth
            )
        }
    }

    Canvas(
        modifier = modifier
            .onSizeChanged {
                canvasWidth = it.width.toFloat()
            }
            .pointerInput(Unit) {
                detectTransformGestures {
                        centroid,
                        pan,
                        zoom,
                        _ ->

                    val oldScale =
                        viewport.scale

                    val newScale =
                        (
                                oldScale * zoom
                                ).coerceIn(
                                10f,
                                500f
                            )

                    val beforeZoomX =
                        viewport.centerX +
                                (
                                        centroid.x.toDouble() -
                                                size.width.toDouble() / 2.0
                                        ) /
                                oldScale.toDouble()

                    val beforeZoomY =
                        viewport.centerY -
                                (
                                        centroid.y.toDouble() -
                                                size.height.toDouble() / 2.0
                                        ) /
                                oldScale.toDouble()

                    val afterZoomX =
                        viewport.centerX +
                                (
                                        centroid.x.toDouble() -
                                                size.width.toDouble() / 2.0
                                        ) /
                                newScale.toDouble()

                    val afterZoomY =
                        viewport.centerY -
                                (
                                        centroid.y.toDouble() -
                                                size.height.toDouble() / 2.0
                                        ) /
                                newScale.toDouble()

                    viewport =
                        viewport.copy(
                            centerX =
                                (
                                        beforeZoomX +
                                                (
                                                        viewport.centerX.toDouble() -
                                                                afterZoomX
                                                        ) -
                                                pan.x.toDouble() /
                                                newScale.toDouble()
                                        ).toFloat(),
                            centerY =
                                (
                                        beforeZoomY +
                                                (
                                                        viewport.centerY.toDouble() -
                                                                afterZoomY
                                                        ) +
                                                pan.y.toDouble() /
                                                newScale.toDouble()
                                        ).toFloat(),
                            scale = newScale
                        )
                }
            }
            .pointerInput(
                graphLayers,
                viewport
            ) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { position ->
                        updateCursor(
                            position = position,
                            viewport = viewport,
                            evaluateAt = evaluateAt,
                            screenWidth = size.width.toFloat()
                        ) {
                                newCursor ->
                            onCursorChanged(newCursor)
                        }
                    },
                    onDrag = { change, _ ->
                        updateCursor(
                            position = change.position,
                            viewport = viewport,
                            evaluateAt = evaluateAt,
                            screenWidth = size.width.toFloat()
                        ) {
                                newCursor ->
                            onCursorChanged(newCursor)
                        }

                        change.consume()
                    },
                    onDragEnd = {
                        onCursorChanged(
                            cursor.copy(
                                visible = false
                            )
                        )
                    },
                    onDragCancel = {
                        onCursorChanged(
                            cursor.copy(
                                visible = false
                            )
                        )
                    }
                )
            }
    ) {
        drawGrid(viewport)

        drawAxes(viewport)

        graphLayers
            .filter {
                it.enabled
            }
            .forEach { layer ->
                drawGraph(
                    graphData = layer.graphData,
                    viewport = viewport,
                    color = graphColor(layer.colorIndex)
                )
            }

        drawListenCursors(
            listenState = listenState,
            viewport = viewport
        )

        drawGraphCursor(
            cursor = cursor,
            viewport = viewport
        )

        drawAxisLabels(
            viewport = viewport,
            textMeasurer = textMeasurer
        )
    }
}

private fun graphColor(
    index: Int
): Color {
    return when (index % 8) {
        0 -> Color(0xFF1565C0)
        1 -> Color(0xFFD32F2F)
        2 -> Color(0xFF2E7D32)
        3 -> Color(0xFF7B1FA2)
        4 -> Color(0xFFEF6C00)
        5 -> Color(0xFF00838F)
        6 -> Color(0xFF6D4C41)
        else -> Color(0xFFC2185B)
    }
}

private fun DrawScope.drawListenCursors(
    listenState: ListenState,
    viewport: GraphViewport
) {
    if (!listenState.isPlaying) {
        return
    }

    val voices =
        listenState.voices

    if (voices.isEmpty()) {
        return
    }

    val firstDefined =
        voices.firstOrNull {
            it.isDefined &&
                    it.x.isFinite()
        }

    if (firstDefined != null) {
        val xPosition =
            graphToScreen(
                x = firstDefined.x,
                y = 0.0,
                screenWidth = size.width,
                screenHeight = size.height,
                viewport = viewport
            ).x

        if (
            xPosition >= 0f &&
            xPosition <= size.width
        ) {
            drawLine(
                color = Color.Red,
                start = Offset(xPosition, 0f),
                end = Offset(xPosition, size.height),
                strokeWidth = 2f
            )
        }
    }

    voices.forEachIndexed {
            index,
            voice ->

        if (
            !voice.isDefined ||
            !voice.x.isFinite() ||
            !voice.y.isFinite()
        ) {
            return@forEachIndexed
        }

        val position =
            graphToScreen(
                x = voice.x,
                y = voice.y,
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
            color = Color.White,
            radius = 4f,
            center = position
        )
    }
}

private fun pointerColor(
    index: Int
): Color {
    return when (index % 6) {
        0 -> Color.Red
        1 -> Color.Blue
        2 -> Color.Green
        3 -> Color.Magenta
        4 -> Color.Cyan
        else -> Color.Yellow
    }
}

private fun updateCursor(
    position: Offset,
    viewport: GraphViewport,
    evaluateAt: (Double) -> Double,
    screenWidth: Float,
    onCursorChanged: (GraphCursorState) -> Unit
) {
    val x =
        screenToGraphX(
            screenX = position.x,
            screenWidth = screenWidth,
            viewport = viewport
        )

    val y =
        evaluateAt(x)

    if (!y.isFinite()) {
        onCursorChanged(
            GraphCursorState(
                visible = false
            )
        )

        return
    }

    onCursorChanged(
        GraphCursorState(
            visible = true,
            x = x,
            y = y
        )
    )
}

private fun DrawScope.drawGrid(
    viewport: GraphViewport
) {
    val width = size.width
    val height = size.height

    val centerX = width / 2f
    val centerY = height / 2f
    val scale = viewport.scale

    val left =
        viewport.centerX -
                centerX.toDouble() /
                scale.toDouble()

    val right =
        viewport.centerX +
                centerX.toDouble() /
                scale.toDouble()

    val bottom =
        viewport.centerY -
                centerY.toDouble() /
                scale.toDouble()

    val top =
        viewport.centerY +
                centerY.toDouble() /
                scale.toDouble()

    val step =
        chooseGridStep(scale)

    var x =
        floor(left / step) * step

    while (x <= right) {
        val screenX =
            centerX +
                    (
                            (x - viewport.centerX) *
                                    scale.toDouble()
                            ).toFloat()

        drawLine(
            color = Color.LightGray,
            start = Offset(screenX, 0f),
            end = Offset(screenX, height),
            strokeWidth = 1f
        )

        x += step
    }

    var y =
        floor(bottom / step) * step

    while (y <= top) {
        val screenY =
            centerY -
                    (
                            (y - viewport.centerY) *
                                    scale.toDouble()
                            ).toFloat()

        drawLine(
            color = Color.LightGray,
            start = Offset(0f, screenY),
            end = Offset(width, screenY),
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
                                viewport.scale.toDouble()
                        ).toFloat()

    val yAxis =
        centerY +
                (
                        viewport.centerY *
                                viewport.scale.toDouble()
                        ).toFloat()

    if (xAxis in 0f..width) {
        drawLine(
            color = Color.Black,
            start = Offset(xAxis, 0f),
            end = Offset(xAxis, height),
            strokeWidth = 2f
        )
    }

    if (yAxis in 0f..height) {
        drawLine(
            color = Color.Black,
            start = Offset(0f, yAxis),
            end = Offset(width, yAxis),
            strokeWidth = 2f
        )
    }
}

private fun DrawScope.drawGraph(
    graphData: com.anuj.graphsonic.domain.model.GraphData,
    viewport: GraphViewport,
    color: Color
) {
    val path =
        Path()

    var pathStarted = false
    var previousPosition: Offset? = null

    val width = size.width
    val height = size.height

    val maximumVerticalJump =
        height * 1.5f

    val maximumTotalJump =
        maxOf(
            width,
            height
        ) * 2.0f

    val maximumOffscreenDistance =
        maxOf(
            width,
            height
        ) * 4.0f

    for (point in graphData.points) {
        if (
            !point.x.isFinite() ||
            !point.y.isFinite()
        ) {
            pathStarted = false
            previousPosition = null
            continue
        }

        val position =
            graphToScreen(
                x = point.x,
                y = point.y,
                screenWidth = width,
                screenHeight = height,
                viewport = viewport
            )

        if (
            !position.x.isFinite() ||
            !position.y.isFinite()
        ) {
            pathStarted = false
            previousPosition = null
            continue
        }

        if (
            abs(position.x) > maximumOffscreenDistance ||
            abs(position.y) > maximumOffscreenDistance
        ) {
            pathStarted = false
            previousPosition = null
            continue
        }

        val previous =
            previousPosition

        if (
            pathStarted &&
            previous != null
        ) {
            val dx =
                abs(
                    position.x -
                            previous.x
                )

            val dy =
                abs(
                    position.y -
                            previous.y
                )

            val totalDistance =
                kotlin.math.hypot(
                    dx.toDouble(),
                    dy.toDouble()
                )

            val pathologicalVerticalJump =
                dy >
                        maximumVerticalJump &&
                        dx <
                        width * 0.25f

            val pathologicalTotalJump =
                totalDistance >
                        maximumTotalJump

            if (
                pathologicalVerticalJump ||
                pathologicalTotalJump
            ) {
                pathStarted = false
                previousPosition = null
                continue
            }

            path.lineTo(
                position.x,
                position.y
            )
        } else {
            path.moveTo(
                position.x,
                position.y
            )

            pathStarted = true
        }

        previousPosition = position
    }

    drawPath(
        path = path,
        color = color,
        style =
            Stroke(
                width = 4f,
                cap = StrokeCap.Round
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
        viewport.centerX -
                centerX.toDouble() /
                scale.toDouble()

    val right =
        viewport.centerX +
                centerX.toDouble() /
                scale.toDouble()

    val bottom =
        viewport.centerY -
                centerY.toDouble() /
                scale.toDouble()

    val top =
        viewport.centerY +
                centerY.toDouble() /
                scale.toDouble()

    val step =
        chooseGridStep(scale)

    val axisX =
        centerX -
                (
                        viewport.centerX *
                                scale.toDouble()
                        ).toFloat()

    val axisY =
        centerY +
                (
                        viewport.centerY *
                                scale.toDouble()
                        ).toFloat()

    val textStyle =
        TextStyle(
            color = Color.DarkGray
        )

    var x =
        ceil(left / step) * step

    while (x <= right) {
        if (abs(x) > step / 100.0) {
            val screenX =
                centerX +
                        (
                                (x - viewport.centerX) *
                                        scale.toDouble()
                                ).toFloat()

            drawText(
                textMeasurer = textMeasurer,
                text = formatAxisValue(x),
                topLeft =
                    Offset(
                        screenX + 4f,
                        (
                                axisY + 4f
                                ).coerceIn(
                                0f,
                                height - 24f
                            )
                    ),
                style = textStyle
            )
        }

        x += step
    }

    var y =
        ceil(bottom / step) * step

    while (y <= top) {
        if (abs(y) > step / 100.0) {
            val screenY =
                centerY -
                        (
                                (y - viewport.centerY) *
                                        scale.toDouble()
                                ).toFloat()

            drawText(
                textMeasurer = textMeasurer,
                text = formatAxisValue(y),
                topLeft =
                    Offset(
                        (
                                axisX + 8f
                                ).coerceIn(
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
    val rawStep =
        80.0 /
                scale.toDouble()

    val exponent =
        floor(
            log10(rawStep)
        )

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
    val rounded =
        kotlin.math.round(value)

    return if (
        abs(value - rounded) < 1e-9
    ) {
        rounded.toLong().toString()
    } else {
        "%.2f".format(value)
    }
}