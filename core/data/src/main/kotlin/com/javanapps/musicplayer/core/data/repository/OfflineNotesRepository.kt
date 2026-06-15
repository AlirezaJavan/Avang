package com.javanapps.musicplayer.core.data.repository

import com.javanapps.musicplayer.core.database.dao.SongNoteDao
import com.javanapps.musicplayer.core.database.model.SongNoteEntity
import com.javanapps.musicplayer.core.database.model.asExternalModel
import com.javanapps.musicplayer.core.domain.repository.NotesRepository
import com.javanapps.musicplayer.core.model.SongNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineNotesRepository
    @Inject
    constructor(
        private val songNoteDao: SongNoteDao,
    ) : NotesRepository {
        override fun getNote(songId: Long): Flow<SongNote?> = songNoteDao.getNote(songId).map { it?.asExternalModel() }

        override fun getAllNotes(): Flow<List<SongNote>> =
            songNoteDao.getAllNotes().map { entities ->
                entities.map { it.asExternalModel() }
            }

        override suspend fun saveNote(
            songId: Long,
            note: String,
        ) = songNoteDao.insertNote(
            SongNoteEntity(
                songId = songId,
                note = note,
                timestamp = System.currentTimeMillis(),
            ),
        )

        override suspend fun deleteNote(songId: Long) = songNoteDao.deleteNote(SongNoteEntity(songId = songId, note = "", timestamp = 0L))
    }
