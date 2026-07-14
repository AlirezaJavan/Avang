package com.javanapps.musicplayer.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity

// Tracked separately from song_tags so a song that was analyzed but produced no genre tags
// (e.g. confidence too low) is still recognized as done and isn't re-analyzed on every sync.
@Entity(tableName = "analyzed_songs", primaryKeys = ["song_id"])
data class AnalyzedSongEntity(
    @ColumnInfo(name = "song_id")
    val songId: Long,
    @ColumnInfo(name = "analyzed_at")
    val analyzedAt: Long,
)
