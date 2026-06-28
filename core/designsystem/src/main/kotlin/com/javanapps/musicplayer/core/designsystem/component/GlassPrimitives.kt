package com.javanapps.musicplayer.core.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

@Composable
fun GlassSurface(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    color: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
    content: @Composable () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Surface(
        modifier =
            modifier
                .shadow(
                    elevation = 12.dp,
                    shape = shape,
                    ambientColor = primaryColor.copy(alpha = 0.15f),
                    spotColor = primaryColor.copy(alpha = 0.25f),
                ).hazeEffect(state = hazeState)
                .border(
                    width = 1.dp,
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.White.copy(alpha = 0.02f),
                                ),
                        ),
                    shape = shape,
                ),
        color = color,
        shape = shape,
        content = content,
    )
}

@Composable
fun GlassScaffold(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        content()
    }
}

@Composable
fun GlassBottomBar(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val topBorderColor = Color.White.copy(alpha = 0.12f)
    NavigationBar(
        modifier =
            modifier
                .hazeEffect(state = hazeState)
                .drawBehind {
                    drawLine(
                        color = topBorderColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx(),
                    )
                },
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        content = content,
    )
}
