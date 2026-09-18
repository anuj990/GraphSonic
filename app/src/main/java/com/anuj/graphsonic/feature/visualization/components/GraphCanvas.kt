package com.anuj.graphsonic.feature.visualization.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material3.MaterialTheme
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
import com.anuj.graphsonic.feature.visualization.GraphCursorValue
import com.anuj.graphsonic.feature.visualization.GraphLayer
import com.anuj.graphsonic.feature.visualization.GraphViewport
import com.anuj.graphsonic.feature.visualization.utils.graphToScreen
import com.anuj.graphsonic.feature.visualization.utils.screenToGraphX
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun GraphCanvas(
    graphLayers: List<GraphLayer>,
    cursor: GraphCursorState,
    listenState: ListenState,
    onCursorChanged: (GraphCursorState) -> Unit,
    evaluateAt: (Long, Double) -> Double,
    onViewportChanged: (GraphViewport, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasWidth by remember {
        mutableStateOf(0f)
    }

    var viewport by remember {
        mutableStateOf(GraphViewport())
    }

    val textMeasurer = rememberTextMeasurer()
    val colorScheme = MaterialTheme.colorScheme

    val backgroundColor = colorScheme.background
    val gridColor = colorScheme.outlineVariant.copy(alpha = 0.35f)
    val axisColor = colorScheme.onBackground.copy(alpha = 0.55f)
    val labelColor = colorScheme.onBackground.copy(alpha = 0.55f)
    val listenColor = colorScheme.secondary
    val cursorColor = colorScheme.primary

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

                    val oldScale = viewport.scale

                    val newScale =
                        (oldScale * zoom)
                            .coerceIn(
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

                    viewport = viewport.copy(
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
                            screenWidth = size.width.toFloat(),
                            graphLayers = graphLayers,
                            onCursorChanged = onCursorChanged
                        )
                    },
                    onDrag = { change, _ ->
                        updateCursor(
                            position = change.position,
                            viewport = viewport,
                            evaluateAt = evaluateAt,
                            screenWidth = size.width.toFloat(),
                            graphLayers = graphLayers,
                            onCursorChanged = onCursorChanged
                        )

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
        drawRect(
            color = backgroundColor
        )

        drawGrid(
            viewport = viewport,
            color = gridColor
        )

        drawAxes(
            viewport = viewport,
            color = axisColor
        )

        graphLayers
            .asSequence()
            .filter { it.enabled }
            .forEach { layer ->
                drawGraph(
                    graphData = layer.graphData,
                    viewport = viewport,
                    color = graphColor(layer.colorIndex)
                )
            }

        drawListenCursors(
            listenState = listenState,
            viewport = viewport,
            color = listenColor,
            markerColor = colorScheme.onSecondary
        )

        drawGraphCursor(
            cursor = cursor,
            viewport = viewport,
            color = cursorColor
        )

        drawAxisLabels(
            viewport = viewport,
            textMeasurer = textMeasurer,
            color = labelColor
        )
    }
}

private fun graphColor(
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

private fun DrawScope.drawListenCursors(
    listenState: ListenState,
    viewport: GraphViewport,
    color: Color,
    markerColor: Color
) {
    if (!listenState.isPlaying) {
        return
    }

    val voices = listenState.voices

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
                color = color.copy(alpha = 0.45f),
                start = Offset(xPosition, 0f),
                end = Offset(xPosition, size.height),
                strokeWidth = 2f
            )
        }
    }

    voices.forEachIndexed { index, voice ->
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
            color = MaterialThemeFallback.white,
            radius = 4f,
            center = position
        )
    }
}

private fun updateCursor(
    position: Offset,
    viewport: GraphViewport,
    evaluateAt: (Long, Double) -> Double,
    screenWidth: Float,
    graphLayers: List<GraphLayer>,
    onCursorChanged: (GraphCursorState) -> Unit
) {
    val x =
        screenToGraphX(
            screenX = position.x,
            screenWidth = screenWidth,
            viewport = viewport
        )

    val values =
        graphLayers
            .asSequence()
            .filter { it.enabled }
            .mapNotNull { layer ->
                val y = evaluateAt(
                    layer.id,
                    x
                )

                if (y.isFinite()) {
                    GraphCursorValue(
                        equationId = layer.id,
                        expression = layer.expression,
                        y = y
                    )
                } else {
                    null
                }
            }
            .toList()

    if (values.isEmpty()) {
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
            y = values.first().y,
            values = values
        )
    )
}

private fun DrawScope.drawGrid(
    viewport: GraphViewport,
    color: Color
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

    val step = chooseGridStep(scale)

    var x = floor(left / step) * step

    while (x <= right) {
        val screenX =
            centerX +
                    (
                            (x - viewport.centerX) *
                                    scale.toDouble()
                            ).toFloat()

        drawLine(
            color = color,
            start = Offset(screenX, 0f),
            end = Offset(screenX, height),
            strokeWidth = 1f
        )

        x += step
    }

    var y = floor(bottom / step) * step

    while (y <= top) {
        val screenY =
            centerY -
                    (
                            (y - viewport.centerY) *
                                    scale.toDouble()
                            ).toFloat()

        drawLine(
            color = color,
            start = Offset(0f, screenY),
            end = Offset(width, screenY),
            strokeWidth = 1f
        )

        y += step
    }
}

private fun DrawScope.drawAxes(
    viewport: GraphViewport,
    color: Color
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
            color = color,
            start = Offset(xAxis, 0f),
            end = Offset(xAxis, height),
            strokeWidth = 2f
        )
    }

    if (yAxis in 0f..height) {
        drawLine(
            color = color,
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
    val path = Path()

    var pathStarted = false
    var previousPosition: Offset? = null

    val width = size.width
    val height = size.height

    val maximumVerticalJump = height * 1.5f
    val maximumTotalJump =
        maxOf(width, height) * 2f
    val maximumOffscreenDistance =
        maxOf(width, height) * 4f

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

        val previous = previousPosition

        if (
            pathStarted &&
            previous != null
        ) {
            val dx = abs(position.x - previous.x)
            val dy = abs(position.y - previous.y)

            val totalDistance =
                hypot(
                    dx.toDouble(),
                    dy.toDouble()
                )

            val pathologicalVerticalJump =
                dy > maximumVerticalJump &&
                        dx < width * 0.25f

            val pathologicalTotalJump =
                totalDistance > maximumTotalJump

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
        style = Stroke(
            width = 4f,
            cap = StrokeCap.Round
        )
    )
}

private fun DrawScope.drawAxisLabels(
    viewport: GraphViewport,
    textMeasurer: TextMeasurer,
    color: Color
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

    val step = chooseGridStep(scale)

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

    val textStyle = TextStyle(
        color = color
    )

    var x = ceil(left / step) * step

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
                topLeft = Offset(
                    screenX + 5f,
                    (axisY + 5f).coerceIn(
                        0f,
                        height - 24f
                    )
                ),
                style = textStyle
            )
        }

        x += step
    }

    var y = ceil(bottom / step) * step

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
                topLeft = Offset(
                    (axisX + 8f).coerceIn(
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
        80.0 / scale.toDouble()

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

private object MaterialThemeFallback {
    val white = Color.White
}