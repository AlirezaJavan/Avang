package com.javanapps.musicplayer.feature.equalizer.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
object EqualizerRoute

fun NavController.navigateToEqualizer(navOptions: NavOptions? = null) {
    navigate(EqualizerRoute, navOptions)
}
