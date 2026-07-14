package com.javanapps.musicplayer.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.musicplayer.core.common.dispatcher.Dispatcher
import com.javanapps.musicplayer.core.common.dispatcher.MusicPlayerDispatchers.Default
import com.javanapps.musicplayer.core.domain.controller.PlayerController
import com.javanapps.musicplayer.core.domain.repository.FavoritesRepository
import com.javanapps.musicplayer.core.domain.repository.NotesRepository
import com.javanapps.musicplayer.core.domain.repository.PlaylistRepository
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.core.model.Album
import com.javanapps.musicplayer.core.model.Artist
import com.javanapps.musicplayer.core.model.Playlist
import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.core.model.SongNote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SEARCH_DEBOUNCE_MS = 300L

@HiltViewModel
class LibraryViewModel
    @Inject
    constructor(
        private val songsRepository: SongsRepository,
        private val favoritesRepository: FavoritesRepository,
        private val playlistRepository: PlaylistRepository,
        private val playerController: PlayerController,
        private val notesRepository: NotesRepository,
        @Dispatcher(Default) private val defaultDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _searchQuery = MutableStateFlow("")
        val searchQuery = _searchQuery.asStateFlow()

        private val _sortOrder = MutableStateFlow(SortOrder.TITLE)
        val sortOrder = _sortOrder.asStateFlow()

        val playlists: StateFlow<List<Playlist>> =
            playlistRepository
                .getPlaylists()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = emptyList(),
                )

        val favoriteSongIds: StateFlow<Set<Long>> =
            favoritesRepository
                .getFavoriteSongIds()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = emptySet(),
                )

        private val selectedSongIdFlow = MutableStateFlow<Long?>(null)

        @OptIn(ExperimentalCoroutinesApi::class)
        val noteForSelectedSong: StateFlow<SongNote?> =
            selectedSongIdFlow
                .flatMapLatest { songId ->
                    if (songId != null) notesRepository.getNote(songId) else flowOf(null)
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = null,
                )

        fun selectSongForNote(songId: Long?) {
            selectedSongIdFlow.value = songId
        }

        val uiState: StateFlow<LibraryUiState> =
            combine(
                songsRepository.getSongs(),
                songsRepository.getAlbums(),
                songsRepository.getArtists(),
                // Empty query (including the initial value) is not debounced, so the screen
                // isn't held on Loading and clearing the search filters instantly.
                _searchQuery.debounce { query -> if (query.isEmpty()) 0L else SEARCH_DEBOUNCE_MS },
                _sortOrder,
            ) { songs, albums, artists, query, sort ->
                val filteredSongs =
                    songs
                        .asSequence()
                        .filter {
                            it.title.contains(query, ignoreCase = true) ||
                                it.artist.contains(query, ignoreCase = true)
                        }.sortedWith(
                            compareBy {
                                when (sort) {
                                    SortOrder.TITLE -> it.title.lowercase()
                                    SortOrder.ARTIST -> it.artist.lowercase()
                                    SortOrder.DATE_ADDED -> it.dateAdded.toString()
                                    SortOrder.DURATION -> it.duration.toString()
                                }
                            },
                        ).toList()

                LibraryUiState.Success(
                    songs = filteredSongs,
                    albums = albums.filter { it.title.contains(query, ignoreCase = true) },
                    artists = artists.filter { it.name.contains(query, ignoreCase = true) },
                )
            }.flowOn(defaultDispatcher)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = LibraryUiState.Loading,
                )

        fun onSearchQueryChanged(query: String) {
            _searchQuery.value = query
        }

        fun onSortOrderChanged(sortOrder: SortOrder) {
            _sortOrder.value = sortOrder
        }

        fun play(mediaId: String) {
            val state = uiState.value
            if (state is LibraryUiState.Success) {
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

        fun playNext(song: Song) {
            playerController.addToQueueNext(song)
        }

        fun addToQueue(song: Song) {
            playerController.addToQueueLast(song)
        }

        fun addToPlaylist(
            playlistId: Long,
            songId: Long,
        ) {
            viewModelScope.launch {
                playlistRepository.addSongToPlaylist(playlistId, songId)
            }
        }

        fun saveNote(
            songId: Long,
            content: String,
        ) {
            viewModelScope.launch {
                notesRepository.saveNote(songId, content)
            }
        }

        fun refresh() {
            viewModelScope.launch {
                songsRepository.refresh()
            }
        }
    }

enum class SortOrder {
    TITLE,
    ARTIST,
    DATE_ADDED,
    DURATION,
}

sealed interface LibraryUiState {
    data object Loading : LibraryUiState

    data class Success(
        val songs: List<Song>,
        val albums: List<Album>,
        val artists: List<Artist>,
    ) : LibraryUiState
}
