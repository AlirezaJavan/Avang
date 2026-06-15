package com.javanapps.musicplayer.core.analysis.classify

import com.google.common.truth.Truth.assertThat
import com.javanapps.musicplayer.core.analysis.decode.PcmAudio
import com.javanapps.musicplayer.core.analysis.metadata.TrackMetadata
import com.javanapps.musicplayer.core.model.AudioFeatures
import org.junit.Test

class RuleTagClassifierTest {
    private val classifier = RuleTagClassifier(MetadataTagger())
    private val emptyPcm = PcmAudio(FloatArray(0), 44_100, 0)
    private val noMetadata = TrackMetadata(year = null, genre = null)

    @Test
    fun classify_lowEnergySlowTrack_taggedCalm() {
        val features = features(rmsEnergy = 0.1f, spectralFlux = 0.1f, tempo = 70f)

        val labels = classifier.classify(SONG_ID, emptyPcm, features, noMetadata).map { it.label }

        assertThat(labels).contains("Calm")
    }

    @Test
    fun classify_steadyBeatMidTempo_taggedDance() {
        val features = features(tempo = 120f, beatStrength = 0.8f)

        val labels = classifier.classify(SONG_ID, emptyPcm, features, noMetadata).map { it.label }

        assertThat(labels).contains("Dance")
    }

    @Test
    fun classify_loudBusyTrack_taggedEnergetic() {
        val features = features(rmsEnergy = 0.8f, spectralFlux = 0.7f)

        val labels = classifier.classify(SONG_ID, emptyPcm, features, noMetadata).map { it.label }

        assertThat(labels).contains("Energetic")
    }

    @Test
    fun classify_lowHighFreqLowZcr_taggedAcoustic() {
        val features = features(highFreqRatio = 0.05f, zeroCrossingRate = 0.05f)

        val labels = classifier.classify(SONG_ID, emptyPcm, features, noMetadata).map { it.label }

        assertThat(labels).contains("Acoustic")
    }

    @Test
    fun classify_fastLoudTrack_taggedIntense() {
        val features = features(rmsEnergy = 0.7f, tempo = 140f)

        val labels = classifier.classify(SONG_ID, emptyPcm, features, noMetadata).map { it.label }

        assertThat(labels).contains("Intense")
    }

    @Test
    fun classify_confidence_clampedToUnitRange() {
        val features = features(rmsEnergy = 0.7f, tempo = 200f)

        val tags = classifier.classify(SONG_ID, emptyPcm, features, noMetadata)

        assertThat(tags).isNotEmpty()
        tags.forEach {
            assertThat(it.confidence).isAtLeast(0f)
            assertThat(it.confidence).isAtMost(1f)
        }
    }

    @Test
    fun classify_metadataYear_taggedDecade() {
        val tags = classifier.classify(SONG_ID, emptyPcm, features(), TrackMetadata(year = 1985, genre = null))

        assertThat(tags.map { it.label }).contains("80s")
    }

    private fun features(
        rmsEnergy: Float = 0f,
        spectralFlux: Float = 0f,
        tempo: Float = 0f,
        beatStrength: Float = 0f,
        highFreqRatio: Float = 0.5f,
        zeroCrossingRate: Float = 0.2f,
    ) = AudioFeatures(
        durationMs = 0,
        sampleRate = 44_100,
        rmsEnergy = rmsEnergy,
        zeroCrossingRate = zeroCrossingRate,
        spectralCentroid = 0f,
        spectralRolloff = 0f,
        spectralFlux = spectralFlux,
        highFreqRatio = highFreqRatio,
        estimatedTempoBpm = tempo,
        beatStrength = beatStrength,
    )

    private companion object {
        const val SONG_ID = 1L
    }
}
