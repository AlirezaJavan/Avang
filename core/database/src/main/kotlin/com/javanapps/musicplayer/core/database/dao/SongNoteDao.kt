package com.javanapps.musicplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.javanapps.musicplayer.core.database.model.SongNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongNoteDao {
    @Query("SELECT * FROM song_notes WHERE song_id = :songId")
    fun getNote(songId: Long): Flow<SongNoteEntity?>

    @Query("SELECT * FROM song_notes")
    fun getAllNotes(): Flow<List<SongNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: SongNoteEntity)

    @Delete
    suspend fun deleteNote(note: SongNoteEntity)
}
