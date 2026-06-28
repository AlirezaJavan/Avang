package com.javanapps.musicplayer.core.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DynamicBackground(
    artworkUri: String?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val backgroundColor = MaterialTheme.colorScheme.background

    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundGlow")
    // Hold the State without reading it here — reading happens in drawBehind (draw scope)
    // so the animation never triggers recomposition of DynamicBackground or its content.
    val animValueState =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * Math.PI.toFloat(),
            animationSpec =
                infiniteRepeatable(
                    animation = tween(15000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "GlowAnim",
        )

    Box(modifier = modifier.fillMaxSize()) {
        // Animated glow circles — animValueState.value is read in draw scope only
        if (artworkUri.isNullOrBlank()) {
            // If no artwork, we don't draw the extra glow circles here
            // to allow the global AuroraBackground from MainActivity to show through clearly.
        } else {
            Spacer(
                modifier =
                    Modifier.fillMaxSize().drawBehind {
                        val animValue = animValueState.value
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius1 = size.width * 0.8f
                        val radius2 = size.width * 0.6f

                        val x1 = center.x + cos(animValue) * 100
                        val y1 = center.y + sin(animValue) * 200
                        val x2 = center.x + sin(animValue * 0.7f) * 150
                        val y2 = center.y + cos(animValue * 0.5f) * 150

                        drawCircle(
                            brush =
                                Brush.radialGradient(
                                    colors = listOf(primaryColor.copy(alpha = 0.15f), Color.Transparent),
                                    center = Offset(x1, y1),
                                    radius = radius1,
                                ),
                            center = Offset(x1, y1),
                            radius = radius1,
                        )

                        drawCircle(
                            brush =
                                Brush.radialGradient(
                                    colors = listOf(secondaryColor.copy(alpha = 0.1f), Color.Transparent),
                                    center = Offset(x2, y2),
                                    radius = radius2,
                                ),
                            center = Offset(x2, y2),
                            radius = radius2,
                        )
                    },
            )

            AsyncImage(
                model = artworkUri,
                contentDescription = null,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .blur(radius = 80.dp)
                        .alpha(0.45f),
                contentScale = ContentScale.Crop,
            )
        }
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.Transparent,
                                    backgroundColor.copy(alpha = 0.4f),
                                    backgroundColor.copy(alpha = 0.8f),
                                ),
                        ),
                    ),
        )
        content()
    }
}
