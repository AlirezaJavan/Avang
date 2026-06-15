package com.javanapps.musicplayer.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.javanapps.musicplayer.core.testing.data.FakeSongNoteDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class OfflineNotesRepositoryTest {
    private lateinit var songNoteDao: FakeSongNoteDao
    private lateinit var repository: OfflineNotesRepository

    @Before
    fun setup() {
        songNoteDao = FakeSongNoteDao()
        repository = OfflineNotesRepository(songNoteDao)
    }

    @Test
    fun getNote_returnsMappedNote() =
        runTest {
            repository.saveNote(1L, "Test note")

            val result = repository.getNote(1L).first()

            assertThat(result).isNotNull()
            assertThat(result?.note).isEqualTo("Test note")
            assertThat(result?.songId).isEqualTo(1L)
        }

    @Test
    fun deleteNote_removesFromDao() =
        runTest {
            repository.saveNote(1L, "Test note")
            assertThat(repository.getNote(1L).first()).isNotNull()

            repository.deleteNote(1L)
            assertThat(repository.getNote(1L).first()).isNull()
        }
}
