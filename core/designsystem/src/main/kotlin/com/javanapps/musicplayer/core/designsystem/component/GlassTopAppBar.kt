package com.javanapps.musicplayer.core.designsystem.component

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.javanapps.musicplayer.core.designsystem.theme.MusicPlayerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},
) {
    val bottomBorderColor = Color.White.copy(alpha = 0.08f)
    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        navigationIcon = navigationIcon,
        actions = { actions() },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            ),
        modifier =
            modifier
                .drawBehind {
                    drawLine(
                        color = bottomBorderColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                },
    )
}

@Preview(name = "GlassTopAppBar Light", showBackground = true)
@Preview(name = "GlassTopAppBar Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GlassTopAppBarPreview() {
    MusicPlayerTheme {
        GlassTopAppBar(
            title = "Library",
        )
    }
}
