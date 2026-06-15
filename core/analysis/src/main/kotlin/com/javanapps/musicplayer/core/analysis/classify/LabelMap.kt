package com.javanapps.musicplayer.core.analysis.classify

object LabelMap {
    private val displayNames =
        mapOf(
            "calm" to "Calm",
            "relax" to "Calm",
            "relaxing" to "Calm",
            "ambient" to "Calm",
            "quiet" to "Calm",
            "dance" to "Dance",
            "techno" to "Dance",
            "house" to "Dance",
            "electronic" to "Energetic",
            "energetic" to "Energetic",
            "loud" to "Energetic",
            "happy" to "Happy",
            "sad" to "Sad",
            "aggressive" to "Intense",
            "classical" to "Classical",
            "instrumental" to "Instrumental",
            "acoustic" to "Acoustic",
        )

    fun displayName(rawLabel: String): String? = displayNames[rawLabel.trim().lowercase()]
}
