package com.javanapps.musicplayer.core.domain.repository

import com.javanapps.musicplayer.core.model.DarkThemeConfig
import com.javanapps.musicplayer.core.model.RepeatMode
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    val userData: Flow<UserData>

    suspend fun setShuffleMode(shuffleMode: Boolean)

    suspend fun setRepeatMode(repeatMode: RepeatMode)

    suspend fun setLastPlayedSong(
        songId: Long,
        position: Long,
    )

    suspend fun setDynamicColor(dynamicColor: Boolean)

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)

    suspend fun setLanguage(language: String)

    suspend fun setLastQueue(
        songIds: List<Long>,
        currentIndex: Int,
    )

    suspend fun setEqualizerEnabled(enabled: Boolean)

    suspend fun setEqualizerBandLevels(levels: List<Short>)

    suspend fun setEqualizerPreset(preset: Int)
}

data class UserData(
    val shuffleMode: Boolean,
    val repeatMode: RepeatMode,
    val lastPlayedSongId: Long?,
    val lastPlaybackPosition: Long,
    val dynamicColor: Boolean,
    val darkThemeConfig: DarkThemeConfig,
    val lastQueueSongIds: List<Long> = emptyList(),
    val lastQueueIndex: Int = 0,
    val language: String,
    val equalizerEnabled: Boolean = false,
    val equalizerBandLevels: List<Short> = emptyList(),
    val equalizerPreset: Int = -1,
)
