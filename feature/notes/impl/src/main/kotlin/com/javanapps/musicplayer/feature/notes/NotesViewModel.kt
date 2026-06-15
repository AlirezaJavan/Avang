package com.javanapps.musicplayer.feature.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.musicplayer.core.domain.repository.NotesRepository
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.core.model.SongNote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel
    @Inject
    constructor(
        private val notesRepository: NotesRepository,
        private val songsRepository: SongsRepository,
    ) : ViewModel() {
        val uiState: StateFlow<NotesUiState> =
            combine(
                notesRepository.getAllNotes(),
                songsRepository.getSongs(),
            ) { notes, songs ->
                val notesWithSongs =
                    notes.mapNotNull { note ->
                        val song = songs.find { it.id == note.songId }
                        if (song != null) NoteWithSong(note, song) else null
                    }
                NotesUiState.Success(notesWithSongs)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NotesUiState.Loading,
            )

        fun getNote(songId: Long) = notesRepository.getNote(songId)

        fun saveNote(
            songId: Long,
            content: String,
        ) {
            viewModelScope.launch {
                notesRepository.saveNote(songId, content)
            }
        }

        fun deleteNote(songId: Long) {
            viewModelScope.launch {
                notesRepository.deleteNote(songId)
            }
        }
    }

data class NoteWithSong(
    val note: SongNote,
    val song: Song,
)

sealed interface NotesUiState {
    data object Loading : NotesUiState

    data class Success(
        val notes: List<NoteWithSong>,
    ) : NotesUiState
}
