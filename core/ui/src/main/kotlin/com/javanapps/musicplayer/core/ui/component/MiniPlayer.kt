package com.javanapps.musicplayer.core.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.javanapps.musicplayer.core.designsystem.component.GlassCard
import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.core.ui.R
import com.javanapps.musicplayer.core.ui.icon.AppIcons

@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    progress: () -> Float,
    onPlayPauseClick: () -> Unit,
    onClick: () -> Unit,
    useAnimations: Boolean,
    modifier: Modifier = Modifier,
) {
    val rotationAnimatable = remember { Animatable(0f) }
    LaunchedEffect(isPlaying, useAnimations) {
        if (!useAnimations) {
            rotationAnimatable.snapTo(0f)
            return@LaunchedEffect
        }
        if (isPlaying) {
            rotationAnimatable.snapTo(0f)
            rotationAnimatable.animateTo(
                targetValue = 360f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(20000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
            )
        } else {
            val current = rotationAnimatable.value
            val target = if (current <= 180f) 0f else 360f
            val duration = (minOf(current, 360f - current) / 360f * 1500f).toInt().coerceAtLeast(200)
            rotationAnimatable.animateTo(
                targetValue = target,
                animationSpec = tween(duration, easing = LinearEasing),
            )
            rotationAnimatable.snapTo(0f)
        }
    }

    GlassCard(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(8.dp),
    ) {
        Column {
            Row(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ArtworkImage(
                    artworkUri = song.artworkUri,
                    isCdStyle = true,
                    modifier =
                        Modifier
                            .size(48.dp)
                            .graphicsLayer {
                                if (useAnimations) {
                                    rotationZ = rotationAnimatable.value
                                }
                            },
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onPlayPauseClick) {
                    Icon(
                        imageVector = if (isPlaying) AppIcons.Pause else Icons.Default.PlayArrow,
                        contentDescription =
                            if (isPlaying) {
                                stringResource(R.string.core_ui_pause)
                            } else {
                                stringResource(R.string.core_ui_play)
                            },
                    )
                }
            }
            LinearProgressIndicator(
                progress = progress,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            )
        }
    }
}
