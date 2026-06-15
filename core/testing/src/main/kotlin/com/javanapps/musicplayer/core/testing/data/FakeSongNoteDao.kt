package com.javanapps.musicplayer.core.testing.data

import com.javanapps.musicplayer.core.database.dao.SongNoteDao
import com.javanapps.musicplayer.core.database.model.SongNoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSongNoteDao : SongNoteDao {
    private val notes = MutableStateFlow<Map<Long, SongNoteEntity>>(emptyMap())

    override fun getNote(songId: Long): Flow<SongNoteEntity?> = notes.map { it[songId] }

    override fun getAllNotes(): Flow<List<SongNoteEntity>> = notes.map { it.values.toList() }

    override suspend fun insertNote(note: SongNoteEntity) {
        notes.value = notes.value + (note.songId to note)
    }

    override suspend fun deleteNote(note: SongNoteEntity) {
        notes.value = notes.value - note.songId
    }
}
