package com.javanapps.musicplayer.core.analysis.dsp

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class MelSpectrogramTest {
    private val melSpectrogram = MelSpectrogram()
    private val targetSampleRate = 16_000
    private val melBands = 96

    @Test
    fun logMel_sine_producesFramesOfMelBands() {
        val samples = sine(frequency = 1_000.0, sampleRate = targetSampleRate, seconds = 1)

        val frames = melSpectrogram.logMel(samples, targetSampleRate)

        assertThat(frames).isNotEmpty()
        assertThat(frames.first()).hasLength(melBands)
    }

    @Test
    fun logMel_sine_energyConcentratedAwayFromDc() {
        val samples = sine(frequency = 1_000.0, sampleRate = targetSampleRate, seconds = 1)

        val frame = melSpectrogram.logMel(samples, targetSampleRate).first()
        val peakBand = frame.indices.maxBy { frame[it] }

        assertThat(peakBand).isGreaterThan(0)
        assertThat(peakBand).isLessThan(melBands - 1)
    }

    @Test
    fun logMel_resamplesHigherRate_withoutError() {
        val samples = sine(frequency = 1_000.0, sampleRate = 44_100, seconds = 1)

        val frames = melSpectrogram.logMel(samples, 44_100)

        assertThat(frames).isNotEmpty()
        assertThat(frames.first()).hasLength(melBands)
    }

    @Test
    fun logMel_tooShort_returnsEmpty() {
        val frames = melSpectrogram.logMel(FloatArray(128), targetSampleRate)

        assertThat(frames).isEmpty()
    }

    private fun sine(
        frequency: Double,
        sampleRate: Int,
        seconds: Int,
    ): FloatArray = FloatArray(sampleRate * seconds) { sin(2.0 * PI * frequency * it / sampleRate).toFloat() }
}
