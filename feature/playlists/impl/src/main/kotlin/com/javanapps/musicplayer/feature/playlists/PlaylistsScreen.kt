package com.javanapps.musicplayer.feature.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.javanapps.musicplayer.core.model.Playlist
import com.javanapps.musicplayer.core.model.SmartPlaylist
import com.javanapps.musicplayer.core.ui.component.EmptyState
import com.javanapps.musicplayer.core.ui.component.ScreenHeader
import com.javanapps.musicplayer.core.ui.component.ShimmerBox
import com.javanapps.musicplayer.core.ui.component.SongRow
import com.javanapps.musicplayer.core.ui.icon.AppIcons
import com.javanapps.musicplayer.core.ui.util.clickableScale
import com.javanapps.musicplayer.feature.playlists.navigation.PlaylistDetailRoute
import com.javanapps.musicplayer.feature.playlists.navigation.PlaylistsRoute
import com.javanapps.musicplayer.feature.playlists.navigation.SmartPlaylistDetailRoute
import kotlin.math.absoluteValue
import androidx.compose.foundation.lazy.grid.items as gridItems
import com.javanapps.musicplayer.core.ui.R as CoreUiR

private val smartPlaylistCardColors =
    mapOf(
        "80s" to Color(0xFF7C3AED),
        "90s" to Color(0xFFD946EF),
        "Acoustic" to Color(0xFFD97706),
        "Classical" to Color(0xFF4F46E5),
        "Dance" to Color(0xFF0EA5E9),
        "Energetic" to Color(0xFFEA580C),
        "Rock" to Color(0xFFE11D48),
        "Pop" to Color(0xFF059669),
    )

private val fallbackCardColors =
    listOf(
        Color(0xFF6366F1),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899),
        Color(0xFF14B8A6),
        Color(0xFF22C55E),
        Color(0xFFF59E0B),
    )

private fun smartPlaylistColor(label: String): Color =
    smartPlaylistCardColors[label]
        ?: fallbackCardColors[label.hashCode().absoluteValue % fallbackCardColors.size]

@Composable
private fun localizedSmartLabel(label: String): String =
    when (label) {
        "80s" -> stringResource(CoreUiR.string.core_ui_label_80s)
        "90s" -> stringResource(CoreUiR.string.core_ui_label_90s)
        "Acoustic" -> stringResource(CoreUiR.string.core_ui_label_acoustic)
        "Classical" -> stringResource(CoreUiR.string.core_ui_label_classical)
        "Dance" -> stringResource(CoreUiR.string.core_ui_label_dance)
        "Energetic" -> stringResource(CoreUiR.string.core_ui_label_energetic)
        "Rock" -> stringResource(CoreUiR.string.core_ui_label_rock)
        "Pop" -> stringResource(CoreUiR.string.core_ui_label_pop)
        else -> label
    }

fun NavGraphBuilder.playlistsScreen(
    onPlaylistClick: (Long) -> Unit,
    onSmartPlaylistClick: (String) -> Unit,
    onSongClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    composable<PlaylistsRoute> {
        PlaylistsScreen(onPlaylistClick = onPlaylistClick, onSmartPlaylistClick = onSmartPlaylistClick)
    }
    composable<PlaylistDetailRoute> {
        PlaylistDetailScreen(onSongClick = onSongClick, onBack = onBack)
    }
    composable<SmartPlaylistDetailRoute> {
        SmartPlaylistDetailScreen(onSongClick = onSongClick, onBack = onBack)
    }
}

@Composable
internal fun PlaylistsScreen(
    onPlaylistClick: (Long) -> Unit,
    onSmartPlaylistClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PlaylistsScreen(
        uiState = uiState,
        onPlaylistClick = onPlaylistClick,
        onSmartPlaylistClick = onSmartPlaylistClick,
        onCreatePlaylist = viewModel::createPlaylist,
        onDeletePlaylist = viewModel::deletePlaylist,
        onRenamePlaylist = viewModel::renamePlaylist,
        modifier = modifier,
    )
}

@Composable
internal fun PlaylistsScreen(
    uiState: PlaylistsUiState,
    onPlaylistClick: (Long) -> Unit,
    onSmartPlaylistClick: (String) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onRenamePlaylist: (Long, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(CoreUiR.string.core_ui_create_playlist))
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (uiState) {
                PlaylistsUiState.Loading -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(5) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ShimmerBox(modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    ShimmerBox(modifier = Modifier.width(150.dp).height(20.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ShimmerBox(modifier = Modifier.width(80.dp).height(16.dp))
                                }
                            }
                        }
                    }
                }
                is PlaylistsUiState.Success -> {
                    if (uiState.playlists.isEmpty() && uiState.smartPlaylists.isEmpty()) {
                        EmptyState(
                            message = stringResource(CoreUiR.string.core_ui_no_playlists),
                            icon = AppIcons.PlaylistPlay,
                        )
                    } else {
                        PlaylistsList(
                            playlists = uiState.playlists,
                            smartPlaylists = uiState.smartPlaylists,
                            onPlaylistClick = onPlaylistClick,
                            onSmartPlaylistClick = onSmartPlaylistClick,
                            onDeletePlaylist = onDeletePlaylist,
                            onRenamePlaylist = onRenamePlaylist,
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        PlaylistDialog(
            title = stringResource(CoreUiR.string.core_ui_create_playlist),
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                onCreatePlaylist(name)
                showCreateDialog = false
            },
        )
    }
}

@Composable
private fun PlaylistsList(
    playlists: List<Playlist>,
    smartPlaylists: List<SmartPlaylist>,
    onPlaylistClick: (Long) -> Unit,
    onSmartPlaylistClick: (String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onRenamePlaylist: (Long, String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // User playlists section
        if (playlists.isNotEmpty()) {
            item(
                key = "user_header",
                span = { GridItemSpan(maxCurrentLineSpan) },
                contentType = "header",
            ) {
                Text(
                    text = stringResource(CoreUiR.string.core_ui_my_playlists),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                )
            }
            gridItems(
                items = playlists,
                key = { it.id },
                span = { GridItemSpan(maxCurrentLineSpan) },
                contentType = { "playlist" },
            ) { playlist ->
                var showRenameDialog by remember { mutableStateOf(false) }

                val rowShape = MaterialTheme.shapes.medium
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .clip(rowShape)
                            .clickableScale { onPlaylistClick(playlist.id) }
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.45f), rowShape)
                            .padding(vertical = 10.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(48.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary,
                                        ),
                                    ),
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = AppIcons.PlaylistPlay,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = Color.White,
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = playlist.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = stringResource(CoreUiR.string.core_ui_song_count, playlist.songCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename")
                    }
                    IconButton(onClick = { onDeletePlaylist(playlist.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }

                if (showRenameDialog) {
                    PlaylistDialog(
                        title = stringResource(CoreUiR.string.core_ui_rename_playlist),
                        initialName = playlist.name,
                        onDismiss = { showRenameDialog = false },
                        onConfirm = { newName ->
                            onRenamePlaylist(playlist.id, newName)
                            showRenameDialog = false
                        },
                    )
                }
            }
        }

        // Separator + automatic playlists section
        if (smartPlaylists.isNotEmpty()) {
            item(
                key = "auto_separator",
                span = { GridItemSpan(maxCurrentLineSpan) },
                contentType = "separator",
            ) {
                Column {
                    if (playlists.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                    Text(
                        text = stringResource(CoreUiR.string.core_ui_automatic_playlists),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                    )
                }
            }
            gridItems(
                items = smartPlaylists,
                key = { "smart_${it.label}" },
                contentType = { "smart_playlist" },
            ) { smartPlaylist ->
                SmartPlaylistCard(
                    smartPlaylist = smartPlaylist,
                    onClick = { onSmartPlaylistClick(smartPlaylist.label) },
                )
            }
        }
    }
}

@Composable
private fun SmartPlaylistCard(
    smartPlaylist: SmartPlaylist,
    onClick: () -> Unit,
) {
    val cardColor = smartPlaylistColor(smartPlaylist.label)
    val localizedLabel = localizedSmartLabel(smartPlaylist.label)

    val shape = RoundedCornerShape(24.dp)
    val gradient =
        Brush.linearGradient(
            colors =
                listOf(
                    lerp(cardColor, Color.White, 0.12f),
                    cardColor,
                    lerp(cardColor, Color.Black, 0.42f),
                ),
        )

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(shape)
                .clickableScale(onClick = onClick)
                .background(gradient),
    ) {
        Icon(
            imageVector = AppIcons.MusicNote,
            contentDescription = null,
            modifier =
                Modifier
                    .size(96.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 4.dp)
                    .alpha(0.16f),
            tint = Color.White,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(18.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = localizedLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(CoreUiR.string.core_ui_song_count, smartPlaylist.songCount),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.82f),
            )
        }
    }
}

@Composable
internal fun PlaylistDetailScreen(
    onSongClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PlaylistDetailScreen(
        uiState = uiState,
        onSongClick = { mediaId ->
            viewModel.play(mediaId)
            onSongClick(mediaId)
        },
        onBack = onBack,
        onRemoveSong = viewModel::removeSong,
        modifier = modifier,
    )
}

@Composable
internal fun PlaylistDetailScreen(
    uiState: PlaylistDetailUiState,
    onSongClick: (String) -> Unit,
    onBack: () -> Unit,
    onRemoveSong: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(CoreUiR.string.core_ui_playlist_details),
            onBack = onBack,
        )

        when (uiState) {
            PlaylistDetailUiState.Loading -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            ShimmerBox(modifier = Modifier.size(120.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            ShimmerBox(modifier = Modifier.width(200.dp).height(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            ShimmerBox(modifier = Modifier.width(100.dp).height(16.dp))
                        }
                    }
                    items(10) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ShimmerBox(modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                ShimmerBox(modifier = Modifier.width(150.dp).height(16.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                ShimmerBox(modifier = Modifier.width(100.dp).height(12.dp))
                            }
                        }
                    }
                }
            }
            PlaylistDetailUiState.Error -> {
                EmptyState(message = "لیست پخش یافت نشد", icon = AppIcons.PlaylistPlay)
            }
            is PlaylistDetailUiState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = AppIcons.PlaylistPlay,
                                contentDescription = null,
                                modifier = Modifier.size(120.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = uiState.playlist.name,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(top = 16.dp),
                            )
                            Text(
                                text = "${uiState.songs.size} آهنگ",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    items(
                        items = uiState.songs,
                        key = { it.id },
                        contentType = { "song" },
                    ) { song ->
                        SongRow(
                            song = song,
                            onClick = { onSongClick(song.mediaId) },
                            modifier = Modifier.animateItem(),
                            trailingContent = {
                                IconButton(onClick = { onRemoveSong(song.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SmartPlaylistDetailScreen(
    onSongClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SmartPlaylistDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        val rawLabel = (uiState as? SmartPlaylistDetailUiState.Success)?.label.orEmpty()
        ScreenHeader(
            title = if (rawLabel.isEmpty()) "" else localizedSmartLabel(rawLabel),
            onBack = onBack,
        )

        when (val state = uiState) {
            SmartPlaylistDetailUiState.Loading -> Unit
            is SmartPlaylistDetailUiState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = state.songs,
                        key = { it.id },
                        contentType = { "song" },
                    ) { song ->
                        SongRow(
                            song = song,
                            onClick = {
                                viewModel.play(song.mediaId)
                                onSongClick(song.mediaId)
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistDialog(
    title: String,
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(CoreUiR.string.core_ui_playlist_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
            ) {
                Text(stringResource(if (initialName.isEmpty()) CoreUiR.string.core_ui_create else CoreUiR.string.core_ui_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(CoreUiR.string.core_ui_cancel))
            }
        },
    )
}
