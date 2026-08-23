package com.anuj.graphsonic.feature.audio

data class ListenState(
    val isPlaying: Boolean = false,
    val x: Double = 0.0,
    val y: Double = 0.0,
    val frequency: Double = 0.0,
    val note: MusicalNote? = null,
    val progress: Double = 0.0
)