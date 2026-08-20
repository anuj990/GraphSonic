package com.anuj.graphsonic

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.anuj.graphsonic.engine.NativeBridge

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nativeBridge = NativeBridge()

        Log.d(
            "GraphSonic",
            nativeBridge.getEngineMessage()
        )
    }
}