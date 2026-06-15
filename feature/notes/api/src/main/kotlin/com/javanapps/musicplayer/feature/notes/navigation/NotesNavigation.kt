package com.javanapps.musicplayer.feature.notes.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
object NotesRoute

fun NavController.navigateToNotes(navOptions: NavOptions? = null) {
    navigate(NotesRoute, navOptions)
}
