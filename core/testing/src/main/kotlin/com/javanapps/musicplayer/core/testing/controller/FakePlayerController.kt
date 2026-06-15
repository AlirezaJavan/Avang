package com.javanapps.musicplayer.core.testing.controller

import com.javanapps.musicplayer.core.domain.controller.PlayerController
import com.javanapps.musicplayer.core.model.PlayerState
import com.javanapps.musicplayer.core.model.RepeatMode
import com.javanapps.musicplayer.core.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakePlayerController : PlayerController {
    private val _playerState = MutableStateFlow(PlayerState())
    override val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    val playCalls = mutableListOf<Pair<List<Song>, Int>>()

    override fun play(
        songs: List<Song>,
        startIndex: Int,
    ) {
        playCalls.add(songs to startIndex)
        _playerState.update { state ->
            state.copy(
                currentSong = songs.getOrNull(startIndex),
                isPlaying = true,
                currentPosition = 0L,
                queue = songs,
            )
        }
    }

    override fun pause() {
        _playerState.update { it.copy(isPlaying = false) }
    }

    override fun resume() {
        _playerState.update { it.copy(isPlaying = true) }
    }

    override fun skipToNext() {
        val state = _playerState.value
        val currentIndex = state.queue.indexOf(state.currentSong)
        val nextIndex = (currentIndex + 1).coerceAtMost(state.queue.lastIndex)
        _playerState.update { it.copy(currentSong = it.queue.getOrNull(nextIndex), currentPosition = 0L) }
    }

    override fun skipToPrevious() {
        val state = _playerState.value
        val currentIndex = state.queue.indexOf(state.currentSong)
        val prevIndex = (currentIndex - 1).coerceAtLeast(0)
        _playerState.update { it.copy(currentSong = it.queue.getOrNull(prevIndex), currentPosition = 0L) }
    }

    override fun seekTo(position: Long) {
        _playerState.update { it.copy(currentPosition = position) }
    }

    override fun setShuffleMode(enabled: Boolean) {
        _playerState.update { it.copy(shuffleMode = enabled) }
    }

    override fun setRepeatMode(repeatMode: RepeatMode) {
        _playerState.update { it.copy(repeatMode = repeatMode) }
    }

    override fun addToQueueNext(song: Song) {
        _playerState.update { state ->
            val current = state.queue.toMutableList()
            val insertAt = (state.queue.indexOf(state.currentSong) + 1).coerceAtLeast(0)
            current.add(insertAt, song)
            state.copy(queue = current)
        }
    }

    override fun addToQueueLast(song: Song) {
        _playerState.update { it.copy(queue = it.queue + song) }
    }
}
