package com.anuj.graphsonic.feature.audio

import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

data class MusicalNote(
    val name: String,
    val octave: Int,
    val frequency: Double
)

class NoteMapper {

    private val noteNames =
        arrayOf(
            "C",
            "C#",
            "D",
            "D#",
            "E",
            "F",
            "F#",
            "G",
            "G#",
            "A",
            "A#",
            "B"
        )

    fun map(
        frequency: Double
    ): MusicalNote? {

        if (
            !frequency.isFinite() ||
            frequency <= 0.0
        ) {
            return null
        }

        val midi =
            (
                    69.0 +
                            12.0 *
                            log2(
                                frequency /
                                        440.0
                            )
                    ).roundToInt()

        val noteIndex =
            ((midi % 12) + 12) % 12

        val octave =
            midi / 12 - 1

        val noteFrequency =
            440.0 *
                    2.0.pow(
                        (midi - 69) /
                                12.0
                    )

        return MusicalNote(
            name =
                noteNames[noteIndex],
            octave = octave,
            frequency =
                noteFrequency
        )
    }
}