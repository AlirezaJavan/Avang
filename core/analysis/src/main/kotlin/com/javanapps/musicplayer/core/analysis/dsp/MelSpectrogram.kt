package com.javanapps.musicplayer.core.analysis.dsp

import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

class MelSpectrogram
    @Inject
    constructor() {
        private val window: FloatArray =
            FloatArray(FFT_SIZE) { 0.5f - 0.5f * cos(2.0 * PI * it / (FFT_SIZE - 1)).toFloat() }

        private val filterbank: Array<FloatArray> = buildFilterbank()

        fun logMel(
            samples: FloatArray,
            sampleRate: Int,
        ): Array<FloatArray> {
            val resampled = resampleToTarget(samples, sampleRate)
            if (resampled.size < FFT_SIZE) return emptyArray()
            val frames = ArrayList<FloatArray>()
            var position = 0
            while (position + FFT_SIZE <= resampled.size) {
                frames += melFrame(powerSpectrum(resampled, position))
                position += HOP
            }
            return frames.toTypedArray()
        }

        private fun powerSpectrum(
            samples: FloatArray,
            offset: Int,
        ): FloatArray {
            val re = FloatArray(FFT_SIZE)
            val im = FloatArray(FFT_SIZE)
            for (i in 0 until FFT_SIZE) re[i] = samples[offset + i] * window[i]
            Fft.transform(re, im)
            val half = FFT_SIZE / 2
            return FloatArray(half) { k -> re[k] * re[k] + im[k] * im[k] }
        }

        private fun melFrame(power: FloatArray): FloatArray =
            FloatArray(MEL_BANDS) { band ->
                var sum = 0f
                val filter = filterbank[band]
                for (k in power.indices) sum += power[k] * filter[k]
                ln(1f + sum)
            }

        private fun resampleToTarget(
            samples: FloatArray,
            sampleRate: Int,
        ): FloatArray {
            if (sampleRate == TARGET_SAMPLE_RATE) return samples
            val ratio = TARGET_SAMPLE_RATE.toDouble() / sampleRate
            val outSize = (samples.size * ratio).roundToInt()
            return FloatArray(outSize) { i ->
                val source = i / ratio
                val index = source.toInt()
                if (index >= samples.size - 1) {
                    samples[samples.size - 1]
                } else {
                    val frac = (source - index).toFloat()
                    samples[index] * (1f - frac) + samples[index + 1] * frac
                }
            }
        }

        private fun buildFilterbank(): Array<FloatArray> {
            val half = FFT_SIZE / 2
            val maxMel = hzToMel(TARGET_SAMPLE_RATE / 2.0)
            val points = DoubleArray(MEL_BANDS + 2) { melToHz(maxMel * it / (MEL_BANDS + 1)) }
            val bins = points.map { (it * FFT_SIZE / TARGET_SAMPLE_RATE).toInt().coerceIn(0, half - 1) }
            return Array(MEL_BANDS) { band -> triangle(bins[band], bins[band + 1], bins[band + 2], half) }
        }

        private fun triangle(
            lower: Int,
            center: Int,
            upper: Int,
            size: Int,
        ): FloatArray {
            val filter = FloatArray(size)
            for (k in lower until center) if (center > lower) filter[k] = (k - lower).toFloat() / (center - lower)
            for (k in center until upper) if (upper > center) filter[k] = (upper - k).toFloat() / (upper - center)
            return filter
        }

        private fun hzToMel(hz: Double): Double = MEL_SCALE * ln(1.0 + hz / MEL_BREAK)

        private fun melToHz(mel: Double): Double = MEL_BREAK * (Math.E.pow(mel / MEL_SCALE) - 1.0)

        private companion object {
            const val TARGET_SAMPLE_RATE = 16_000
            const val FFT_SIZE = 512
            const val HOP = 256
            const val MEL_BANDS = 96
            const val MEL_SCALE = 1127.0
            const val MEL_BREAK = 700.0
        }
    }
