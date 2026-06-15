package com.javanapps.musicplayer.feature.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.javanapps.musicplayer.core.model.Playlist
import com.javanapps.musicplayer.core.model.SmartPlaylist
import com.javanapps.musicplayer.core.ui.component.EmptyState
import com.javanapps.musicplayer.core.ui.component.ShimmerBox
import com.javanapps.musicplayer.core.ui.component.SongRow
import com.javanapps.musicplayer.core.ui.icon.AppIcons
import com.javanapps.musicplayer.feature.playlists.navigation.PlaylistDetailRoute
import com.javanapps.musicplayer.feature.playlists.navigation.PlaylistsRoute
import com.javanapps.musicplayer.feature.playlists.navigation.SmartPlaylistDetailRoute
import com.javanapps.musicplayer.core.ui.R as CoreUiR

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
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (smartPlaylists.isNotEmpty()) {
            item(key = "smart_header", contentType = "header") {
                Text(
                    text = stringResource(CoreUiR.string.core_ui_smart_playlists),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            items(
                items = smartPlaylists,
                key = { "smart_${it.label}" },
                contentType = { "smart_playlist" },
            ) { smartPlaylist ->
                SmartPlaylistRow(
                    smartPlaylist = smartPlaylist,
                    onClick = { onSmartPlaylistClick(smartPlaylist.label) },
                )
            }
        }
        items(
            items = playlists,
            key = { it.id },
            contentType = { "playlist" },
        ) { playlist ->
            var showRenameDialog by remember { mutableStateOf(false) }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .clickable { onPlaylistClick(playlist.id) }
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = AppIcons.PlaylistPlay,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = stringResource(CoreUiR.string.core_ui_playlist_details),
                style = MaterialTheme.typography.titleLarge,
            )
        }

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
private fun SmartPlaylistRow(
    smartPlaylist: SmartPlaylist,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = AppIcons.PlaylistPlay,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.secondary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = smartPlaylist.label, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(CoreUiR.string.core_ui_song_count, smartPlaylist.songCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(
                text = (uiState as? SmartPlaylistDetailUiState.Success)?.label.orEmpty(),
                style = MaterialTheme.typography.titleLarge,
            )
        }

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
