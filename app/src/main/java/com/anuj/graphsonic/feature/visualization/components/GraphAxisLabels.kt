package com.anuj.graphsonic.feature.visualization.components


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.anuj.graphsonic.feature.visualization.GraphViewport
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

fun DrawScope.drawGraphAxisLabels(
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
        chooseAxisStep(scale)

    val axisX =
        centerX -
                (
                        viewport.centerX *
                                scale
                        ).toFloat()

    val axisY =
        centerY +
                (
                        viewport.centerY *
                                scale
                        ).toFloat()

    val style =
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
                                        scale
                                ).toFloat()

            drawText(
                textMeasurer = textMeasurer,
                text = formatAxisValue(x),
                topLeft = androidx.compose.ui.geometry.Offset(
                    screenX + 4f,
                    (axisY + 4f)
                        .coerceIn(
                            0f,
                            height - 24f
                        )
                ),
                style = style
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
                                        scale
                                ).toFloat()

            drawText(
                textMeasurer = textMeasurer,
                text = formatAxisValue(y),
                topLeft = androidx.compose.ui.geometry.Offset(
                    (axisX + 8f)
                        .coerceIn(
                            0f,
                            width - 40f
                        ),
                    screenY - 20f
                ),
                style = style
            )
        }

        y += step
    }
}

private fun chooseAxisStep(
    scale: Float
): Double {
    val rawStep =
        80.0 / scale.toDouble()

    val exponent =
        floor(
            kotlin.math.log10(rawStep)
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