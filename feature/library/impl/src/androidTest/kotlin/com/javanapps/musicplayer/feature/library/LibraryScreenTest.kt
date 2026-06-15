package com.javanapps.musicplayer.feature.library

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.javanapps.musicplayer.core.model.Song
import org.junit.Rule
import org.junit.Test

class LibraryScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loading_showsSongsTab() {
        composeTestRule.setContent {
            LibraryScreen(
                uiState = LibraryUiState.Loading,
                searchQuery = "",
                onSearchQueryChanged = {},
                sortOrder = SortOrder.TITLE,
                onSortOrderChanged = {},
                onSongClick = {},
                onAlbumClick = {},
                onArtistClick = {},
                favoriteSongIds = emptySet(),
                onToggleFavorite = {},
                onPlayNext = {},
                onAddToQueue = {},
                onAddToPlaylistClick = {},
                onAddNoteClick = {},
            )
        }

        // Check if the "Songs" tab is displayed
        composeTestRule.onNodeWithText("Songs").assertIsDisplayed()
    }

    @Test
    fun success_showsSongs() {
        val song =
            Song(
                id = 1L,
                mediaId = "1",
                title = "Test Song",
                artist = "Test Artist",
                artistId = 1L,
                album = "Test Album",
                albumId = 1L,
                duration = 3000L,
                artworkUri = null,
                mediaUri = "",
                dateAdded = 0L,
            )
        val uiState =
            LibraryUiState.Success(
                songs = listOf(song),
                albums = emptyList(),
                artists = emptyList(),
            )

        composeTestRule.setContent {
            LibraryScreen(
                uiState = uiState,
                searchQuery = "",
                onSearchQueryChanged = {},
                sortOrder = SortOrder.TITLE,
                onSortOrderChanged = {},
                onSongClick = {},
                onAlbumClick = {},
                onArtistClick = {},
                favoriteSongIds = emptySet(),
                onToggleFavorite = {},
                onPlayNext = {},
                onAddToQueue = {},
                onAddToPlaylistClick = {},
                onAddNoteClick = {},
            )
        }

        composeTestRule.onNodeWithText("Test Song").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Artist").assertIsDisplayed()
    }

    @Test
    fun success_empty_showsEmptyState() {
        val uiState =
            LibraryUiState.Success(
                songs = emptyList(),
                albums = emptyList(),
                artists = emptyList(),
            )

        composeTestRule.setContent {
            LibraryScreen(
                uiState = uiState,
                searchQuery = "",
                onSearchQueryChanged = {},
                sortOrder = SortOrder.TITLE,
                onSortOrderChanged = {},
                onSongClick = {},
                onAlbumClick = {},
                onArtistClick = {},
                favoriteSongIds = emptySet(),
                onToggleFavorite = {},
                onPlayNext = {},
                onAddToQueue = {},
                onAddToPlaylistClick = {},
                onAddNoteClick = {},
            )
        }

        composeTestRule.onNodeWithText("No songs found").assertIsDisplayed()
    }
}
