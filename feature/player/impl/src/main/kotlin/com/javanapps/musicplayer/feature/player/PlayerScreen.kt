package com.javanapps.musicplayer.feature.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.javanapps.musicplayer.core.model.PlayerState
import com.javanapps.musicplayer.core.model.RepeatMode
import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.core.model.SongNote
import com.javanapps.musicplayer.core.ui.component.ArtworkImage
import com.javanapps.musicplayer.core.ui.component.DynamicBackground
import com.javanapps.musicplayer.core.ui.component.SongRow
import com.javanapps.musicplayer.core.ui.icon.AppIcons
import com.javanapps.musicplayer.core.ui.transition.PlayerTransitionKeys
import com.javanapps.musicplayer.core.ui.util.rememberHapticFeedback
import com.javanapps.musicplayer.feature.player.navigation.PlayerRoute
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.Locale
import androidx.compose.animation.core.RepeatMode as AnimationRepeatMode
import com.javanapps.musicplayer.core.ui.R as CoreUiR

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.playerScreen(
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    onEqualizerClick: () -> Unit = {},
) {
    composable<PlayerRoute> {
        PlayerScreen(
            onBack = onBack,
            onEqualizerClick = onEqualizerClick,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun PlayerScreen(
    onBack: () -> Unit,
    onEqualizerClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val playerStateFlow = viewModel.playerState
    val stableStateFlow =
        remember(playerStateFlow) {
            playerStateFlow
                .map { it.copy(currentPosition = 0) }
                .distinctUntilChanged()
        }
    val positionFlow =
        remember(playerStateFlow) {
            playerStateFlow
                .map { it.currentPosition }
                .distinctUntilChanged()
        }

    val playerState by stableStateFlow.collectAsStateWithLifecycle(PlayerState())
    val currentPosition by positionFlow.collectAsStateWithLifecycle(0L)

    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val currentNote by viewModel.currentNote.collectAsStateWithLifecycle()
    val isEqualizerAvailable by viewModel.isEqualizerAvailable.collectAsStateWithLifecycle()

    PlayerScreen(
        playerState = playerState,
        currentPosition = currentPosition,
        isFavorite = isFavorite,
        currentNote = currentNote,
        isEqualizerAvailable = isEqualizerAvailable,
        onBack = onBack,
        onEqualizerClick = onEqualizerClick,
        onFavoriteToggle = viewModel::toggleFavorite,
        onSeek = viewModel::seekTo,
        onPlayPause = { if (playerState.isPlaying) viewModel.pause() else viewModel.resume() },
        onPrevious = viewModel::skipToPrevious,
        onNext = viewModel::skipToNext,
        onShuffleToggle = viewModel::toggleShuffle,
        onRepeatToggle = viewModel::toggleRepeat,
        onPlayMediaId = viewModel::play,
        onSaveNote = viewModel::saveNote,
        onDeleteNote = viewModel::deleteNote,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun PlayerScreen(
    playerState: PlayerState,
    currentPosition: Long,
    isFavorite: Boolean,
    currentNote: SongNote?,
    isEqualizerAvailable: Boolean,
    onBack: () -> Unit,
    onEqualizerClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onPlayMediaId: (String) -> Unit,
    onSaveNote: (String) -> Unit,
    onDeleteNote: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    var showQueue by remember { mutableStateOf(false) }
    var showNoteSheet by rememberSaveable { mutableStateOf(false) }
    val haptic = rememberHapticFeedback()

    val rotationAnimatable = remember { Animatable(0f) }
    val isPlaying = playerState.isPlaying

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            rotationAnimatable.snapTo(0f)
            rotationAnimatable.animateTo(
                targetValue = 360f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(20000, easing = LinearEasing),
                        repeatMode = AnimationRepeatMode.Restart,
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

    with(sharedTransitionScope) {
        val scope = this
        val artworkSharedState =
            rememberSharedContentState(
                key = PlayerTransitionKeys.artwork(playerState.currentSong?.id ?: 0L),
            )
        val artworkModifier =
            remember(artworkSharedState, animatedVisibilityScope) {
                with(scope) {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .graphicsLayer { rotationZ = rotationAnimatable.value }
                        .sharedElement(artworkSharedState, animatedVisibilityScope)
                }
            }

        DynamicBackground(artworkUri = playerState.currentSong?.artworkUri) {
            Column(
                modifier =
                    modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val onHeaderQueueClick = remember { { showQueue = true } }
                PlayerHeader(
                    onBack = onBack,
                    onEqualizerClick = onEqualizerClick,
                    onQueueClick = onHeaderQueueClick,
                    isEqualizerAvailable = isEqualizerAvailable,
                )

                Spacer(modifier = Modifier.height(32.dp))

                ArtworkImage(
                    artworkUri = playerState.currentSong?.artworkUri,
                    isCdStyle = true,
                    modifier = artworkModifier,
                )

                Spacer(modifier = Modifier.weight(1f))

                SongInfo(
                    title = playerState.currentSong?.title ?: "",
                    artist = playerState.currentSong?.artist ?: "",
                    isFavorite = isFavorite,
                    hasNote = currentNote != null,
                    onFavoriteToggle = {
                        haptic()
                        onFavoriteToggle()
                    },
                    onAddNoteClick = { showNoteSheet = true },
                )

                Spacer(modifier = Modifier.height(32.dp))

                PlaybackSlider(
                    position = currentPosition,
                    duration = playerState.duration,
                    onSeek = {
                        haptic()
                        onSeek(it)
                    },
                )

                Spacer(modifier = Modifier.height(24.dp))

                PlaybackControls(
                    isPlaying = playerState.isPlaying,
                    shuffleMode = playerState.shuffleMode,
                    repeatMode = playerState.repeatMode,
                    onPlayPauseClick = {
                        haptic()
                        onPlayPause()
                    },
                    onPreviousClick = {
                        haptic()
                        onPrevious()
                    },
                    onNextClick = {
                        haptic()
                        onNext()
                    },
                    onShuffleClick = {
                        haptic()
                        onShuffleToggle()
                    },
                    onRepeatClick = {
                        haptic()
                        onRepeatToggle()
                    },
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        if (showQueue) {
            QueueBottomSheet(
                queue = playerState.queue,
                currentSong = playerState.currentSong,
                onSongClick = { mediaId ->
                    onPlayMediaId(mediaId)
                    showQueue = false
                },
                onDismiss = { showQueue = false },
            )
        }

        if (showNoteSheet) {
            NoteBottomSheet(
                currentNote = currentNote,
                onDismiss = { showNoteSheet = false },
                onSave = { content ->
                    onSaveNote(content)
                    showNoteSheet = false
                },
                onDelete = {
                    onDeleteNote()
                    showNoteSheet = false
                },
            )
        }
    }
}

@Composable
private fun PlayerHeader(
    onBack: () -> Unit,
    onEqualizerClick: () -> Unit,
    onQueueClick: () -> Unit,
    isEqualizerAvailable: Boolean,
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(CoreUiR.string.core_ui_close),
                    modifier = Modifier.size(32.dp),
                )
            }
        }

        Text(
            text = stringResource(CoreUiR.string.core_ui_now_playing),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.Center,
        )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isEqualizerAvailable) {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(CoreUiR.string.core_ui_more_options),
                        )
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(CoreUiR.string.core_ui_equalizer)) },
                                leadingIcon = { Icon(AppIcons.Equalizer, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onEqualizerClick()
                                },
                            )
                        }
                    }
                }

                IconButton(onClick = onQueueClick) {
                    Icon(
                        imageVector = AppIcons.PlaylistPlay,
                        contentDescription = stringResource(CoreUiR.string.core_ui_up_next),
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SongInfo(
    title: String,
    artist: String,
    isFavorite: Boolean,
    hasNote: Boolean,
    onFavoriteToggle: () -> Unit,
    onAddNoteClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = artist,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onAddNoteClick) {
            Icon(
                imageVector = AppIcons.NoteAdd,
                contentDescription = stringResource(CoreUiR.string.core_ui_notes),
                tint = if (hasNote) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(onClick = onFavoriteToggle) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(CoreUiR.string.core_ui_favorite),
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun PlaybackSlider(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
) {
    var sliderPosition by remember(position) { mutableFloatStateOf(position.toFloat()) }
    var isDragging by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = if (isDragging) sliderPosition else position.toFloat(),
            onValueChange = {
                isDragging = true
                sliderPosition = it
            },
            onValueChangeFinished = {
                isDragging = false
                onSeek(sliderPosition.toLong())
            },
            valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatDuration(if (isDragging) sliderPosition.toLong() else position),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    shuffleMode: Boolean,
    repeatMode: RepeatMode,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1000),
                repeatMode = AnimationRepeatMode.Reverse,
            ),
        label = "PulseScale",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onShuffleClick) {
            Icon(
                imageVector = AppIcons.Shuffle,
                contentDescription = stringResource(CoreUiR.string.core_ui_shuffle),
                tint = if (shuffleMode) MaterialTheme.colorScheme.primary else Color.White,
            )
        }

        IconButton(onClick = onPreviousClick) {
            Icon(
                imageVector = AppIcons.SkipPrevious,
                contentDescription = stringResource(CoreUiR.string.core_ui_previous),
                modifier = Modifier.size(40.dp),
            )
        }

        IconButton(
            onClick = onPlayPauseClick,
            modifier =
                Modifier
                    .size(72.dp)
                    .graphicsLayer(
                        scaleX = if (!isPlaying) pulseScale else 1f,
                        scaleY = if (!isPlaying) pulseScale else 1f,
                    ),
        ) {
            AnimatedContent(
                targetState = isPlaying,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "PlayPauseAnimation",
            ) { playing ->
                Icon(
                    imageVector = if (playing) AppIcons.PauseCircleFilled else AppIcons.PlayCircleFilled,
                    contentDescription = stringResource(if (playing) CoreUiR.string.core_ui_pause else CoreUiR.string.core_ui_play),
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        IconButton(onClick = onNextClick) {
            Icon(
                imageVector = AppIcons.SkipNext,
                contentDescription = stringResource(CoreUiR.string.core_ui_next),
                modifier = Modifier.size(40.dp),
            )
        }

        IconButton(onClick = onRepeatClick) {
            val icon =
                when (repeatMode) {
                    RepeatMode.ONE -> AppIcons.RepeatOne
                    else -> AppIcons.Repeat
                }
            Icon(
                imageVector = icon,
                contentDescription = stringResource(CoreUiR.string.core_ui_repeat),
                tint = if (repeatMode != RepeatMode.NONE) MaterialTheme.colorScheme.primary else Color.White,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueBottomSheet(
    queue: List<Song>,
    currentSong: Song?,
    onSongClick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val currentMediaId = currentSong?.mediaId

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(CoreUiR.string.core_ui_up_next),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp),
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    items = queue,
                    key = { it.id },
                    contentType = { "song" },
                ) { song ->
                    SongRow(
                        song = song,
                        onClick = { onSongClick(song.mediaId) },
                        modifier =
                            if (song.mediaId == currentMediaId) {
                                Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            } else {
                                Modifier
                            },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteBottomSheet(
    currentNote: SongNote?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var noteText by rememberSaveable(currentNote) { mutableStateOf(currentNote?.note ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
            Text(
                text =
                    stringResource(
                        if (currentNote == null) {
                            CoreUiR.string.core_ui_add_note
                        } else {
                            CoreUiR.string.core_ui_edit_note
                        },
                    ),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                label = { Text(stringResource(CoreUiR.string.core_ui_note_content)) },
                maxLines = 10,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                if (currentNote != null) {
                    TextButton(onClick = onDelete) {
                        Text(
                            text = stringResource(CoreUiR.string.core_ui_delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(CoreUiR.string.core_ui_cancel))
                }
                TextButton(
                    onClick = { onSave(noteText) },
                    enabled = noteText.isNotBlank(),
                ) {
                    Text(stringResource(CoreUiR.string.core_ui_save))
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
