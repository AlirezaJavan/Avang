package com.javanapps.musicplayer.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.javanapps.musicplayer.core.database.dao.FavoriteDao
import com.javanapps.musicplayer.core.database.dao.PlayCountDao
import com.javanapps.musicplayer.core.database.dao.PlaylistDao
import com.javanapps.musicplayer.core.database.dao.SongNoteDao
import com.javanapps.musicplayer.core.database.dao.SongTagDao
import com.javanapps.musicplayer.core.database.model.FavoriteEntity
import com.javanapps.musicplayer.core.database.model.PlayCountEntity
import com.javanapps.musicplayer.core.database.model.PlaylistEntity
import com.javanapps.musicplayer.core.database.model.PlaylistSongCrossRef
import com.javanapps.musicplayer.core.database.model.SongNoteEntity
import com.javanapps.musicplayer.core.database.model.SongTagEntity

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        FavoriteEntity::class,
        SongNoteEntity::class,
        PlayCountEntity::class,
        SongTagEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao

    abstract fun favoriteDao(): FavoriteDao

    abstract fun songNoteDao(): SongNoteDao

    abstract fun playCountDao(): PlayCountDao

    abstract fun songTagDao(): SongTagDao
}
