package com.javanapps.musicplayer.core.data.repository

import com.javanapps.musicplayer.core.datastore.MusicPlayerPreferencesDataSource
import com.javanapps.musicplayer.core.domain.repository.UserData
import com.javanapps.musicplayer.core.domain.repository.UserDataRepository
import com.javanapps.musicplayer.core.model.DarkThemeConfig
import com.javanapps.musicplayer.core.model.RepeatMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineUserDataRepository
    @Inject
    constructor(
        private val musicPlayerPreferencesDataSource: MusicPlayerPreferencesDataSource,
    ) : UserDataRepository {
        override val userData: Flow<UserData> =
            musicPlayerPreferencesDataSource.userData.map {
                UserData(
                    shuffleMode = it.shuffleMode,
                    repeatMode = it.repeatMode,
                    lastPlayedSongId = it.lastPlayedSongId,
                    lastPlaybackPosition = it.lastPlaybackPosition,
                    dynamicColor = it.dynamicColor,
                    darkThemeConfig = it.darkThemeConfig,
                    lastQueueSongIds = it.lastQueueSongIds,
                    lastQueueIndex = it.lastQueueIndex,
                    language = it.language,
                    equalizerEnabled = it.equalizerEnabled,
                    equalizerBandLevels = it.equalizerBandLevels,
                    equalizerPreset = it.equalizerPreset,
                )
            }

        override suspend fun setShuffleMode(shuffleMode: Boolean) = musicPlayerPreferencesDataSource.setShuffleMode(shuffleMode)

        override suspend fun setRepeatMode(repeatMode: RepeatMode) = musicPlayerPreferencesDataSource.setRepeatMode(repeatMode)

        override suspend fun setLastPlayedSong(
            songId: Long,
            position: Long,
        ) = musicPlayerPreferencesDataSource.setLastPlayedSong(songId, position)

        override suspend fun setDynamicColor(dynamicColor: Boolean) = musicPlayerPreferencesDataSource.setDynamicColor(dynamicColor)

        override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) =
            musicPlayerPreferencesDataSource.setDarkThemeConfig(darkThemeConfig)

        override suspend fun setLanguage(language: String) = musicPlayerPreferencesDataSource.setLanguage(language)

        override suspend fun setLastQueue(
            songIds: List<Long>,
            currentIndex: Int,
        ) = musicPlayerPreferencesDataSource.setLastQueue(songIds, currentIndex)

        override suspend fun setEqualizerEnabled(enabled: Boolean) = musicPlayerPreferencesDataSource.setEqualizerEnabled(enabled)

        override suspend fun setEqualizerBandLevels(levels: List<Short>) = musicPlayerPreferencesDataSource.setEqualizerBandLevels(levels)

        override suspend fun setEqualizerPreset(preset: Int) = musicPlayerPreferencesDataSource.setEqualizerPreset(preset)
    }
