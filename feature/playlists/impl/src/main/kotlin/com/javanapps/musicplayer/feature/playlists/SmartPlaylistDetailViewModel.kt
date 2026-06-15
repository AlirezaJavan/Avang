package com.javanapps.musicplayer.feature.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.javanapps.musicplayer.core.domain.controller.PlayerController
import com.javanapps.musicplayer.core.domain.repository.AnalysisRepository
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.feature.playlists.navigation.SmartPlaylistDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SmartPlaylistDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        analysisRepository: AnalysisRepository,
        songsRepository: SongsRepository,
        private val playerController: PlayerController,
    ) : ViewModel() {
        val label = savedStateHandle.toRoute<SmartPlaylistDetailRoute>().label

        val uiState: StateFlow<SmartPlaylistDetailUiState> =
            combine(
                analysisRepository.observeSongIdsForLabel(label),
                songsRepository.getSongs(),
            ) { songIds, songs ->
                val idSet = songIds.toSet()
                SmartPlaylistDetailUiState.Success(label, songs.filter { idSet.contains(it.id) })
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SmartPlaylistDetailUiState.Loading,
            )

        fun play(mediaId: String) {
            val state = uiState.value
            if (state is SmartPlaylistDetailUiState.Success) {
                val index = state.songs.indexOfFirst { it.mediaId == mediaId }
                if (index != -1) playerController.play(state.songs, index)
            }
        }
    }

sealed interface SmartPlaylistDetailUiState {
    data object Loading : SmartPlaylistDetailUiState

    data class Success(
        val label: String,
        val songs: List<Song>,
    ) : SmartPlaylistDetailUiState
}
