package com.javanapps.musicplayer.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val shuffleMode: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val queue: List<Song> = emptyList(),
)
