package com.javanapps.musicplayer.core.analysis.dsp

import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

// Reproduces the mel-spectrogram preprocessing that Essentia's TensorflowPredictEffnetDiscogs
// pipeline expects (frameSize=512, hopSize=256, 96 Slaney-warped mel bands, area-normalized
// triangular filters, log10(1+10000x) compression). Matching this exactly matters: the
// discogs-effnet embedding model was trained on this exact feature representation, and any
// deviation (HTK vs Slaney mel warping, peak- vs area-normalized filters, wrong compression
// constant) silently degrades every downstream genre prediction without throwing an error.
object MelSpectrogram {
    const val SAMPLE_RATE = 16_000
    const val FRAME_SIZE = 512
    const val HOP_SIZE = 256
    const val NUM_BANDS = 96
    const val PATCH_SIZE = 128
    const val PATCH_HOP = 62

    private const val MIN_FREQUENCY_HZ = 0.0
    private const val MAX_FREQUENCY_HZ = 8_000.0
    private const val LOG_COMPRESSION_SCALE = 10_000.0

    private val hannWindow: FloatArray by lazy { hannWindow(FRAME_SIZE) }
    private val filterbank: Array<FloatArray> by lazy {
        melFilterbank(SAMPLE_RATE, FRAME_SIZE, NUM_BANDS, MIN_FREQUENCY_HZ, MAX_FREQUENCY_HZ)
    }

    // One row per STFT frame, NUM_BANDS log-mel-energy values per row.
    fun melFrames(samples: FloatArray): Array<FloatArray> {
        val frameCount = if (samples.size < FRAME_SIZE) 0 else (samples.size - FRAME_SIZE) / HOP_SIZE + 1
        return Array(frameCount) { frameIndex ->
            val start = frameIndex * HOP_SIZE
            melFrame(samples, start)
        }
    }

    // Stacks consecutive mel-frames into fixed-size [PATCH_SIZE x NUM_BANDS] patches, sliding by
    // PATCH_HOP frames between patches (~50% overlap), matching TensorflowPredictEffnetDiscogs's
    // default patch windowing. Clips shorter than one patch produce no patches.
    fun patches(samples: FloatArray): List<Array<FloatArray>> {
        val frames = melFrames(samples)
        if (frames.size < PATCH_SIZE) return emptyList()
        val patches = mutableListOf<Array<FloatArray>>()
        var start = 0
        while (start + PATCH_SIZE <= frames.size) {
            patches += Array(PATCH_SIZE) { frames[start + it] }
            start += PATCH_HOP
        }
        return patches
    }

    private fun melFrame(
        samples: FloatArray,
        start: Int,
    ): FloatArray {
        val re = FloatArray(FRAME_SIZE)
        val im = FloatArray(FRAME_SIZE)
        for (i in 0 until FRAME_SIZE) {
            re[i] = samples[start + i] * hannWindow[i]
        }
        Fft.transform(re, im)
        val bins = FRAME_SIZE / 2 + 1
        val power = FloatArray(bins) { re[it] * re[it] + im[it] * im[it] }
        return FloatArray(NUM_BANDS) { band ->
            var energy = 0f
            val weights = filterbank[band]
            for (bin in 0 until bins) energy += weights[bin] * power[bin]
            log10(1.0 + LOG_COMPRESSION_SCALE * energy).toFloat()
        }
    }

    // Plain (non-normalized) textbook Hann window, symmetric: w[n] = 0.5 - 0.5*cos(2*pi*n/(N-1)).
    private fun hannWindow(size: Int): FloatArray =
        FloatArray(size) { n ->
            (0.5 - 0.5 * cos(2.0 * Math.PI * n / (size - 1))).toFloat()
        }

    // Builds a Slaney-style mel filterbank identical to librosa.filters.mel(htk=False,
    // norm='slaney'): mel-spaced center frequencies via the Slaney hz<->mel formulas, triangular
    // filters over linear FFT bin frequencies, each filter's area normalized to a constant
    // (peak height = 2 / bandwidth) rather than peak-normalized to 1.
    private fun melFilterbank(
        sampleRate: Int,
        fftSize: Int,
        numBands: Int,
        minFrequencyHz: Double,
        maxFrequencyHz: Double,
    ): Array<FloatArray> {
        val bins = fftSize / 2 + 1
        val fftFrequencies = DoubleArray(bins) { it * sampleRate.toDouble() / fftSize }

        val melMin = hzToMel(minFrequencyHz)
        val melMax = hzToMel(maxFrequencyHz)
        val melPoints = DoubleArray(numBands + 2) { melMin + (melMax - melMin) * it / (numBands + 1) }
        val hzPoints = DoubleArray(numBands + 2) { melToHz(melPoints[it]) }

        return Array(numBands) { band ->
            val lowerHz = hzPoints[band]
            val centerHz = hzPoints[band + 1]
            val upperHz = hzPoints[band + 2]
            val lowerSlope = centerHz - lowerHz
            val upperSlope = upperHz - centerHz
            val enorm = 2.0 / (upperHz - lowerHz)
            FloatArray(bins) { bin ->
                val freq = fftFrequencies[bin]
                val lower = (freq - lowerHz) / lowerSlope
                val upper = (upperHz - freq) / upperSlope
                (max(0.0, min(lower, upper)) * enorm).toFloat()
            }
        }
    }

    // Slaney's auditory toolbox mel scale: linear below 1kHz, logarithmic above.
    private const val MIN_LOG_HZ = 1000.0
    private const val MIN_LOG_MEL = MIN_LOG_HZ / (200.0 / 3.0)
    private val LOG_STEP = ln(6.4) / 27.0

    private fun hzToMel(hz: Double): Double =
        if (hz < MIN_LOG_HZ) {
            hz / (200.0 / 3.0)
        } else {
            MIN_LOG_MEL + ln(hz / MIN_LOG_HZ) / LOG_STEP
        }

    private fun melToHz(mel: Double): Double =
        if (mel < MIN_LOG_MEL) {
            mel * (200.0 / 3.0)
        } else {
            MIN_LOG_HZ * exp(LOG_STEP * (mel - MIN_LOG_MEL))
        }
}
