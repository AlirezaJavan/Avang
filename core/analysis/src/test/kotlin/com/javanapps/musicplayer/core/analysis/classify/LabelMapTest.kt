package com.javanapps.musicplayer.core.analysis.classify

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LabelMapTest {
    @Test
    fun topLevelGenre_discogsStyle_returnsGenrePrefix() {
        assertThat(LabelMap.topLevelGenre("Electronic---Deep House")).isEqualTo("Electronic")
        assertThat(LabelMap.topLevelGenre("Jazz---Bop")).isEqualTo("Jazz")
        assertThat(LabelMap.topLevelGenre("Hip Hop---Trap")).isEqualTo("Hip Hop")
        assertThat(LabelMap.topLevelGenre("Folk, World, & Country---Flamenco")).isEqualTo("Folk, World, & Country")
    }

    @Test
    fun topLevelGenre_nonMusicStyle_returnsNull() {
        assertThat(LabelMap.topLevelGenre("Non-Music---Spoken Word")).isNull()
    }

    @Test
    fun topLevelGenre_labelWithoutSeparator_returnsWholeLabel() {
        assertThat(LabelMap.topLevelGenre("Pop")).isEqualTo("Pop")
    }
}
