package com.javanapps.musicplayer.core.domain.controller

import com.javanapps.musicplayer.core.model.PlayerState
import com.javanapps.musicplayer.core.model.RepeatMode
import com.javanapps.musicplayer.core.model.Song
import kotlinx.coroutines.flow.StateFlow

interface PlayerController {
    val playerState: StateFlow<PlayerState>

    fun play(
        songs: List<Song>,
        startIndex: Int = 0,
    )

    fun pause()

    fun resume()

    fun skipToNext()

    fun skipToPrevious()

    fun seekTo(position: Long)

    fun setShuffleMode(enabled: Boolean)

    fun setRepeatMode(repeatMode: RepeatMode)

    fun addToQueueNext(song: Song)

    fun addToQueueLast(song: Song)
}
