package com.javanapps.musicplayer.core.designsystem.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.cos
import kotlin.math.sin

/**
 * App-wide animated "aurora" backdrop: a set of slowly drifting radial gradient
 * blobs built from the theme's primary / secondary / tertiary roles, painted over
 * the base background. It adapts automatically to light and dark themes (blob
 * opacity is scaled down on light backgrounds so it stays subtle).
 *
 * The animated phase is read only inside [drawBehind] (draw scope), so the motion
 * never triggers recomposition of this composable or anything above it.
 */
@Composable
fun AuroraBackground(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val base = scheme.background
    val isLight = base.luminance() > 0.5f

    val blobAlpha = if (isLight) 0.22f else 0.40f
    val primary = scheme.primary.copy(alpha = blobAlpha)
    val secondary = scheme.secondary.copy(alpha = blobAlpha * 0.9f)
    val tertiary = scheme.tertiary.copy(alpha = blobAlpha * 0.85f)

    val transition = rememberInfiniteTransition(label = "Aurora")
    val phaseState =
        transition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * Math.PI).toFloat(),
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 22000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "AuroraPhase",
        )

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .drawBehind {
                    val phase = phaseState.value
                    drawRect(color = base)

                    val w = size.width
                    val h = size.height
                    val r = maxOf(w, h)

                    // Blob 1 — primary, upper area
                    val c1 = Offset(w * (0.28f + 0.12f * cos(phase)), h * (0.20f + 0.10f * sin(phase)))
                    drawCircle(
                        brush = Brush.radialGradient(listOf(primary, Color.Transparent), center = c1, radius = r * 0.62f),
                        center = c1,
                        radius = r * 0.62f,
                    )

                    // Blob 2 — tertiary accent, lower-right
                    val c2 = Offset(w * (0.82f + 0.10f * sin(phase * 0.8f)), h * (0.70f + 0.12f * cos(phase * 0.6f)))
                    drawCircle(
                        brush = Brush.radialGradient(listOf(tertiary, Color.Transparent), center = c2, radius = r * 0.55f),
                        center = c2,
                        radius = r * 0.55f,
                    )

                    // Blob 3 — secondary, mid-left drifting
                    val c3 = Offset(w * (0.15f + 0.10f * sin(phase * 1.2f)), h * (0.85f + 0.08f * cos(phase)))
                    drawCircle(
                        brush = Brush.radialGradient(listOf(secondary, Color.Transparent), center = c3, radius = r * 0.50f),
                        center = c3,
                        radius = r * 0.50f,
                    )
                },
    )
}
