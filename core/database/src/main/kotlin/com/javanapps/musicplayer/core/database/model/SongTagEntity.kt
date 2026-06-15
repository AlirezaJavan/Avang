package com.javanapps.musicplayer.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "song_tags", primaryKeys = ["song_id", "label"])
data class SongTagEntity(
    @ColumnInfo(name = "song_id")
    val songId: Long,
    val label: String,
    val confidence: Float,
    val source: String,
    @ColumnInfo(name = "analyzed_at")
    val analyzedAt: Long,
)
