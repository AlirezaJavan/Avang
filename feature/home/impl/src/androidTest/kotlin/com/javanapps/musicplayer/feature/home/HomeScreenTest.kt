package com.javanapps.musicplayer.feature.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.javanapps.musicplayer.core.model.HomeFeed
import com.javanapps.musicplayer.core.model.PlayerState
import com.javanapps.musicplayer.core.model.Song
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class HomeScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val song = Song(1L, "1", "Song 1", "Artist 1", 1L, "Album 1", 1L, 1000, null, "", 0)

    @Test
    fun success_empty_showsEmptyState() {
        composeTestRule.setContent {
            HomeScreen(
                uiState = HomeUiState.Success(HomeFeed(), PlayerState()),
                onSongClick = { _, _ -> },
                onHeroClick = {},
                onPlayPauseClick = {},
            )
        }

        composeTestRule.onNodeWithText("No songs found").assertExists()
    }

    @Test
    fun success_withRecentlyAdded_showsShelfAndHandlesClick() {
        var clickedSongs: List<Song>? = null
        var clickedIndex: Int? = null

        composeTestRule.setContent {
            HomeScreen(
                uiState = HomeUiState.Success(HomeFeed(recentlyAdded = listOf(song)), PlayerState()),
                onSongClick = { songs, index ->
                    clickedSongs = songs
                    clickedIndex = index
                },
                onHeroClick = {},
                onPlayPauseClick = {},
            )
        }

        composeTestRule.onNodeWithText("Recently Added").assertExists()
        composeTestRule.onNodeWithText("Song 1").assertExists()

        composeTestRule.onNodeWithText("Song 1").performClick()

        assertEquals(listOf(song), clickedSongs)
        assertEquals(0, clickedIndex)
    }

    @Test
    fun success_withCurrentSong_showsHeroCard() {
        val playerState = PlayerState(currentSong = song, isPlaying = true)

        composeTestRule.setContent {
            HomeScreen(
                uiState = HomeUiState.Success(HomeFeed(), playerState),
                onSongClick = { _, _ -> },
                onHeroClick = {},
                onPlayPauseClick = {},
            )
        }

        composeTestRule.onNodeWithText("Song 1").assertExists()
        composeTestRule.onNodeWithText("Artist 1").assertExists()
    }
}
