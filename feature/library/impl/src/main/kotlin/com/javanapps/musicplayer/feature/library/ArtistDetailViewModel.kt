package com.javanapps.musicplayer.feature.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.javanapps.musicplayer.core.domain.controller.PlayerController
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.feature.library.navigation.ArtistDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        songsRepository: SongsRepository,
        private val playerController: PlayerController,
    ) : ViewModel() {
        private val route = savedStateHandle.toRoute<ArtistDetailRoute>()
        val artistId = route.artistId

        val uiState: StateFlow<ArtistDetailUiState> =
            songsRepository
                .getSongsByArtist(artistId)
                .map { songs ->
                    if (songs.isEmpty()) ArtistDetailUiState.Error else ArtistDetailUiState.Success(songs)
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = ArtistDetailUiState.Loading,
                )

        fun play(mediaId: String) {
            val state = uiState.value
            if (state is ArtistDetailUiState.Success) {
                val index = state.songs.indexOfFirst { it.mediaId == mediaId }
                if (index != -1) {
                    playerController.play(state.songs, index)
                }
            }
        }
    }

sealed interface ArtistDetailUiState {
    data object Loading : ArtistDetailUiState

    data class Success(
        val songs: List<Song>,
    ) : ArtistDetailUiState

    data object Error : ArtistDetailUiState
}
