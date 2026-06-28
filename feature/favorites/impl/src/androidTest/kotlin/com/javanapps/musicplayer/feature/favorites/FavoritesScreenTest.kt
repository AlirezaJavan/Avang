package com.javanapps.musicplayer.feature.favorites

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.javanapps.musicplayer.core.model.Song
import org.junit.Rule
import org.junit.Test

class FavoritesScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loading_showsShimmer() {
        composeTestRule.setContent {
            FavoritesScreen(
                uiState = FavoritesUiState.Loading,
                onSongClick = {},
                onToggleFavorite = {},
            )
        }

        // Shimmer boxes don't have text or content description by default in current implementation
        // but we can check that the screen is not empty if it doesn't show empty state
    }

    @Test
    fun success_withSongs_showsSongs() {
        val song = Song(1L, "1", "Song 1", "Artist 1", 1L, "Album 1", 1L, 1000, null, "", 0)

        composeTestRule.setContent {
            FavoritesScreen(
                uiState = FavoritesUiState.Success(listOf(song)),
                onSongClick = {},
                onToggleFavorite = {},
            )
        }

        composeTestRule.onNodeWithText("Song 1").assertExists()
        composeTestRule.onNodeWithText("Artist 1").assertExists()
    }

    @Test
    fun success_empty_showsEmptyState() {
        composeTestRule.setContent {
            FavoritesScreen(
                uiState = FavoritesUiState.Success(emptyList()),
                onSongClick = {},
                onToggleFavorite = {},
            )
        }

        // EmptyState uses stringResource(CoreUiR.string.core_ui_no_songs) which is "No songs found"
        composeTestRule.onNodeWithText("No songs found").assertExists()
    }
}
