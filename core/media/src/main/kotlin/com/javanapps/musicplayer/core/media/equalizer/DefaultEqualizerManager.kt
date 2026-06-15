package com.javanapps.musicplayer.core.media.equalizer

import com.javanapps.musicplayer.core.common.dispatcher.di.ApplicationScope
import com.javanapps.musicplayer.core.domain.equalizer.EqualizerManager
import com.javanapps.musicplayer.core.domain.equalizer.EqualizerState
import com.javanapps.musicplayer.core.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultEqualizerManager
    @Inject
    constructor(
        @ApplicationScope private val appScope: CoroutineScope,
        private val userDataRepository: UserDataRepository,
    ) : EqualizerManager {
        private var equalizer: android.media.audiofx.Equalizer? = null

        private val _state = MutableStateFlow(EqualizerState())
        override val state: StateFlow<EqualizerState> = _state.asStateFlow()

        override fun initialize(audioSessionId: Int) {
            if (audioSessionId == 0) return
            try {
                equalizer?.release()
                val eq = android.media.audiofx.Equalizer(0, audioSessionId)
                equalizer = eq

                val numBands = eq.numberOfBands.toInt()
                val numPresets = eq.numberOfPresets.toInt()
                val range = eq.bandLevelRange
                val presets = (0 until numPresets).map { eq.getPresetName(it.toShort()) }
                val centerFrequencies =
                    (0 until numBands).map { i ->
                        val freqRange = eq.getBandFreqRange(i.toShort())
                        if (freqRange != null && freqRange.size >= 2) (freqRange[0] + freqRange[1]) / 2 else 0
                    }

                appScope.launch {
                    val userData = userDataRepository.userData.first()

                    val savedLevels = userData.equalizerBandLevels
                    val levels =
                        if (savedLevels.size == numBands) {
                            savedLevels.forEachIndexed { i, level -> eq.setBandLevel(i.toShort(), level) }
                            savedLevels.map { it.toInt() }
                        } else {
                            (0 until numBands).map { i -> eq.getBandLevel(i.toShort()).toInt() }
                        }

                    eq.enabled = userData.equalizerEnabled

                    _state.update {
                        EqualizerState(
                            enabled = userData.equalizerEnabled,
                            initialized = true,
                            isSupported = true,
                            bandCount = numBands,
                            levelRangeMin = range[0].toInt(),
                            levelRangeMax = range[1].toInt(),
                            bandLevels = levels,
                            centerFrequencies = centerFrequencies,
                            presets = presets,
                            currentPreset = userData.equalizerPreset,
                        )
                    }
                }
            } catch (_: Exception) {
                _state.update { it.copy(initialized = false, isSupported = false) }
            }
        }

        override fun setEnabled(enabled: Boolean) {
            equalizer?.enabled = enabled
            _state.update { it.copy(enabled = enabled) }
        }

        override fun setBandLevel(
            band: Int,
            level: Int,
        ) {
            equalizer?.setBandLevel(band.toShort(), level.toShort())
            _state.update { state ->
                val newLevels =
                    state.bandLevels.toMutableList().also {
                        if (band < it.size) it[band] = level
                    }
                state.copy(bandLevels = newLevels, currentPreset = -1)
            }
        }

        override fun applyPreset(presetIndex: Int) {
            val eq = equalizer ?: return
            eq.usePreset(presetIndex.toShort())
            val numBands = eq.numberOfBands.toInt()
            val levels = (0 until numBands).map { eq.getBandLevel(it.toShort()).toInt() }
            _state.update { it.copy(bandLevels = levels, currentPreset = presetIndex) }
        }

        override fun release() {
            equalizer?.release()
            equalizer = null
        }
    }
