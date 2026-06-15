package com.javanapps.musicplayer.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.musicplayer.core.domain.controller.PlayerController
import com.javanapps.musicplayer.core.domain.equalizer.EqualizerManager
import com.javanapps.musicplayer.core.domain.repository.FavoritesRepository
import com.javanapps.musicplayer.core.domain.repository.NotesRepository
import com.javanapps.musicplayer.core.model.PlayerState
import com.javanapps.musicplayer.core.model.RepeatMode
import com.javanapps.musicplayer.core.model.SongNote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel
    @Inject
    constructor(
        private val playerController: PlayerController,
        private val favoritesRepository: FavoritesRepository,
        private val notesRepository: NotesRepository,
        equalizerManager: EqualizerManager,
    ) : ViewModel() {
        val playerState: StateFlow<PlayerState> = playerController.playerState

        val isEqualizerAvailable: StateFlow<Boolean> =
            equalizerManager.state
                .map { it.isSupported }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = false,
                )

        @OptIn(ExperimentalCoroutinesApi::class)
        val isFavorite: StateFlow<Boolean> =
            playerState
                .flatMapLatest { state ->
                    state.currentSong?.let { song ->
                        favoritesRepository.isFavorite(song.id)
                    } ?: flowOf(false)
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = false,
                )

        @OptIn(ExperimentalCoroutinesApi::class)
        val currentNote: StateFlow<SongNote?> =
            playerState
                .flatMapLatest { state ->
                    state.currentSong?.let { song ->
                        notesRepository.getNote(song.id)
                    } ?: flowOf(null)
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = null,
                )

        fun pause() = playerController.pause()

        fun resume() = playerController.resume()

        fun skipToNext() = playerController.skipToNext()

        fun skipToPrevious() = playerController.skipToPrevious()

        fun seekTo(position: Long) = playerController.seekTo(position)

        fun toggleShuffle() {
            playerController.setShuffleMode(!playerState.value.shuffleMode)
        }

        fun toggleRepeat() {
            val nextMode =
                when (playerState.value.repeatMode) {
                    RepeatMode.NONE -> RepeatMode.ALL
                    RepeatMode.ALL -> RepeatMode.ONE
                    RepeatMode.ONE -> RepeatMode.NONE
                }
            playerController.setRepeatMode(nextMode)
        }

        fun toggleFavorite() {
            val songId = playerState.value.currentSong?.id ?: return
            viewModelScope.launch {
                favoritesRepository.toggleFavorite(songId)
            }
        }

        fun saveNote(content: String) {
            val songId = playerState.value.currentSong?.id ?: return
            viewModelScope.launch {
                notesRepository.saveNote(songId, content)
            }
        }

        fun deleteNote() {
            val songId = playerState.value.currentSong?.id ?: return
            viewModelScope.launch {
                notesRepository.deleteNote(songId)
            }
        }

        fun play(mediaId: String) {
            val state = playerState.value
            val index = state.queue.indexOfFirst { it.mediaId == mediaId }
            if (index != -1) {
                playerController.play(state.queue, index)
            }
        }
    }
