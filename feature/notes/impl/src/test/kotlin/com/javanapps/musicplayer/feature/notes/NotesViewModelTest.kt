package com.javanapps.musicplayer.feature.notes

import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.core.testing.repository.FakeNotesRepository
import com.javanapps.musicplayer.core.testing.repository.FakeSongsRepository
import com.javanapps.musicplayer.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class NotesViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val notesRepository = FakeNotesRepository()
    private val songsRepository = FakeSongsRepository()
    private lateinit var viewModel: NotesViewModel

    @Before
    fun setup() {
        viewModel =
            NotesViewModel(
                notesRepository = notesRepository,
                songsRepository = songsRepository,
            )
    }

    @Test
    fun uiState_initiallyLoading() =
        runTest {
            assertEquals(NotesUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun uiState_loadsNotesWithSongs() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            val song = Song(1L, "1", "Title", "Artist", 1L, "Album", 1L, 1000, null, "", 0)
            songsRepository.setSongs(listOf(song))
            notesRepository.saveNote(1L, "Test Note")

            val state = viewModel.uiState.value
            assertIs<NotesUiState.Success>(state)
            assertEquals(1, state.notes.size)
            assertEquals("Test Note", state.notes[0].note.note)
            assertEquals("Title", state.notes[0].song.title)
        }

    @Test
    fun saveNote_callsRepository() =
        runTest {
            viewModel.saveNote(1L, "New Note")
            val note = notesRepository.getNote(1L).first()
            assertEquals("New Note", note?.note)
        }

    @Test
    fun deleteNote_callsRepository() =
        runTest {
            notesRepository.saveNote(1L, "To Delete")
            viewModel.deleteNote(1L)
            val note = notesRepository.getNote(1L).first()
            assertNull(note)
        }
}
