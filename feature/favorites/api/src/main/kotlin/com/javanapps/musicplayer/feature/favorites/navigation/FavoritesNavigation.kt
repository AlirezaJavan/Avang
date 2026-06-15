package com.javanapps.musicplayer.feature.favorites.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
object FavoritesRoute

fun NavController.navigateToFavorites(navOptions: NavOptions? = null) {
    navigate(FavoritesRoute, navOptions)
}
