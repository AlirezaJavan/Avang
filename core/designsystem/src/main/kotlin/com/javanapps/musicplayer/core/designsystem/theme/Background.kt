package com.javanapps.musicplayer.core.designsystem.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun DynamicBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> =
        listOf(
            Blue20,
            DarkBlue20,
            Gray10,
        ),
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .drawWithCache {
                    onDrawBehind {
                        drawRect(
                            brush =
                                Brush.verticalGradient(
                                    colors = colors,
                                ),
                        )
                    }
                },
    ) {
        content()
    }
}
