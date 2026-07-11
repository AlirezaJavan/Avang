package com.javanapps.musicplayer.feature.home

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.javanapps.musicplayer.core.model.HomeFeed
import com.javanapps.musicplayer.core.model.PlayerState
import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.core.ui.component.ArtworkImage
import com.javanapps.musicplayer.core.ui.component.EmptyState
import com.javanapps.musicplayer.core.ui.component.NotificationPermissionBanner
import com.javanapps.musicplayer.core.ui.component.PermissionContent
import com.javanapps.musicplayer.core.ui.component.ScreenHeader
import com.javanapps.musicplayer.core.ui.component.ShimmerBox
import com.javanapps.musicplayer.core.ui.icon.AppIcons
import com.javanapps.musicplayer.core.ui.util.clickableScale
import com.javanapps.musicplayer.feature.home.navigation.HomeRoute
import com.javanapps.musicplayer.core.ui.R as CoreUiR

fun NavGraphBuilder.homeScreen(onSongClick: (String) -> Unit) {
    composable<HomeRoute> {
        HomeScreen(onSongClick = onSongClick)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun HomeScreen(
    onSongClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val permission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    val permissionState = rememberPermissionState(permission)

    if (permissionState.status.isGranted) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        val onSongClickStable =
            remember(onSongClick) {
                { songs: List<Song>, index: Int ->
                    viewModel.playFromShelf(songs, index)
                    onSongClick(songs[index].mediaId)
                }
            }

        val onPlayPauseClickStable =
            remember(uiState) {
                {
                    val state = uiState
                    if (state is HomeUiState.Success && state.playerState.isPlaying) {
                        viewModel.pause()
                    } else {
                        viewModel.resume()
                    }
                }
            }

        Column(modifier = modifier.fillMaxSize()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                NotificationPermissionRow()
            }
            HomeScreen(
                uiState = uiState,
                onSongClick = onSongClickStable,
                onHeroClick = onSongClick,
                onPlayPauseClick = onPlayPauseClickStable,
                modifier = Modifier.weight(1f),
            )
        }
    } else {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                ),
                        ),
                    ),
        ) {
            PermissionContent(
                shouldShowRationale = permissionState.status.shouldShowRationale,
                onRequestPermission = { permissionState.launchPermissionRequest() },
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun NotificationPermissionRow(modifier: Modifier = Modifier) {
    val notificationPermissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    var dismissed by rememberSaveable { mutableStateOf(false) }

    if (!dismissed && !notificationPermissionState.status.isGranted) {
        NotificationPermissionBanner(
            shouldShowRationale = notificationPermissionState.status.shouldShowRationale,
            onRequestPermission = { notificationPermissionState.launchPermissionRequest() },
            onDismiss = { dismissed = true },
            modifier = modifier.fillMaxWidth().wrapContentHeight().padding(16.dp),
        )
    }
}

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onSongClick: (List<Song>, Int) -> Unit,
    onHeroClick: (String) -> Unit,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(CoreUiR.string.core_ui_home))

        when (uiState) {
            HomeUiState.Loading -> HomeLoading()
            is HomeUiState.Success -> {
                if (uiState.isEmpty && uiState.playerState.currentSong == null) {
                    EmptyState(
                        message = stringResource(CoreUiR.string.core_ui_no_songs),
                        icon = AppIcons.Home,
                    )
                } else {
                    HomeContent(
                        feed = uiState.feed,
                        playerState = uiState.playerState,
                        useAnimations = uiState.useAnimations,
                        onSongClick = onSongClick,
                        onHeroClick = onHeroClick,
                        onPlayPauseClick = onPlayPauseClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeLoading(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(180.dp))
        Spacer(modifier = Modifier.height(24.dp))
        repeat(2) {
            ShimmerBox(modifier = Modifier.width(120.dp).height(18.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Row {
                repeat(3) {
                    ShimmerBox(modifier = Modifier.size(120.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeContent(
    feed: HomeFeed,
    playerState: PlayerState,
    useAnimations: Boolean,
    onSongClick: (List<Song>, Int) -> Unit,
    onHeroClick: (String) -> Unit,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val recentlyPlayedTitle = stringResource(CoreUiR.string.core_ui_recently_played)
    val mostPlayedTitle = stringResource(CoreUiR.string.core_ui_most_played)
    val recentlyAddedTitle = stringResource(CoreUiR.string.core_ui_recently_added)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        val currentSong = playerState.currentSong
        if (currentSong != null) {
            item(key = "hero", contentType = "hero") {
                StaggeredEntrance(index = 0, enabled = useAnimations) {
                    HeroNowPlayingCard(
                        song = currentSong,
                        isPlaying = playerState.isPlaying,
                        onClick = { onHeroClick(currentSong.mediaId) },
                        onPlayPauseClick = onPlayPauseClick,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }
        }

        songShelf(
            key = "recently_played",
            title = recentlyPlayedTitle,
            songs = feed.recentlyPlayed,
            onSongClick = onSongClick,
            delayIndex = 1,
            useAnimations = useAnimations,
        )
        songShelf(
            key = "most_played",
            title = mostPlayedTitle,
            songs = feed.mostPlayed,
            onSongClick = onSongClick,
            delayIndex = 2,
            useAnimations = useAnimations,
        )
        songShelf(
            key = "recently_added",
            title = recentlyAddedTitle,
            songs = feed.recentlyAdded,
            onSongClick = onSongClick,
            delayIndex = 3,
            useAnimations = useAnimations,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.songShelf(
    key: String,
    title: String,
    songs: List<Song>,
    onSongClick: (List<Song>, Int) -> Unit,
    delayIndex: Int,
    useAnimations: Boolean,
) {
    if (songs.isEmpty()) return

    item(key = "${key}_header", contentType = "header") {
        StaggeredEntrance(index = delayIndex, enabled = useAnimations) {
            ShelfHeader(title = title)
        }
    }
    item(key = "${key}_row", contentType = "song_row") {
        StaggeredEntrance(index = delayIndex, enabled = useAnimations) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(songs, key = { it.id }) { song ->
                    SongShelfCard(
                        song = song,
                        onClick = { onSongClick(songs, songs.indexOf(song)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ShelfHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun SongShelfCard(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(120.dp).clickableScale(onClick = onClick),
    ) {
        ArtworkImage(
            artworkUri = song.artworkUri,
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(16.dp),
            contentDescription = song.title,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
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
}

@Composable
private fun HeroNowPlayingCard(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(shape)
                .clickable(onClick = onClick),
    ) {
        ArtworkImage(
            artworkUri = song.artworkUri,
            modifier = Modifier.fillMaxSize(),
            shape = shape,
            contentDescription = song.title,
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.Black.copy(alpha = 0.05f),
                                    Color.Black.copy(alpha = 0.75f),
                                ),
                        ),
                    ),
        )
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(CoreUiR.string.core_ui_now_playing),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.75f),
                )
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(
                onClick = onPlayPauseClick,
                modifier =
                    Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
            ) {
                Icon(
                    imageVector = if (isPlaying) AppIcons.Pause else AppIcons.PlayCircleFilled,
                    contentDescription =
                        if (isPlaying) {
                            stringResource(CoreUiR.string.core_ui_pause)
                        } else {
                            stringResource(CoreUiR.string.core_ui_play)
                        },
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun StaggeredEntrance(
    index: Int,
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        content()
        return
    }
    var visible by remember(index) { mutableStateOf(false) }
    LaunchedEffect(index) {
        kotlinx.coroutines.delay(index * 60L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(320)) + slideInVertically(tween(320)) { it / 4 },
    ) {
        content()
    }
}
