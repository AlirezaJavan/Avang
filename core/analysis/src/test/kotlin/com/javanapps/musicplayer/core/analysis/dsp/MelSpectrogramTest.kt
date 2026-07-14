package com.javanapps.musicplayer.core.analysis.dsp

import com.google.common.truth.Truth.assertThat
import org.junit.Test

// Cross-validates the Kotlin mel-spectrogram pipeline against a numpy/librosa reference
// (see scratchpad conversion script): same Slaney filterbank formulas, same frame/hop/window,
// same log10(1+10000x) compression, run on a fixed seeded-random test signal. A mismatch here
// means genre predictions downstream are effectively noise, so the tolerance is tight.
class MelSpectrogramTest {
    // The mel filterbank isn't exposed on its own, so this exercises it indirectly via
    // melFrames: the reference signal is short-window random noise, so a filterbank warping or
    // normalization bug (wrong mel spacing, peak- instead of area-normalized filters) would
    // still show up as a per-band mismatch against the numpy/librosa reference here.
    @Test
    fun melFrames_matchesNumpyReference() {
        val signal = readCsv("melspectrogram/test_signal.csv").map { it[0].toFloat() }.toFloatArray()
        val expected = readCsv("melspectrogram/expected_mel_frames.csv")

        val actual = MelSpectrogram.melFrames(signal)

        assertThat(actual.size).isEqualTo(expected.size)
        for (frame in expected.indices) {
            for (band in expected[frame].indices) {
                assertThat(actual[frame][band].toDouble()).isWithin(1e-4).of(expected[frame][band])
            }
        }
    }

    @Test
    fun patches_stacksFramesWithExpectedHop() {
        val frameCount = MelSpectrogram.PATCH_SIZE + MelSpectrogram.PATCH_HOP * 2
        val samples = FloatArray(MelSpectrogram.HOP_SIZE * (frameCount - 1) + MelSpectrogram.FRAME_SIZE)

        val patches = MelSpectrogram.patches(samples)

        assertThat(patches).hasSize(3)
        assertThat(patches[0]).hasLength(MelSpectrogram.PATCH_SIZE)
        assertThat(patches[0][0]).hasLength(MelSpectrogram.NUM_BANDS)
    }

    @Test
    fun patches_shorterThanOnePatch_returnsEmpty() {
        val samples = FloatArray(MelSpectrogram.FRAME_SIZE * 2)

        val patches = MelSpectrogram.patches(samples)

        assertThat(patches).isEmpty()
    }

    private fun readCsv(resourcePath: String): List<List<Double>> {
        val stream =
            requireNotNull(javaClass.classLoader?.getResourceAsStream(resourcePath)) {
                "Missing test resource $resourcePath"
            }
        return stream.bufferedReader().useLines { lines ->
            lines.filter { it.isNotBlank() }.map { line -> line.split(",").map { it.toDouble() } }.toList()
        }
    }
}
