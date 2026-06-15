package com.javanapps.musicplayer.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_counts")
data class PlayCountEntity(
    @PrimaryKey
    @ColumnInfo(name = "song_id")
    val songId: Long,
    val count: Int,
    @ColumnInfo(name = "last_played")
    val lastPlayed: Long,
)
