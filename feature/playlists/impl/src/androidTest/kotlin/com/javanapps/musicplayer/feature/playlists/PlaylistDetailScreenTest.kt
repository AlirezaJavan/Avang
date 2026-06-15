package com.javanapps.musicplayer.feature.playlists

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.javanapps.musicplayer.core.model.Playlist
import com.javanapps.musicplayer.core.model.Song
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class PlaylistDetailScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun success_showsPlaylistNameAndSongs() {
        val playlist = Playlist(1L, "My Chill Mix", 2)
        val songs =
            listOf(
                Song(1L, "s1", "Song One", "Artist One", 1L, "Album One", 1L, 1000, null, "", 0),
                Song(2L, "s2", "Song Two", "Artist Two", 1L, "Album Two", 1L, 2000, null, "", 0),
            )
        val uiState = PlaylistDetailUiState.Success(playlist, songs)

        composeTestRule.setContent {
            PlaylistDetailScreen(
                uiState = uiState,
                onSongClick = {},
                onBack = {},
                onRemoveSong = {},
            )
        }

        composeTestRule.onNodeWithText("My Chill Mix").assertIsDisplayed()
        composeTestRule.onNodeWithText("Song One").assertIsDisplayed()
        composeTestRule.onNodeWithText("Song Two").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 آهنگ").assertIsDisplayed()
    }

    @Test
    fun error_showsErrorState() {
        composeTestRule.setContent {
            PlaylistDetailScreen(
                uiState = PlaylistDetailUiState.Error,
                onSongClick = {},
                onBack = {},
                onRemoveSong = {},
            )
        }

        composeTestRule.onNodeWithText("لیست پخش یافت نشد").assertIsDisplayed()
    }

    @Test
    fun backClick_callsOnBack() {
        var backClicked = false
        composeTestRule.setContent {
            PlaylistDetailScreen(
                uiState = PlaylistDetailUiState.Loading,
                onSongClick = {},
                onBack = { backClicked = true },
                onRemoveSong = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue(backClicked)
    }
}
