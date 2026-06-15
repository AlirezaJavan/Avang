package com.javanapps.musicplayer.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int,
)
