package com.javanapps.musicplayer.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.javanapps.musicplayer.feature.equalizer.equalizerScreen
import com.javanapps.musicplayer.feature.equalizer.navigation.navigateToEqualizer
import com.javanapps.musicplayer.feature.favorites.favoritesScreen
import com.javanapps.musicplayer.feature.library.libraryScreen
import com.javanapps.musicplayer.feature.library.navigation.navigateToAlbumDetail
import com.javanapps.musicplayer.feature.library.navigation.navigateToArtistDetail
import com.javanapps.musicplayer.feature.notes.notesScreen
import com.javanapps.musicplayer.feature.player.navigation.navigateToPlayer
import com.javanapps.musicplayer.feature.player.playerScreen
import com.javanapps.musicplayer.feature.playlists.navigation.navigateToPlaylistDetail
import com.javanapps.musicplayer.feature.playlists.navigation.navigateToSmartPlaylistDetail
import com.javanapps.musicplayer.feature.playlists.playlistsScreen
import com.javanapps.musicplayer.feature.settings.settingsScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.LIBRARY.route,
        modifier = modifier,
        enterTransition = {
            val targetIndex = getTabIndex(targetState.destination)
            val currentIndex = getTabIndex(initialState.destination)
            val factor = if (targetIndex >= currentIndex) 1 else -1
            fadeIn() + slideInHorizontally { factor * it }
        },
        exitTransition = {
            val targetIndex = getTabIndex(targetState.destination)
            val currentIndex = getTabIndex(initialState.destination)
            val factor = if (targetIndex >= currentIndex) -1 else 1
            fadeOut() + slideOutHorizontally { factor * it }
        },
        popEnterTransition = {
            val targetIndex = getTabIndex(targetState.destination)
            val currentIndex = getTabIndex(initialState.destination)
            val factor = if (targetIndex >= currentIndex) 1 else -1
            fadeIn() + slideInHorizontally { factor * it }
        },
        popExitTransition = {
            val targetIndex = getTabIndex(targetState.destination)
            val currentIndex = getTabIndex(initialState.destination)
            val factor = if (targetIndex >= currentIndex) -1 else 1
            fadeOut() + slideOutHorizontally { factor * it }
        },
    ) {
        libraryScreen(
            onSongClick = { navController.navigateToPlayer() },
            onAlbumClick = { albumId -> navController.navigateToAlbumDetail(albumId) },
            onArtistClick = { artistId -> navController.navigateToArtistDetail(artistId) },
        ) { navController.popBackStack() }
        playerScreen(
            onBack = { navController.popBackStack() },
            onEqualizerClick = { navController.navigateToEqualizer() },
            sharedTransitionScope = sharedTransitionScope,
        )
        playlistsScreen(
            onPlaylistClick = { playlistId -> navController.navigateToPlaylistDetail(playlistId) },
            onSmartPlaylistClick = { label -> navController.navigateToSmartPlaylistDetail(label) },
            onSongClick = { navController.navigateToPlayer() },
            onBack = { navController.popBackStack() },
        )
        favoritesScreen(
            onSongClick = { navController.navigateToPlayer() },
        )
        equalizerScreen(onBack = { navController.popBackStack() })
        notesScreen()
        settingsScreen()
    }
}

private fun getTabIndex(destination: NavDestination): Int =
    TopLevelDestination.entries.indexOfFirst {
        destination.hasRoute(it.route::class)
    }
