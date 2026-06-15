package com.javanapps.musicplayer.feature.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.javanapps.musicplayer.core.domain.controller.PlayerController
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.feature.library.navigation.AlbumDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        songsRepository: SongsRepository,
        private val playerController: PlayerController,
    ) : ViewModel() {
        private val route = savedStateHandle.toRoute<AlbumDetailRoute>()
        val albumId = route.albumId

        val uiState: StateFlow<AlbumDetailUiState> =
            songsRepository
                .getSongsByAlbum(albumId)
                .map { songs ->
                    if (songs.isEmpty()) AlbumDetailUiState.Error else AlbumDetailUiState.Success(songs)
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = AlbumDetailUiState.Loading,
                )

        fun play(mediaId: String) {
            val state = uiState.value
            if (state is AlbumDetailUiState.Success) {
                val index = state.songs.indexOfFirst { it.mediaId == mediaId }
                if (index != -1) {
                    playerController.play(state.songs, index)
                }
            }
        }
    }

sealed interface AlbumDetailUiState {
    data object Loading : AlbumDetailUiState

    data class Success(
        val songs: List<Song>,
    ) : AlbumDetailUiState

    data object Error : AlbumDetailUiState
}
