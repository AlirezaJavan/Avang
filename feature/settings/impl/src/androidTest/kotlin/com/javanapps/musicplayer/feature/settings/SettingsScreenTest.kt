package com.javanapps.musicplayer.feature.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.javanapps.musicplayer.core.domain.repository.UserData
import com.javanapps.musicplayer.core.model.DarkThemeConfig
import org.junit.Rule
import org.junit.Test
import com.javanapps.musicplayer.core.ui.R as CoreUiR

class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun userDataLoaded_showsSettings() {
        val userData =
            UserData(
                shuffleMode = false,
                repeatMode = com.javanapps.musicplayer.core.model.RepeatMode.NONE,
                lastPlayedSongId = null,
                lastPlaybackPosition = 0L,
                dynamicColor = true,
                darkThemeConfig = DarkThemeConfig.DARK,
                language = "en",
            )

        composeTestRule.setContent {
            SettingsScreen(
                userData = userData,
                onThemeChange = {},
                onDynamicColorChange = {},
                onLanguageChange = {},
            )
        }

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(CoreUiR.string.core_ui_theme),
            ).assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(CoreUiR.string.core_ui_theme_dark),
            ).assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(CoreUiR.string.core_ui_dynamic_color),
            ).assertIsDisplayed()
    }
}
