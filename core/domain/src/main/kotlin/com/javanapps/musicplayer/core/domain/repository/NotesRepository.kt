package com.javanapps.musicplayer.core.domain.repository

import com.javanapps.musicplayer.core.model.SongNote
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun getNote(songId: Long): Flow<SongNote?>

    fun getAllNotes(): Flow<List<SongNote>>

    suspend fun saveNote(
        songId: Long,
        note: String,
    )

    suspend fun deleteNote(songId: Long)
}
