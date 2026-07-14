package com.javanapps.musicplayer.core.analysis.dsp

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ResamplerTest {
    @Test
    fun resample_sameRate_returnsSameArray() {
        val samples = floatArrayOf(1f, 2f, 3f, 4f)

        val result = Resampler.resample(samples, sourceRate = 16_000, targetRate = 16_000)

        assertThat(result).isSameInstanceAs(samples)
    }

    @Test
    fun resample_downsampling_halvesSampleCount() {
        val samples = FloatArray(32_000) { it.toFloat() }

        val result = Resampler.resample(samples, sourceRate = 32_000, targetRate = 16_000)

        assertThat(result.size).isEqualTo(16_000)
    }

    @Test
    fun resample_upsampling_interpolatesBetweenSamples() {
        val samples = floatArrayOf(0f, 10f)

        val result = Resampler.resample(samples, sourceRate = 1, targetRate = 2)

        assertThat(result[0]).isEqualTo(0f)
        assertThat(result.last()).isWithin(0.01f).of(10f)
    }

    @Test
    fun resample_constantSignal_staysConstant() {
        val samples = FloatArray(1_000) { 5f }

        val result = Resampler.resample(samples, sourceRate = 44_100, targetRate = 16_000)

        assertThat(result.all { it == 5f }).isTrue()
    }
}
