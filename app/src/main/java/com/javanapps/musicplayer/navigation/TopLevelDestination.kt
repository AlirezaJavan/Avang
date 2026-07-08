package com.javanapps.musicplayer.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.javanapps.musicplayer.core.ui.R
import com.javanapps.musicplayer.core.ui.icon.AppIcons
import com.javanapps.musicplayer.feature.favorites.navigation.FavoritesRoute
import com.javanapps.musicplayer.feature.home.navigation.HomeRoute
import com.javanapps.musicplayer.feature.library.navigation.LibraryRoute
import com.javanapps.musicplayer.feature.playlists.navigation.PlaylistsRoute
import com.javanapps.musicplayer.feature.settings.navigation.SettingsRoute

enum class TopLevelDestination(
    val route: Any,
    val icon: ImageVector,
    @StringRes val labelRes: Int,
) {
    HOME(HomeRoute, AppIcons.Home, R.string.core_ui_home),
    LIBRARY(LibraryRoute, AppIcons.LibraryMusic, R.string.core_ui_library),
    PLAYLISTS(PlaylistsRoute, AppIcons.PlaylistPlay, R.string.core_ui_playlists),
    FAVORITES(FavoritesRoute, Icons.Default.FavoriteBorder, R.string.core_ui_favorites),
    SETTINGS(SettingsRoute, Icons.Default.Settings, R.string.core_ui_settings),
}
