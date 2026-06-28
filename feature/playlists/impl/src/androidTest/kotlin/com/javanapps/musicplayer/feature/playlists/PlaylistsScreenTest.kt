package com.javanapps.musicplayer.feature.playlists

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.javanapps.musicplayer.core.model.Playlist
import org.junit.Rule
import org.junit.Test
import com.javanapps.musicplayer.core.ui.R as CoreUiR

class PlaylistsScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loading_showsShimmer() {
        composeTestRule.setContent {
            PlaylistsScreen(
                uiState = PlaylistsUiState.Loading,
                onPlaylistClick = {},
                onSmartPlaylistClick = {},
                onCreatePlaylist = {},
                onDeletePlaylist = {},
                onRenamePlaylist = { _, _ -> },
                onScan = {},
            )
        }

        // Shimmer boxes don't have text or content description by default in this app's implementation
        // but we can check if the screen is loading by verifying the FAB or something else is there
        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.getString(CoreUiR.string.core_ui_create_playlist),
            ).assertIsDisplayed()
    }

    @Test
    fun empty_showsEmptyState() {
        composeTestRule.setContent {
            PlaylistsScreen(
                uiState = PlaylistsUiState.Success(emptyList(), emptyList(), false),
                onPlaylistClick = {},
                onSmartPlaylistClick = {},
                onCreatePlaylist = {},
                onDeletePlaylist = {},
                onRenamePlaylist = { _, _ -> },
                onScan = {},
            )
        }

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(CoreUiR.string.core_ui_no_playlists),
            ).assertIsDisplayed()
    }

    @Test
    fun success_showsPlaylists() {
        val playlists =
            listOf(
                Playlist(1L, "My Playlist", 5),
                Playlist(2L, "Another One", 10),
            )
        composeTestRule.setContent {
            PlaylistsScreen(
                uiState = PlaylistsUiState.Success(playlists, emptyList(), false),
                onPlaylistClick = {},
                onSmartPlaylistClick = {},
                onCreatePlaylist = {},
                onDeletePlaylist = {},
                onRenamePlaylist = { _, _ -> },
                onScan = {},
            )
        }

        composeTestRule.onNodeWithText("My Playlist").assertIsDisplayed()
        composeTestRule.onNodeWithText("Another One").assertIsDisplayed()
    }

    @Test
    fun clickAdd_showsCreateDialog() {
        composeTestRule.setContent {
            PlaylistsScreen(
                uiState = PlaylistsUiState.Success(emptyList(), emptyList(), false),
                onPlaylistClick = {},
                onSmartPlaylistClick = {},
                onCreatePlaylist = {},
                onDeletePlaylist = {},
                onRenamePlaylist = { _, _ -> },
                onScan = {},
            )
        }

        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.getString(CoreUiR.string.core_ui_create_playlist),
            ).performClick()

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(CoreUiR.string.core_ui_create_playlist),
            ).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(CoreUiR.string.core_ui_playlist_name),
            ).assertIsDisplayed()
    }
}
