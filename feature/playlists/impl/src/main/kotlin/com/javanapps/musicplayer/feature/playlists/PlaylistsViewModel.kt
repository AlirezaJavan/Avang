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
        private val analysisRepository: AnalysisRepository,
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

        /** Deletes every auto playlist and re-analyzes the whole library from scratch. */
        fun retryAnalysis() {
            viewModelScope.launch {
                analysisRepository.rescanAll()
            }
        }
    }

// Mirrors core:analysis's LabelMap genre display names (not depended on directly, since that
// module pulls in TFLite/Android decoding internals this feature module has no other need for).
private val ALLOWED_LABELS =
    setOf(
        "Pop",
        "Hip-Hop",
        "Rock",
        "Metal",
        "Punk",
        "Grunge",
        "Progressive Rock",
        "Rock and Roll",
        "Psychedelic Rock",
        "R&B",
        "Soul",
        "Reggae",
        "Country",
        "Swing",
        "Bluegrass",
        "Funk",
        "Folk",
        "Jazz",
        "Disco",
        "Classical",
        "Opera",
        "Electronic",
        "House",
        "Techno",
        "Dubstep",
        "Drum and Bass",
        "EDM",
        "Ambient",
        "Trance",
        "Salsa",
        "Flamenco",
        "Blues",
        "New Age",
        "Afrobeat",
        "Christian",
        "Gospel",
        "Bollywood",
        "Ska",
        "Christmas",
        "Dance",
    )

sealed interface PlaylistsUiState {
    data object Loading : PlaylistsUiState

    data class Success(
        val playlists: List<Playlist>,
        val smartPlaylists: List<SmartPlaylist>,
        val noSongs: Boolean,
    ) : PlaylistsUiState
}
