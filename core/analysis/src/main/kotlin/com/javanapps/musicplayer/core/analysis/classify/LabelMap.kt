package com.javanapps.musicplayer.core.analysis.classify

// Maps YAMNet's AudioSet class names (see autotag_labels.txt) to this app's genre tags.
// Only YAMNet's music-genre classes are mapped; mood classes (e.g. "Happy music") and the
// other ~470 non-music event/instrument classes are intentionally ignored since a clip's mood
// is far less reliable than its genre and isn't something this app can act on for a playlist.
object LabelMap {
    private val displayNames =
        mapOf(
            "pop music" to "Pop",
            "hip hop music" to "Hip-Hop",
            "rock music" to "Rock",
            "heavy metal" to "Metal",
            "punk rock" to "Punk",
            "grunge" to "Grunge",
            "progressive rock" to "Progressive Rock",
            "rock and roll" to "Rock and Roll",
            "psychedelic rock" to "Psychedelic Rock",
            "rhythm and blues" to "R&B",
            "soul music" to "Soul",
            "reggae" to "Reggae",
            "country" to "Country",
            "swing music" to "Swing",
            "bluegrass" to "Bluegrass",
            "funk" to "Funk",
            "folk music" to "Folk",
            "jazz" to "Jazz",
            "disco" to "Disco",
            "classical music" to "Classical",
            "opera" to "Opera",
            "electronic music" to "Electronic",
            "house music" to "House",
            "techno" to "Techno",
            "dubstep" to "Dubstep",
            "drum and bass" to "Drum and Bass",
            "electronic dance music" to "EDM",
            "ambient music" to "Ambient",
            "trance music" to "Trance",
            "salsa music" to "Salsa",
            "flamenco" to "Flamenco",
            "blues" to "Blues",
            "new-age music" to "New Age",
            "afrobeat" to "Afrobeat",
            "christian music" to "Christian",
            "gospel music" to "Gospel",
            "music of bollywood" to "Bollywood",
            "ska" to "Ska",
            "christmas music" to "Christmas",
            "dance music" to "Dance",
        )

    val allDisplayNames: Set<String> = displayNames.values.toSet()

    fun displayName(rawLabel: String): String? = displayNames[rawLabel.trim().lowercase()]
}
