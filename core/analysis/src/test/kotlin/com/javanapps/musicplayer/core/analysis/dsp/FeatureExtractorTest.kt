package com.javanapps.musicplayer.core.analysis.dsp

import com.google.common.truth.Truth.assertThat
import com.javanapps.musicplayer.core.analysis.decode.PcmAudio
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class FeatureExtractorTest {
    private val extractor = FeatureExtractor()
    private val sampleRate = 44_100

    @Test
    fun extract_pureSine_centroidNearToneFrequency() {
        val frequency = 2_000.0
        val samples = FloatArray(sampleRate) { sin(2.0 * PI * frequency * it / sampleRate).toFloat() }

        val features = extractor.extract(PcmAudio(samples, sampleRate, 1_000))

        assertThat(features.spectralCentroid).isWithin(250f).of(frequency.toFloat())
        assertThat(features.rmsEnergy).isGreaterThan(0f)
    }

    @Test
    fun extract_silence_returnsZeroEnergy() {
        val features = extractor.extract(PcmAudio(FloatArray(sampleRate), sampleRate, 1_000))

        assertThat(features.rmsEnergy).isEqualTo(0f)
        assertThat(features.spectralFlux).isEqualTo(0f)
    }

    @Test
    fun extract_tooShort_returnsEmptyFeatures() {
        val features = extractor.extract(PcmAudio(FloatArray(16), sampleRate, 0))

        assertThat(features.spectralCentroid).isEqualTo(0f)
        assertThat(features.estimatedTempoBpm).isEqualTo(0f)
    }
}
