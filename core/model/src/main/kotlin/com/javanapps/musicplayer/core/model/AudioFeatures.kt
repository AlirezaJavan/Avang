package com.javanapps.musicplayer.core.model

data class AudioFeatures(
    val durationMs: Long,
    val sampleRate: Int,
    val rmsEnergy: Float,
    val zeroCrossingRate: Float,
    val spectralCentroid: Float,
    val spectralRolloff: Float,
    val spectralFlux: Float,
    val highFreqRatio: Float,
    val estimatedTempoBpm: Float,
    val beatStrength: Float,
)
