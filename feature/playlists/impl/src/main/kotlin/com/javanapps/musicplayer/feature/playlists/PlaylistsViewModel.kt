package com.javanapps.musicplayer.feature.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.musicplayer.core.domain.repository.AnalysisRepository
import com.javanapps.musicplayer.core.domain.repository.PlaylistRepository
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.core.model.Playlist
import com.javanapps.musicplayer.core.model.SmartPlaylist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel
    @Inject
    constructor(
        private val playlistRepository: PlaylistRepository,
        private val songsRepository: SongsRepository,
        analysisRepository: AnalysisRepository,
    ) : ViewModel() {
        val uiState: StateFlow<PlaylistsUiState> =
            combine(
                playlistRepository.getPlaylists(),
                analysisRepository.observeSmartPlaylists(),
                songsRepository.getSongs(),
            ) { playlists, smartPlaylists, songs ->
                val filtered = smartPlaylists.filter { it.label in ALLOWED_LABELS }
                PlaylistsUiState.Success(playlists, filtered, songs.isEmpty())
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PlaylistsUiState.Loading,
            )

        fun createPlaylist(name: String) {
            viewModelScope.launch {
                playlistRepository.createPlaylist(name)
            }
        }

        fun deletePlaylist(id: Long) {
            viewModelScope.launch {
                playlistRepository.deletePlaylist(id)
            }
        }

        fun renamePlaylist(
            id: Long,
            name: String,
        ) {
            viewModelScope.launch {
                playlistRepository.renamePlaylist(id, name)
            }
        }

        fun scan() {
            viewModelScope.launch {
                songsRepository.refresh()
            }
        }
    }

private val ALLOWED_LABELS = setOf("80s", "90s", "Acoustic", "Classical", "Dance", "Energetic", "Rock", "Pop")

sealed interface PlaylistsUiState {
    data object Loading : PlaylistsUiState

    data class Success(
        val playlists: List<Playlist>,
        val smartPlaylists: List<SmartPlaylist>,
        val noSongs: Boolean,
    ) : PlaylistsUiState
}
