package com.javanapps.musicplayer.feature.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.javanapps.musicplayer.core.ui.component.EmptyState
import com.javanapps.musicplayer.core.ui.component.ScreenHeader
import com.javanapps.musicplayer.core.ui.component.SongRow
import com.javanapps.musicplayer.core.ui.icon.AppIcons
import com.javanapps.musicplayer.feature.notes.navigation.NotesRoute
import com.javanapps.musicplayer.core.ui.R as CoreUiR

fun NavGraphBuilder.notesScreen() {
    composable<NotesRoute> {
        NotesScreen()
    }
}

@Composable
internal fun NotesScreen(
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NotesScreen(
        uiState = uiState,
        onSaveNote = viewModel::saveNote,
        onDeleteNote = viewModel::deleteNote,
        modifier = modifier,
    )
}

@Composable
internal fun NotesScreen(
    uiState: NotesUiState,
    onSaveNote: (Long, String) -> Unit,
    onDeleteNote: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(CoreUiR.string.core_ui_notes))

        when (val state = uiState) {
            NotesUiState.Loading -> { /* Loading */ }
            is NotesUiState.Success -> {
                if (state.notes.isEmpty()) {
                    EmptyState(
                        message = stringResource(CoreUiR.string.core_ui_no_notes),
                        icon = AppIcons.Notes,
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            items = state.notes,
                            key = { it.note.songId },
                            contentType = { "note" },
                        ) { noteWithSong ->
                            var showEditDialog by remember { mutableStateOf(false) }

                            val cardShape = MaterialTheme.shapes.medium
                            Column(
                                modifier =
                                    Modifier
                                        .animateItem()
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                        .clip(cardShape)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
                            ) {
                                SongRow(
                                    song = noteWithSong.song,
                                    onClick = { showEditDialog = true },
                                    trailingContent = {
                                        Row {
                                            IconButton(onClick = { showEditDialog = true }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                                            }
                                            IconButton(onClick = { onDeleteNote(noteWithSong.song.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                                            }
                                        }
                                    },
                                )
                                Text(
                                    text = noteWithSong.note.note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 24.dp, end = 16.dp, bottom = 16.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            if (showEditDialog) {
                                NoteDialog(
                                    initialNote = noteWithSong.note.note,
                                    onDismiss = { showEditDialog = false },
                                    onConfirm = { newNote ->
                                        onSaveNote(noteWithSong.song.id, newNote)
                                        showEditDialog = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteDialog(
    initialNote: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var note by remember { mutableStateOf(initialNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(if (initialNote.isEmpty()) CoreUiR.string.core_ui_add_note else CoreUiR.string.core_ui_edit_note),
            )
        },
        text = {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(CoreUiR.string.core_ui_note_content)) },
                modifier = Modifier.fillMaxWidth().height(150.dp),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(note) },
            ) {
                Text(stringResource(CoreUiR.string.core_ui_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(CoreUiR.string.core_ui_cancel))
            }
        },
    )
}
