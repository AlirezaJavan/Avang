package com.javanapps.musicplayer.core.ui.transition

object PlayerTransitionKeys {
    fun artwork(songId: Long) = "player_artwork_$songId"

    fun title(songId: Long) = "player_title_$songId"

    fun artist(songId: Long) = "player_artist_$songId"
}
