package com.javanapps.musicplayer.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class SongNote(
    val songId: Long,
    val note: String,
    val timestamp: Long,
)
