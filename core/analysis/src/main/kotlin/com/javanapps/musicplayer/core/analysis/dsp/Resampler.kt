package com.javanapps.musicplayer.core.analysis.dsp

import kotlin.math.roundToInt

object Resampler {
    fun resample(
        samples: FloatArray,
        sourceRate: Int,
        targetRate: Int,
    ): FloatArray {
        if (sourceRate == targetRate) return samples
        val ratio = targetRate.toDouble() / sourceRate
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
}
