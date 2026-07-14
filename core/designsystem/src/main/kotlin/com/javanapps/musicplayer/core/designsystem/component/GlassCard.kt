package com.javanapps.musicplayer.core.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.javanapps.musicplayer.core.designsystem.theme.MusicPlayerTheme

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    color: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    content: @Composable () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val baseModifier =
        modifier
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = primaryColor.copy(alpha = 0.15f),
                spotColor = primaryColor.copy(alpha = 0.25f),
            ).border(
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
            )
    val cardModifier = if (onClick != null) baseModifier.clickable(onClick = onClick) else baseModifier

    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = shape,
        content = { content() },
    )
}

@Preview(name = "GlassCard Light", showBackground = true)
@Preview(name = "GlassCard Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GlassCardPreview() {
    MusicPlayerTheme {
        GlassCard(
            modifier = Modifier.fillMaxWidth().height(80.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp))
        }
    }
}
