package com.javanapps.musicplayer.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class Song(
    val id: Long,
    val mediaId: String,
    val title: String,
    val artist: String,
    val artistId: Long,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val artworkUri: String?,
    val mediaUri: String,
    val dateAdded: Long,
)
