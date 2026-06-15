package com.javanapps.musicplayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.musicplayer.core.domain.controller.PlayerController
import com.javanapps.musicplayer.core.domain.repository.UserData
import com.javanapps.musicplayer.core.domain.repository.UserDataRepository
import com.javanapps.musicplayer.core.model.DarkThemeConfig
import com.javanapps.musicplayer.core.model.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val userDataRepository: UserDataRepository,
        private val playerController: PlayerController,
    ) : ViewModel() {
        val uiState: StateFlow<MainUiState> =
            userDataRepository.userData
                .map { MainUiState.Success(it) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = MainUiState.Loading,
                )

        val playerState: StateFlow<PlayerState> = playerController.playerState

        fun pause() = playerController.pause()

        fun resume() = playerController.resume()

        fun skipToNext() = playerController.skipToNext()

        fun skipToPrevious() = playerController.skipToPrevious()
    }

sealed interface MainUiState {
    data object Loading : MainUiState

    data class Success(
        val userData: UserData,
    ) : MainUiState {
        fun shouldUseDarkTheme(isSystemDarkTheme: Boolean): Boolean =
            when (userData.darkThemeConfig) {
                DarkThemeConfig.FOLLOW_SYSTEM -> isSystemDarkTheme
                DarkThemeConfig.LIGHT -> false
                DarkThemeConfig.DARK -> true
            }

        val shouldUseDynamicColor: Boolean get() = userData.dynamicColor
    }

    fun shouldKeepSplashScreen() = this is Loading
}
