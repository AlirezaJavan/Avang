package com.javanapps.musicplayer.core.analysis.dsp

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class FftTest {
    @Test
    fun transform_pureSine_peaksAtSourceBin() {
        val n = 1024
        val bin = 64
        val re = FloatArray(n) { sin(2.0 * PI * bin * it / n).toFloat() }
        val im = FloatArray(n)

        Fft.transform(re, im)

        val magnitudes = FloatArray(n / 2) { k -> re[k] * re[k] + im[k] * im[k] }
        val peakBin = magnitudes.indices.maxBy { magnitudes[it] }
        assertThat(peakBin).isEqualTo(bin)
    }

    @Test(expected = IllegalArgumentException::class)
    fun transform_nonPowerOfTwo_throws() {
        Fft.transform(FloatArray(1000), FloatArray(1000))
    }
}
