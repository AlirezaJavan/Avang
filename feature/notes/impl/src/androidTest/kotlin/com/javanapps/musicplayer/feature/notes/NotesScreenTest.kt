package com.javanapps.musicplayer.feature.notes

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.core.model.SongNote
import org.junit.Rule
import org.junit.Test

class NotesScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun success_showsNotes() {
        val song = Song(1L, "s1", "Song Title", "Artist", 1L, "Album", 1L, 1000, null, "", 0)
        val note = SongNote(1L, "This is a test note", 0L)
        val notes = listOf(NoteWithSong(note, song))
        val uiState = NotesUiState.Success(notes)

        composeTestRule.setContent {
            NotesScreen(
                uiState = uiState,
                onSaveNote = { _, _ -> },
                onDeleteNote = {},
            )
        }

        composeTestRule.onNodeWithText("Song Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("This is a test note").assertIsDisplayed()
    }
}
