package com.javanapps.musicplayer.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class SmartPlaylist(
    val label: String,
    val songCount: Int,
)
