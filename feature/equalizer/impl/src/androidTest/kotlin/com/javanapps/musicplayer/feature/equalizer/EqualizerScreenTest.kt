package com.javanapps.musicplayer.feature.equalizer

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.javanapps.musicplayer.core.domain.equalizer.EqualizerState
import org.junit.Rule
import org.junit.Test
import com.javanapps.musicplayer.core.ui.R as CoreUiR

class EqualizerScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun notInitialized_showsNotAvailableMessage() {
        composeTestRule.setContent {
            EqualizerScreen(
                state = EqualizerState(initialized = false),
                onBack = {},
                onToggleEnabled = {},
                onBandLevelChange = { _, _ -> },
                onApplyPreset = {},
            )
        }

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(CoreUiR.string.core_ui_eq_not_available),
            ).assertIsDisplayed()
    }

    @Test
    fun initialized_showsPresets() {
        val presets = listOf("Normal", "Pop", "Rock")
        composeTestRule.setContent {
            EqualizerScreen(
                state =
                    EqualizerState(
                        initialized = true,
                        isSupported = true,
                        presets = presets,
                        currentPreset = 0,
                    ),
                onBack = {},
                onToggleEnabled = {},
                onBandLevelChange = { _, _ -> },
                onApplyPreset = {},
            )
        }

        composeTestRule.onNodeWithText("Normal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pop").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rock").assertIsDisplayed()
    }
}
