package com.javanapps.musicplayer.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
)

data class PlaylistEntityWithCount(
    val id: Long,
    val name: String,
    val songCount: Int,
)
