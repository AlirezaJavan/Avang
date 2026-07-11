package com.javanapps.musicplayer.core.analysis.classify

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LabelMapTest {
    @Test
    fun displayName_knownGenreClass_returnsGenreTag() {
        assertThat(LabelMap.displayName("Jazz")).isEqualTo("Jazz")
        assertThat(LabelMap.displayName("Reggae")).isEqualTo("Reggae")
        assertThat(LabelMap.displayName("Hip hop music")).isEqualTo("Hip-Hop")
        assertThat(LabelMap.displayName("Rhythm and blues")).isEqualTo("R&B")
    }

    @Test
    fun displayName_isCaseInsensitiveAndTrimsWhitespace() {
        assertThat(LabelMap.displayName("  JAZZ  ")).isEqualTo("Jazz")
    }

    @Test
    fun displayName_moodClass_returnsNull() {
        assertThat(LabelMap.displayName("Happy music")).isNull()
        assertThat(LabelMap.displayName("Sad music")).isNull()
        assertThat(LabelMap.displayName("Angry music")).isNull()
        assertThat(LabelMap.displayName("Scary music")).isNull()
    }

    @Test
    fun displayName_nonMusicAudioSetClass_returnsNull() {
        assertThat(LabelMap.displayName("Guitar")).isNull()
        assertThat(LabelMap.displayName("Speech")).isNull()
        assertThat(LabelMap.displayName("Music")).isNull()
    }

    @Test
    fun displayName_unknownLabel_returnsNull() {
        assertThat(LabelMap.displayName("not a real label")).isNull()
    }

    @Test
    fun allDisplayNames_containsMappedGenres() {
        assertThat(LabelMap.allDisplayNames).contains("Jazz")
        assertThat(LabelMap.allDisplayNames).doesNotContain("Happy")
    }
}
