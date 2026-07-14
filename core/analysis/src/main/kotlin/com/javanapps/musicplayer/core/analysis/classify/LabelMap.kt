package com.javanapps.musicplayer.core.analysis.classify

// Discogs genre_discogs400 labels are self-describing "Genre---Style" strings (see
// discogs_genre_labels.txt), so unlike the old YAMNet AudioSet taxonomy, no hand-curated mapping
// table is needed: the top-level genre is just the part before "---". "Non-Music" is one of the
// 15 top-level genres (spoken word, field recordings, etc.) and is intentionally excluded, since
// it means the model didn't recognize a music genre at all.
object LabelMap {
    private const val GENRE_STYLE_SEPARATOR = "---"
    private const val NON_MUSIC_GENRE = "Non-Music"

    fun topLevelGenre(discogsLabel: String): String? {
        val genre = discogsLabel.substringBefore(GENRE_STYLE_SEPARATOR)
        return genre.takeUnless { it.isEmpty() || it == NON_MUSIC_GENRE }
    }
}
