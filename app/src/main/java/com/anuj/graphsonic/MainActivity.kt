package com.anuj.graphsonic

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.anuj.graphsonic.engine.NativeBridge

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bridge = NativeBridge()

        val handle =
            bridge.createExpression("1/x")
        val points =
            bridge.generateGraph(
                handle = handle,
                xMin = -2.0,
                xMax = 2.0,
                sampleCount = 9
            )

        for (i in points.indices step 2) {

            val x = points[i]
            val y = points[i + 1]

            Log.d(
                "GraphSonic",
                "x=$x, y=$y"
            )
        }

        bridge.destroyExpression(handle)
    }
}