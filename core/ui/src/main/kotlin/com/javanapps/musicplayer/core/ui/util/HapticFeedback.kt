package com.javanapps.musicplayer.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun rememberHapticFeedback(): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return remember {
        {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}

@Composable
fun rememberLongPressHapticFeedback(): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return remember {
        {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}
