package com.javanapps.musicplayer.core.testing.repository

import com.javanapps.musicplayer.core.domain.repository.NotesRepository
import com.javanapps.musicplayer.core.model.SongNote
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

class FakeNotesRepository : NotesRepository {
    private val notes = mutableMapOf<Long, SongNote>()
    private val flow = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        flow.tryEmit(Unit)
    }

    override fun getNote(songId: Long): Flow<SongNote?> = flow.map { notes[songId] }

    override fun getAllNotes(): Flow<List<SongNote>> = flow.map { notes.values.toList() }

    override suspend fun saveNote(
        songId: Long,
        note: String,
    ) {
        notes[songId] = SongNote(songId = songId, note = note, timestamp = System.currentTimeMillis())
        flow.tryEmit(Unit)
    }

    override suspend fun deleteNote(songId: Long) {
        notes.remove(songId)
        flow.tryEmit(Unit)
    }
}
