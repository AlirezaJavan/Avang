package com.javanapps.musicplayer.feature.playlists.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
object PlaylistsRoute

@Serializable
data class PlaylistDetailRoute(
    val playlistId: Long,
)

@Serializable
data class SmartPlaylistDetailRoute(
    val label: String,
)

fun NavController.navigateToPlaylists(navOptions: NavOptions? = null) {
    navigate(PlaylistsRoute, navOptions)
}

fun NavController.navigateToPlaylistDetail(playlistId: Long) {
    navigate(PlaylistDetailRoute(playlistId))
}

fun NavController.navigateToSmartPlaylistDetail(label: String) {
    navigate(SmartPlaylistDetailRoute(label))
}
