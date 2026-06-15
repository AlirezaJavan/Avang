package com.javanapps.musicplayer.feature.player

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.javanapps.musicplayer.core.model.PlayerState
import com.javanapps.musicplayer.core.model.Song
import org.junit.Rule
import org.junit.Test

class PlayerScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun playingSong_showsSongDetails() {
        val song = Song(1L, "s1", "Test Title", "Test Artist", 1L, "Test Album", 1L, 300000L, null, "", 0)
        val playerState =
            PlayerState(
                currentSong = song,
                isPlaying = true,
                duration = 300000L,
            )

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    PlayerScreen(
                        playerState = playerState,
                        currentPosition = 5000L,
                        isFavorite = true,
                        currentNote = null,
                        isEqualizerAvailable = true,
                        onBack = {},
                        onEqualizerClick = {},
                        onFavoriteToggle = {},
                        onSeek = {},
                        onPlayPause = {},
                        onPrevious = {},
                        onNext = {},
                        onShuffleToggle = {},
                        onRepeatToggle = {},
                        onPlayMediaId = {},
                        onSaveNote = {},
                        onDeleteNote = {},
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedVisibility,
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Test Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Artist").assertIsDisplayed()
        // Check "Now Playing" header (persian translation handled in resources, but we can check for text if we know it)
        // composeTestRule.onNodeWithText("Now Playing").assertIsDisplayed()
    }
}
