package com.javanapps.musicplayer.core.analysis.classify

import com.google.common.truth.Truth.assertThat
import com.javanapps.musicplayer.core.analysis.decode.PcmAudio
import com.javanapps.musicplayer.core.analysis.metadata.TrackMetadata
import com.javanapps.musicplayer.core.model.AudioFeatures
import com.javanapps.musicplayer.core.model.SongTag
import com.javanapps.musicplayer.core.model.TagSource
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class CompositeTagClassifierTest {
    private val rules = RuleTagClassifier(MetadataTagger())
    private val model = mockk<TfLiteTagClassifier>()
    private val composite = CompositeTagClassifier(model, rules)

    private val pcm = PcmAudio(FloatArray(0), 44_100, 0)
    private val features = features()

    @Test
    fun classify_modelUnavailable_usesRuleEngine() {
        every { model.isAvailable() } returns false

        val tags = composite.classify(SONG_ID, pcm, features, TrackMetadata(year = 1985, genre = null))

        assertThat(tags.map { it.label }).contains("80s")
        assertThat(tags.map { it.source }).doesNotContain(TagSource.AUDIO_MODEL)
    }

    @Test
    fun classify_modelAvailable_usesModel() {
        val modelTag = SongTag(SONG_ID, "Happy", 0.9f, TagSource.AUDIO_MODEL)
        every { model.isAvailable() } returns true
        every { model.classify(SONG_ID, pcm, features, any()) } returns listOf(modelTag)

        val tags = composite.classify(SONG_ID, pcm, features, TrackMetadata(year = null, genre = null))

        assertThat(tags).containsExactly(modelTag)
    }

    private fun features() =
        AudioFeatures(
            durationMs = 0,
            sampleRate = 44_100,
            rmsEnergy = 0f,
            zeroCrossingRate = 0f,
            spectralCentroid = 0f,
            spectralRolloff = 0f,
            spectralFlux = 0f,
            highFreqRatio = 0f,
            estimatedTempoBpm = 0f,
            beatStrength = 0f,
        )

    private companion object {
        const val SONG_ID = 1L
    }
}
