package com.javanapps.musicplayer.core.analysis.classify

import com.google.common.truth.Truth.assertThat
import com.javanapps.musicplayer.core.analysis.metadata.TrackMetadata
import com.javanapps.musicplayer.core.model.TagSource
import org.junit.Test

class MetadataTaggerTest {
    private val tagger = MetadataTagger()

    @Test
    fun tag_decadeAndGenre_emitsBoth() {
        val labels = tagger.tag(SONG_ID, TrackMetadata(year = 1995, genre = "Rock")).map { it.label }

        assertThat(labels).containsAtLeast("90s", "Rock")
    }

    @Test
    fun tag_classicalGenre_emitsClassicalLabel() {
        val labels = tagger.tag(SONG_ID, TrackMetadata(year = null, genre = "Orchestra")).map { it.label }

        assertThat(labels).contains("Classical")
    }

    @Test
    fun tag_emptyMetadata_emitsNothing() {
        val tags = tagger.tag(SONG_ID, TrackMetadata(year = null, genre = null))

        assertThat(tags).isEmpty()
    }

    @Test
    fun tag_decade_sourcedFromMetadata() {
        val tags = tagger.tag(SONG_ID, TrackMetadata(year = 2021, genre = null))

        assertThat(tags.single().source).isEqualTo(TagSource.METADATA)
        assertThat(tags.single().label).isEqualTo("2020s")
    }

    private companion object {
        const val SONG_ID = 7L
    }
}
