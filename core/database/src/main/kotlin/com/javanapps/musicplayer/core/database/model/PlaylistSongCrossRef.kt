package com.javanapps.musicplayer.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlist_id", "song_id"],
)
data class PlaylistSongCrossRef(
    @ColumnInfo(name = "playlist_id")
    val playlistId: Long,
    @ColumnInfo(name = "song_id")
    val songId: Long,
)
