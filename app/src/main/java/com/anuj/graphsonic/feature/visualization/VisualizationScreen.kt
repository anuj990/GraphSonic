package com.anuj.graphsonic.feature.visualization

import androidx.compose.ui.graphics.nativeCanvas


import androidx.compose.foundation.Canvas
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
import com.anuj.graphsonic.domain.model.GraphData
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round
import android.graphics.Paint
private val axisPaint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 28f
        color = android.graphics.Color.DKGRAY
    }
@Composable
fun VisualizationScreen(
    graphData: GraphData,
    modifier: Modifier = Modifier
) {
    var viewport by remember {
        mutableStateOf(GraphViewport())
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->

                    val oldScale = viewport.scale

                    val newScale = (oldScale * zoom)
                        .coerceIn(10f, 500f)

                    // Mathematical point under the fingers
                    // before zoom.
                    val beforeZoomX =
                        viewport.centerX +
                                (centroid.x - size.width / 2f) /
                                oldScale

                    val beforeZoomY =
                        viewport.centerY -
                                (centroid.y - size.height / 2f) /
                                oldScale

                    // Mathematical point under the fingers
                    // after zoom.
                    val afterZoomX =
                        viewport.centerX +
                                (centroid.x - size.width / 2f) /
                                newScale

                    val afterZoomY =
                        viewport.centerY -
                                (centroid.y - size.height / 2f) /
                                newScale

                    viewport = viewport.copy(
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
    ) {

        drawGrid(viewport)

        drawAxes(viewport)

        drawGraph(
            graphData = graphData,
            viewport = viewport
        )

        drawAxisLabels(viewport)
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

    val step = chooseGridStep(scale)

    var x = floor(left / step) * step

    while (x <= right) {

        val screenX =
            screenCenterX +
                    ((x - viewport.centerX) * scale)
                        .toFloat()

        drawLine(
            color = Color.LightGray,
            start = Offset(screenX, 0f),
            end = Offset(screenX, height),
            strokeWidth = 1f
        )

        x += step
    }

    var y = floor(bottom / step) * step

    while (y <= top) {

        val screenY =
            screenCenterY -
                    ((y - viewport.centerY) * scale)
                        .toFloat()

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
                (viewport.centerX * viewport.scale)
                    .toFloat()

    val yAxis =
        centerY +
                (viewport.centerY * viewport.scale)
                    .toFloat()

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

        if (!point.x.isFinite() ||
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
private fun DrawScope.drawAxisLabels(
    viewport: GraphViewport
) {
    val width = size.width
    val height = size.height

    val centerX = width / 2f
    val centerY = height / 2f

    val scale = viewport.scale

    val left =
        viewport.centerX -
                centerX / scale

    val right =
        viewport.centerX +
                centerX / scale

    val bottom =
        viewport.centerY -
                centerY / scale

    val top =
        viewport.centerY +
                centerY / scale

    val step =
        chooseGridStep(scale)

    val axisX =
        centerX -
                (
                        viewport.centerX * scale
                        ).toFloat()

    val axisY =
        centerY +
                (
                        viewport.centerY * scale
                        ).toFloat()

    val firstX =
        ceil(left / step) * step

    var x = firstX

    while (x <= right) {

        if (abs(x) > step / 100.0) {

            val screenX =
                centerX +
                        (
                                (x - viewport.centerX) *
                                        scale
                                ).toFloat()

            drawContext.canvas.nativeCanvas.drawText(
                formatAxisValue(x),
                screenX + 4f,
                (axisY + 18f)
                    .coerceIn(
                        18f,
                        height - 4f
                    ),
                axisPaint
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
                        (
                                (y - viewport.centerY) *
                                        scale
                                ).toFloat()

            drawContext.canvas.nativeCanvas.drawText(
                formatAxisValue(y),
                (axisX + 8f)
                    .coerceIn(
                        4f,
                        width - 40f
                    ),
                screenY - 4f,
                axisPaint
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

    val rounded = round(value)

    return if (
        abs(value - rounded) < 1e-9
    ) {
        rounded.toLong().toString()
    } else {
        "%.2f".format(value)
    }
}
