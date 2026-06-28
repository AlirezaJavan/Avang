package com.javanapps.musicplayer.feature.favorites

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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.javanapps.musicplayer.core.ui.component.EmptyState
import com.javanapps.musicplayer.core.ui.component.ShimmerBox
import com.javanapps.musicplayer.core.ui.component.SongRow
import com.javanapps.musicplayer.feature.favorites.navigation.FavoritesRoute
import com.javanapps.musicplayer.core.ui.R as CoreUiR

fun NavGraphBuilder.favoritesScreen(onSongClick: (String) -> Unit) {
    composable<FavoritesRoute> {
        FavoritesScreen(onSongClick = onSongClick)
    }
}

@Composable
internal fun FavoritesScreen(
    onSongClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FavoritesScreen(
        uiState = uiState,
        onSongClick = { mediaId ->
            viewModel.play(mediaId)
            onSongClick(mediaId)
        },
        onToggleFavorite = viewModel::toggleFavorite,
        modifier = modifier,
    )
}

@Composable
internal fun FavoritesScreen(
    uiState: FavoritesUiState,
    onSongClick: (String) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (val state = uiState) {
                FavoritesUiState.Loading -> {
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
                is FavoritesUiState.Success -> {
                    if (state.songs.isEmpty()) {
                        EmptyState(
                            message = stringResource(CoreUiR.string.core_ui_no_songs),
                            icon = Icons.Default.Favorite,
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(
                                items = state.songs,
                                key = { it.id },
                                contentType = { "song" },
                            ) { song ->
                                SongRow(
                                    song = song,
                                    onClick = { onSongClick(song.mediaId) },
                                    modifier = Modifier.animateItem(),
                                    trailingContent = {
                                        IconButton(onClick = { onToggleFavorite(song.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = "Remove from favorites",
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
