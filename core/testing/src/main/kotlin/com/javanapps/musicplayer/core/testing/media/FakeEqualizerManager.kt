package com.javanapps.musicplayer.core.testing.media

import com.javanapps.musicplayer.core.domain.equalizer.EqualizerManager
import com.javanapps.musicplayer.core.domain.equalizer.EqualizerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeEqualizerManager : EqualizerManager {
    private val _state = MutableStateFlow(EqualizerState())
    override val state: StateFlow<EqualizerState> = _state.asStateFlow()

    override fun initialize(audioSessionId: Int) {
        _state.update { it.copy(initialized = true, isSupported = true) }
    }

    override fun setEnabled(enabled: Boolean) {
        _state.update { it.copy(enabled = enabled) }
    }

    override fun setBandLevel(
        band: Int,
        level: Int,
    ) {
        _state.update { state ->
            val newLevels = state.bandLevels.toMutableList().also { it[band] = level }
            state.copy(bandLevels = newLevels)
        }
    }

    override fun applyPreset(presetIndex: Int) {
        _state.update { it.copy(currentPreset = presetIndex) }
    }

    override fun release() {
        _state.update { it.copy(initialized = false) }
    }
}
