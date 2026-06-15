package com.javanapps.musicplayer.feature.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.javanapps.musicplayer.core.domain.controller.PlayerController
import com.javanapps.musicplayer.core.domain.repository.PlaylistRepository
import com.javanapps.musicplayer.core.model.Playlist
import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.feature.playlists.navigation.PlaylistDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val playlistRepository: PlaylistRepository,
        private val playerController: PlayerController,
    ) : ViewModel() {
        private val route = savedStateHandle.toRoute<PlaylistDetailRoute>()
        val playlistId = route.playlistId

        val uiState: StateFlow<PlaylistDetailUiState> =
            combine(
                playlistRepository.getPlaylist(playlistId),
                playlistRepository.getSongsInPlaylist(playlistId),
            ) { playlist, songs ->
                if (playlist == null) {
                    PlaylistDetailUiState.Error
                } else {
                    PlaylistDetailUiState.Success(playlist, songs)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PlaylistDetailUiState.Loading,
            )

        fun play(mediaId: String) {
            val state = uiState.value
            if (state is PlaylistDetailUiState.Success) {
                val index = state.songs.indexOfFirst { it.mediaId == mediaId }
                if (index != -1) {
                    playerController.play(state.songs, index)
                }
            }
        }

        fun removeSong(songId: Long) {
            viewModelScope.launch {
                playlistRepository.removeSongFromPlaylist(playlistId, songId)
            }
        }
    }

sealed interface PlaylistDetailUiState {
    data object Loading : PlaylistDetailUiState

    data class Success(
        val playlist: Playlist,
        val songs: List<Song>,
    ) : PlaylistDetailUiState

    data object Error : PlaylistDetailUiState
}
