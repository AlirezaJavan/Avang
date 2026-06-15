package com.javanapps.musicplayer.feature.library.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
object LibraryRoute

@Serializable
data class AlbumDetailRoute(
    val albumId: Long,
)

@Serializable
data class ArtistDetailRoute(
    val artistId: Long,
)

fun NavController.navigateToLibrary(navOptions: NavOptions? = null) {
    navigate(LibraryRoute, navOptions)
}

fun NavController.navigateToAlbumDetail(albumId: Long) {
    navigate(AlbumDetailRoute(albumId))
}

fun NavController.navigateToArtistDetail(artistId: Long) {
    navigate(ArtistDetailRoute(artistId))
}
