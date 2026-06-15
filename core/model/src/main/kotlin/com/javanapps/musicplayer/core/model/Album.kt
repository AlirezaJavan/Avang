package com.javanapps.musicplayer.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val artworkUri: String?,
    val songCount: Int,
)
