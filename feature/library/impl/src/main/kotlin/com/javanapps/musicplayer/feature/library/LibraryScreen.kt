package com.javanapps.musicplayer.feature.library

import android.Manifest
import android.os.Build
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.javanapps.musicplayer.core.model.Album
import com.javanapps.musicplayer.core.model.Artist
import com.javanapps.musicplayer.core.model.Playlist
import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.core.ui.component.ArtworkImage
import com.javanapps.musicplayer.core.ui.component.EmptyState
import com.javanapps.musicplayer.core.ui.component.ScreenHeader
import com.javanapps.musicplayer.core.ui.component.ShimmerBox
import com.javanapps.musicplayer.core.ui.component.SongRow
import com.javanapps.musicplayer.core.ui.icon.AppIcons
import com.javanapps.musicplayer.core.ui.util.clickableScale
import com.javanapps.musicplayer.feature.library.navigation.AlbumDetailRoute
import com.javanapps.musicplayer.feature.library.navigation.ArtistDetailRoute
import com.javanapps.musicplayer.feature.library.navigation.LibraryRoute
import kotlinx.coroutines.launch
import com.javanapps.musicplayer.core.ui.R as CoreUiR

fun NavGraphBuilder.libraryScreen(
    onSongClick: (String) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onArtistClick: (Long) -> Unit,
    onBackClick: () -> Unit,
) {
    composable<LibraryRoute> {
        LibraryScreen(
            onSongClick = onSongClick,
            onAlbumClick = onAlbumClick,
            onArtistClick = onArtistClick,
        )
    }
    composable<AlbumDetailRoute> {
        AlbumDetailScreen(onSongClick = onSongClick, onBackClick = onBackClick)
    }
    composable<ArtistDetailRoute> {
        ArtistDetailScreen(onSongClick = onSongClick, onBackClick = onBackClick)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun LibraryScreen(
    onSongClick: (String) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onArtistClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val permission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    val permissionState = rememberPermissionState(permission)

    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            viewModel.refresh()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (permissionState.status.isGranted) {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
            val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
            val playlists by viewModel.playlists.collectAsStateWithLifecycle()
            val favoriteSongIds by viewModel.favoriteSongIds.collectAsStateWithLifecycle()

            val noteForSelectedSong by viewModel.noteForSelectedSong.collectAsStateWithLifecycle()
            var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }
            var songToAddNote by remember { mutableStateOf<Song?>(null) }

            LibraryScreen(
                uiState = uiState,
                searchQuery = searchQuery,
                sortOrder = sortOrder,
                favoriteSongIds = favoriteSongIds,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onSortOrderChanged = viewModel::onSortOrderChanged,
                onSongClick = { mediaId ->
                    viewModel.play(mediaId)
                    onSongClick(mediaId)
                },
                onAlbumClick = onAlbumClick,
                onArtistClick = onArtistClick,
                onToggleFavorite = viewModel::toggleFavorite,
                onPlayNext = viewModel::playNext,
                onAddToQueue = viewModel::addToQueue,
                onAddToPlaylistClick = { songToAddToPlaylist = it },
                onAddNoteClick = { song ->
                    viewModel.selectSongForNote(song.id)
                    songToAddNote = song
                },
                onRefresh = viewModel::refresh,
            )

            if (songToAddToPlaylist != null) {
                AddToPlaylistDialog(
                    playlists = playlists,
                    onDismiss = { songToAddToPlaylist = null },
                    onPlaylistSelected = { playlistId ->
                        viewModel.addToPlaylist(playlistId, songToAddToPlaylist!!.id)
                        songToAddToPlaylist = null
                    },
                )
            }

            if (songToAddNote != null) {
                NoteDialog(
                    initialNote = noteForSelectedSong?.note ?: "",
                    onDismiss = {
                        viewModel.selectSongForNote(null)
                        songToAddNote = null
                    },
                    onConfirm = { noteContent ->
                        viewModel.saveNote(songToAddNote!!.id, noteContent)
                        viewModel.selectSongForNote(null)
                        songToAddNote = null
                    },
                )
            }
        } else {
            PermissionRationale(
                shouldShowRationale = permissionState.status.shouldShowRationale,
                onRequestPermission = { permissionState.launchPermissionRequest() },
            )
        }
    }
}

@Composable
internal fun LibraryScreen(
    uiState: LibraryUiState,
    searchQuery: String,
    sortOrder: SortOrder,
    favoriteSongIds: Set<Long>,
    onSearchQueryChanged: (String) -> Unit,
    onSortOrderChanged: (SortOrder) -> Unit,
    onSongClick: (String) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onArtistClick: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onAddNoteClick: (Song) -> Unit,
    onRefresh: () -> Unit,
) {
    val tabs = LibraryTab.entries
    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        LibrarySearchBar(
            query = searchQuery,
            onQueryChanged = onSearchQueryChanged,
            sortOrder = sortOrder,
            onSortOrderChanged = onSortOrderChanged,
            modifier = Modifier.padding(16.dp),
        )

        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {},
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(text = stringResource(tab.titleRes)) },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            when (tabs[page]) {
                LibraryTab.SONGS -> {
                    when (uiState) {
                        LibraryUiState.Loading -> {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                        is LibraryUiState.Success -> {
                            if (uiState.songs.isEmpty()) {
                                EmptyState(
                                    message = stringResource(CoreUiR.string.core_ui_no_songs),
                                    icon = AppIcons.MusicNote,
                                    action = {
                                        Button(onClick = onRefresh) {
                                            Text(stringResource(CoreUiR.string.core_ui_scan))
                                        }
                                    },
                                )
                            } else {
                                SongsList(
                                    songs = uiState.songs,
                                    favoriteSongIds = favoriteSongIds,
                                    onSongClick = onSongClick,
                                    onToggleFavorite = onToggleFavorite,
                                    onPlayNext = onPlayNext,
                                    onAddToQueue = onAddToQueue,
                                    onAddToPlaylistClick = onAddToPlaylistClick,
                                    onAddNoteClick = onAddNoteClick,
                                )
                            }
                        }
                    }
                }
                LibraryTab.ALBUMS -> {
                    if (uiState is LibraryUiState.Success) {
                        AlbumsList(albums = uiState.albums, onAlbumClick = onAlbumClick)
                    }
                }
                LibraryTab.ARTISTS -> {
                    if (uiState is LibraryUiState.Success) {
                        ArtistsList(artists = uiState.artists, onArtistClick = onArtistClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun LibrarySearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChanged: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(CoreUiR.string.core_ui_search)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
        )
        Box {
            IconButton(onClick = { sortMenuExpanded = true }) {
                Icon(
                    imageVector = AppIcons.Sort,
                    contentDescription = "Sort",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false },
            ) {
                SortOrder.entries.forEach { order ->
                    DropdownMenuItem(
                        text = {
                            val labelRes =
                                when (order) {
                                    SortOrder.TITLE -> CoreUiR.string.core_ui_sort_title
                                    SortOrder.ARTIST -> CoreUiR.string.core_ui_sort_artist
                                    SortOrder.DATE_ADDED -> CoreUiR.string.core_ui_sort_date
                                    SortOrder.DURATION -> CoreUiR.string.core_ui_sort_duration
                                }
                            Text(
                                text = stringResource(labelRes),
                                color =
                                    if (order == sortOrder) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                            )
                        },
                        onClick = {
                            onSortOrderChanged(order)
                            sortMenuExpanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SongsList(
    songs: List<Song>,
    favoriteSongIds: Set<Long>,
    onSongClick: (String) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onAddNoteClick: (Song) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            items = songs,
            key = { it.id },
            contentType = { "song" },
        ) { song ->
            val isFavorite = song.id in favoriteSongIds
            SongRow(
                song = song,
                onClick = { onSongClick(song.mediaId) },
                modifier = Modifier.animateItem(),
                trailingContent = {
                    SongOverflowMenu(
                        isFavorite = isFavorite,
                        onFavoriteClick = { onToggleFavorite(song.id) },
                        onPlayNextClick = { onPlayNext(song) },
                        onAddToQueueClick = { onAddToQueue(song) },
                        onAddToPlaylistClick = { onAddToPlaylistClick(song) },
                        onAddNoteClick = { onAddNoteClick(song) },
                    )
                },
            )
        }
    }
}

@Composable
private fun AlbumsList(
    albums: List<Album>,
    onAlbumClick: (Long) -> Unit,
) {
    if (albums.isEmpty()) {
        EmptyState(
            message = stringResource(CoreUiR.string.core_ui_no_albums),
            icon = AppIcons.MusicNote,
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                items = albums,
                key = { it.id },
                contentType = { "album" },
            ) { album ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .clickableScale { onAlbumClick(album.id) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ArtworkImage(
                        artworkUri = album.artworkUri,
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = album.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text =
                                stringResource(
                                    CoreUiR.string.core_ui_artist_album_count,
                                    album.artist,
                                    album.songCount,
                                ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistsList(
    artists: List<Artist>,
    onArtistClick: (Long) -> Unit,
) {
    if (artists.isEmpty()) {
        EmptyState(
            message = stringResource(CoreUiR.string.core_ui_no_artists),
            icon = AppIcons.MusicNote,
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                items = artists,
                key = { it.id },
                contentType = { "artist" },
            ) { artist ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .clickableScale { onArtistClick(artist.id) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = AppIcons.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = artist.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text =
                                stringResource(
                                    CoreUiR.string.core_ui_artist_album_track_count,
                                    artist.albumCount,
                                    artist.songCount,
                                ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun AlbumDetailScreen(
    onSongClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(CoreUiR.string.core_ui_album_details),
            onBack = onBackClick,
        )

        when (val state = uiState) {
            AlbumDetailUiState.Loading -> { /* Loading */ }
            AlbumDetailUiState.Error -> {
                EmptyState(
                    message = stringResource(CoreUiR.string.core_ui_no_albums),
                    icon = AppIcons.MusicNote,
                )
            }
            is AlbumDetailUiState.Success -> {
                LazyColumn {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            ArtworkImage(
                                artworkUri = state.songs.firstOrNull()?.artworkUri,
                                modifier = Modifier.size(200.dp),
                            )
                            Text(
                                text = state.songs.firstOrNull()?.album ?: "",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(top = 16.dp),
                            )
                            Text(
                                text = state.songs.firstOrNull()?.artist ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
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
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ArtistDetailScreen(
    onSongClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArtistDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(CoreUiR.string.core_ui_artist_details),
            onBack = onBackClick,
        )

        when (val state = uiState) {
            ArtistDetailUiState.Loading -> { /* Loading */ }
            ArtistDetailUiState.Error -> {
                EmptyState(
                    message = stringResource(CoreUiR.string.core_ui_no_artists),
                    icon = AppIcons.MusicNote,
                )
            }
            is ArtistDetailUiState.Success -> {
                LazyColumn {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = AppIcons.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = state.songs.firstOrNull()?.artist ?: "",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(top = 16.dp),
                            )
                        }
                    }
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
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SongOverflowMenu(
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onPlayNextClick: () -> Unit,
    onAddToQueueClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onAddNoteClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(CoreUiR.string.core_ui_favorite)) },
                leadingIcon = {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                    )
                },
                onClick = {
                    onFavoriteClick()
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(CoreUiR.string.core_ui_play_next)) },
                leadingIcon = { Icon(AppIcons.PlaylistPlay, contentDescription = null) },
                onClick = {
                    onPlayNextClick()
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(CoreUiR.string.core_ui_add_to_queue)) },
                leadingIcon = { Icon(AppIcons.QueueMusic, contentDescription = null) },
                onClick = {
                    onAddToQueueClick()
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(CoreUiR.string.core_ui_create_playlist)) },
                leadingIcon = { Icon(AppIcons.PlaylistAdd, contentDescription = null) },
                onClick = {
                    onAddToPlaylistClick()
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(CoreUiR.string.core_ui_add_note)) },
                leadingIcon = { Icon(AppIcons.Notes, contentDescription = null) },
                onClick = {
                    onAddNoteClick()
                    expanded = false
                },
            )
        }
    }
}

@Composable
private fun NoteDialog(
    initialNote: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var note by remember(initialNote) { mutableStateOf(initialNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(CoreUiR.string.core_ui_add_note)) },
        text = {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(CoreUiR.string.core_ui_note_content)) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (note.isNotBlank()) onConfirm(note) },
                enabled = note.isNotBlank(),
            ) {
                Text(stringResource(CoreUiR.string.core_ui_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(CoreUiR.string.core_ui_cancel))
            }
        },
    )
}

@Composable
private fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Long) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(CoreUiR.string.core_ui_create_playlist)) },
        text = {
            if (playlists.isEmpty()) {
                Text(text = stringResource(CoreUiR.string.core_ui_no_playlists))
            } else {
                LazyColumn {
                    items(
                        items = playlists,
                        key = { it.id },
                        contentType = { "playlist" },
                    ) { playlist ->
                        Text(
                            text = playlist.name,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onPlaylistSelected(playlist.id) }
                                    .padding(16.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(CoreUiR.string.core_ui_cancel))
            }
        },
    )
}

@Composable
private fun PermissionRationale(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text =
                    if (shouldShowRationale) {
                        stringResource(CoreUiR.string.core_ui_permission_rationale)
                    } else {
                        stringResource(CoreUiR.string.core_ui_permission_required)
                    },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            Button(onClick = onRequestPermission) {
                Text(stringResource(CoreUiR.string.core_ui_grant_permission))
            }
        }
    }
}

private enum class LibraryTab(
    val titleRes: Int,
) {
    SONGS(CoreUiR.string.core_ui_songs),
    ALBUMS(CoreUiR.string.core_ui_albums),
    ARTISTS(CoreUiR.string.core_ui_artists),
}
