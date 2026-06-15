package com.javanapps.musicplayer.feature.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.musicplayer.core.domain.equalizer.EqualizerManager
import com.javanapps.musicplayer.core.domain.equalizer.EqualizerState
import com.javanapps.musicplayer.core.domain.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EqualizerViewModel
    @Inject
    constructor(
        private val equalizerManager: EqualizerManager,
        private val userDataRepository: UserDataRepository,
    ) : ViewModel() {
        val uiState: StateFlow<EqualizerState> = equalizerManager.state

        fun setEnabled(enabled: Boolean) {
            equalizerManager.setEnabled(enabled)
            viewModelScope.launch { userDataRepository.setEqualizerEnabled(enabled) }
        }

        fun setBandLevel(
            band: Int,
            level: Int,
        ) {
            equalizerManager.setBandLevel(band, level)
            viewModelScope.launch {
                userDataRepository.setEqualizerBandLevels(
                    equalizerManager.state.value.bandLevels
                        .map { it.toShort() },
                )
                userDataRepository.setEqualizerPreset(-1)
            }
        }

        fun applyPreset(presetIndex: Int) {
            equalizerManager.applyPreset(presetIndex)
            viewModelScope.launch {
                val levels =
                    equalizerManager.state.value.bandLevels
                        .map { it.toShort() }
                userDataRepository.setEqualizerPreset(presetIndex)
                userDataRepository.setEqualizerBandLevels(levels)
            }
        }
    }
