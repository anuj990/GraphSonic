package com.anuj.graphsonic

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.anuj.graphsonic.engine.NativeBridge

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nativeBridge = NativeBridge()

        val expression =
            nativeBridge.createExpression(
                "sin(x)"
            )

        val result =
            nativeBridge.evaluateExpression(
                expression,
                0.0
            )

        Log.d(
            "GraphSonic",
            "f(3) = $result"
        )

        nativeBridge.destroyExpression(expression)
    }
}