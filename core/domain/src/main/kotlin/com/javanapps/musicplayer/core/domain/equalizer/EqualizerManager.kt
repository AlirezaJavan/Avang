package com.javanapps.musicplayer.core.domain.equalizer

import kotlinx.coroutines.flow.StateFlow

interface EqualizerManager {
    val state: StateFlow<EqualizerState>

    fun initialize(audioSessionId: Int)

    fun setEnabled(enabled: Boolean)

    fun setBandLevel(
        band: Int,
        level: Int,
    )

    fun applyPreset(presetIndex: Int)

    fun release()
}

data class EqualizerState(
    val enabled: Boolean = false,
    val initialized: Boolean = false,
    val isSupported: Boolean = false,
    val bandCount: Int = 5,
    val levelRangeMin: Int = -1500,
    val levelRangeMax: Int = 1500,
    val bandLevels: List<Int> = List(5) { 0 },
    val centerFrequencies: List<Int> = emptyList(),
    val presets: List<String> = emptyList(),
    val currentPreset: Int = -1,
)
