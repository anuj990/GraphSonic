package com.anuj.graphsonic.feature.visualization.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anuj.graphsonic.feature.visualization.GraphCursorState

@Composable
fun CursorInfoCard(
    cursor: GraphCursorState,
    modifier: Modifier = Modifier
) {
    if (!cursor.visible) {
        return
    }

    Column(
        modifier = modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp
            )
    ) {
        Text(
            text = "x = ${formatValue(cursor.x)}"
        )

        Text(
            text = "y = ${formatValue(cursor.y)}"
        )
    }
}

private fun formatValue(
    value: Double
): String {
    return "%.4f".format(value)
}