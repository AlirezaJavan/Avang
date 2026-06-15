package com.javanapps.musicplayer.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song_notes")
data class SongNoteEntity(
    @PrimaryKey
    @ColumnInfo(name = "song_id")
    val songId: Long,
    val note: String,
    val timestamp: Long,
)
