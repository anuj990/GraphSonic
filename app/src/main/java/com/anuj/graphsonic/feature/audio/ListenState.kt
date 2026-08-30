package com.anuj.graphsonic.feature.audio

data class ListenVoiceState(
    val equationId: Long,
    val isDefined: Boolean = false,
    val x: Double = 0.0,
    val y: Double = Double.NaN,
    val frequency: Double = 0.0,
    val note: MusicalNote? = null
)

data class ListenState(
    val isPlaying: Boolean = false,
    val progress: Double = 0.0,
    val voices: List<ListenVoiceState> = emptyList()
) {
    val x: Double
        get() =
            voices.firstOrNull()?.x ?: 0.0

    val y: Double
        get() =
            voices.firstOrNull()?.y
                ?: Double.NaN

    val frequency: Double
        get() =
            voices.firstOrNull()?.frequency
                ?: 0.0

    val note: MusicalNote?
        get() =
            voices.firstOrNull()?.note
}