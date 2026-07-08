package com.javanapps.musicplayer.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class HomeFeed(
    val recentlyPlayed: List<Song> = emptyList(),
    val mostPlayed: List<Song> = emptyList(),
    val recentlyAdded: List<Song> = emptyList(),
)
