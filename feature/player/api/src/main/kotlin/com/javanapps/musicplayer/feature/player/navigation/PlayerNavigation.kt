package com.javanapps.musicplayer.feature.player.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
object PlayerRoute

fun NavController.navigateToPlayer(navOptions: NavOptions? = null) {
    navigate(PlayerRoute, navOptions)
}
