package com.javanapps.musicplayer.core.analysis.classify

import com.javanapps.musicplayer.core.analysis.metadata.TrackMetadata
import com.javanapps.musicplayer.core.model.SongTag
import com.javanapps.musicplayer.core.model.TagSource
import javax.inject.Inject

class MetadataTagger
    @Inject
    constructor() {
        fun tag(
            songId: Long,
            metadata: TrackMetadata,
        ): List<SongTag> {
            val tags = mutableListOf<SongTag>()
            metadata.year?.let { year ->
                decadeLabel(year)?.let { decade ->
                    tags += SongTag(songId, decade, FULL_CONFIDENCE, TagSource.METADATA)
                }
            }
            metadata.genre?.let { genre ->
                if (isClassical(genre)) {
                    tags += SongTag(songId, CLASSICAL_LABEL, CLASSICAL_CONFIDENCE, TagSource.METADATA)
                }
                tags += SongTag(songId, genre.replaceFirstChar { it.uppercase() }, GENRE_CONFIDENCE, TagSource.METADATA)
            }
            return tags
        }

        private fun decadeLabel(year: Int): String? =
            when (year) {
                in 1980..1989 -> "80s"
                in 1990..1999 -> "90s"
                in 2000..2009 -> "2000s"
                in 2010..2019 -> "2010s"
                in 2020..2029 -> "2020s"
                else -> null
            }

        private fun isClassical(genre: String): Boolean {
            val lowered = genre.lowercase()
            return CLASSICAL_KEYWORDS.any { lowered.contains(it) }
        }

        private companion object {
            const val FULL_CONFIDENCE = 1f
            const val CLASSICAL_CONFIDENCE = 0.9f
            const val GENRE_CONFIDENCE = 0.8f
            const val CLASSICAL_LABEL = "Classical"
            val CLASSICAL_KEYWORDS = listOf("classical", "orchestra", "symphony", "baroque")
        }
    }
