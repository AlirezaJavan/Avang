package com.javanapps.musicplayer.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.javanapps.musicplayer.core.ui.icon.AppIcons

@Composable
fun ArtworkImage(
    artworkUri: String?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    contentDescription: String? = null,
    isCdStyle: Boolean = false,
) {
    Surface(
        modifier = modifier.aspectRatio(1f),
        shape = if (isCdStyle) CircleShape else shape,
        color = if (isCdStyle) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                if (isCdStyle) {
                    Modifier
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }.drawWithContent {
                            drawContent()
                            drawCircle(
                                color = Color.Black,
                                radius = size.minDimension * 0.18f / 2,
                                blendMode = BlendMode.Clear,
                            )
                        }
                } else {
                    Modifier.fillMaxSize()
                },
        ) {
            if (artworkUri != null) {
                val context = LocalContext.current
                val request =
                    remember(artworkUri) {
                        ImageRequest
                            .Builder(context)
                            .data(artworkUri)
                            .crossfade(true)
                            .build()
                    }
                AsyncImage(
                    model = request,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                val primary = MaterialTheme.colorScheme.primary
                val secondary = MaterialTheme.colorScheme.secondary
                val tertiary = MaterialTheme.colorScheme.tertiary

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.sweepGradient(
                                    colors =
                                        listOf(
                                            primary.copy(alpha = 0.6f),
                                            secondary.copy(alpha = 0.6f),
                                            tertiary.copy(alpha = 0.6f),
                                            primary.copy(alpha = 0.6f),
                                        ),
                                ),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    // Vinyl grooves effect
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val radius = size.minDimension / 2
                        for (i in 1..5) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.05f),
                                radius = radius * (0.3f + i * 0.12f),
                                style =
                                    androidx.compose.ui.graphics.drawscope
                                        .Stroke(width = 1.dp.toPx()),
                            )
                        }
                    }

                    Icon(
                        imageVector = AppIcons.MusicNote,
                        contentDescription = contentDescription,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxSize(0.35f),
                    )
                }
            }

            if (isCdStyle) {
                // Subtle vinyl ring around the hole
                Canvas(modifier = Modifier.fillMaxSize(0.2f)) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        style =
                            androidx.compose.ui.graphics.drawscope
                                .Stroke(width = 1.dp.toPx()),
                    )
                }
                // Subtle vinyl ring
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .border(
                                width = 0.5.dp,
                                color = Color.White.copy(alpha = 0.05f),
                                shape = CircleShape,
                            ),
                )
            }
        }
    }
}
