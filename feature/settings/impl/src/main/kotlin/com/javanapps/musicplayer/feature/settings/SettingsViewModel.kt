package com.javanapps.musicplayer.feature.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.musicplayer.core.domain.repository.UserData
import com.javanapps.musicplayer.core.domain.repository.UserDataRepository
import com.javanapps.musicplayer.core.model.DarkThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val userDataRepository: UserDataRepository,
    ) : ViewModel() {
        val userData: StateFlow<UserData?> =
            userDataRepository.userData
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = null,
                )

        fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
            viewModelScope.launch {
                userDataRepository.setDarkThemeConfig(darkThemeConfig)
            }
        }

        fun setDynamicColor(dynamicColor: Boolean) {
            viewModelScope.launch {
                userDataRepository.setDynamicColor(dynamicColor)
            }
        }

        fun setLanguage(language: String) {
            viewModelScope.launch {
                userDataRepository.setLanguage(language)
            }
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
        }
    }
