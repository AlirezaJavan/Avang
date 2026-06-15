package com.javanapps.musicplayer.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.musicplayer.core.domain.controller.PlayerController
import com.javanapps.musicplayer.core.domain.repository.FavoritesRepository
import com.javanapps.musicplayer.core.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel
    @Inject
    constructor(
        private val favoritesRepository: FavoritesRepository,
        private val playerController: PlayerController,
    ) : ViewModel() {
        val uiState: StateFlow<FavoritesUiState> =
            favoritesRepository
                .getFavoriteSongs()
                .map { songs ->
                    FavoritesUiState.Success(songs)
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = FavoritesUiState.Loading,
                )

        fun play(mediaId: String) {
            val state = uiState.value
            if (state is FavoritesUiState.Success) {
                val index = state.songs.indexOfFirst { it.mediaId == mediaId }
                if (index != -1) {
                    playerController.play(state.songs, index)
                }
            }
        }

        fun toggleFavorite(songId: Long) {
            viewModelScope.launch {
                favoritesRepository.toggleFavorite(songId)
            }
        }
    }

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState

    data class Success(
        val songs: List<Song>,
    ) : FavoritesUiState
}
