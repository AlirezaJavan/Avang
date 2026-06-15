package com.javanapps.musicplayer.core.testing.repository

import com.javanapps.musicplayer.core.domain.repository.UserData
import com.javanapps.musicplayer.core.domain.repository.UserDataRepository
import com.javanapps.musicplayer.core.model.DarkThemeConfig
import com.javanapps.musicplayer.core.model.RepeatMode
import com.javanapps.musicplayer.core.testing.BuildConfig
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeUserDataRepository : UserDataRepository {
    private val _userData =
        MutableSharedFlow<UserData>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private var current =
        UserData(
            shuffleMode = false,
            repeatMode = RepeatMode.NONE,
            lastPlayedSongId = null,
            lastPlaybackPosition = 0L,
            dynamicColor = true,
            darkThemeConfig = DarkThemeConfig.DARK,
            language = BuildConfig.DEFAULT_LANGUAGE,
        )

    init {
        _userData.tryEmit(current)
    }

    override val userData: Flow<UserData> = _userData

    override suspend fun setShuffleMode(shuffleMode: Boolean) {
        current = current.copy(shuffleMode = shuffleMode)
        _userData.tryEmit(current)
    }

    override suspend fun setRepeatMode(repeatMode: RepeatMode) {
        current = current.copy(repeatMode = repeatMode)
        _userData.tryEmit(current)
    }

    override suspend fun setLastPlayedSong(
        songId: Long,
        position: Long,
    ) {
        current = current.copy(lastPlayedSongId = songId, lastPlaybackPosition = position)
        _userData.tryEmit(current)
    }

    override suspend fun setDynamicColor(dynamicColor: Boolean) {
        current = current.copy(dynamicColor = dynamicColor)
        _userData.tryEmit(current)
    }

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        current = current.copy(darkThemeConfig = darkThemeConfig)
        _userData.tryEmit(current)
    }

    override suspend fun setLanguage(language: String) {
        current = current.copy(language = language)
        _userData.tryEmit(current)
    }

    override suspend fun setLastQueue(
        songIds: List<Long>,
        currentIndex: Int,
    ) {
        current = current.copy(lastQueueSongIds = songIds, lastQueueIndex = currentIndex)
        _userData.tryEmit(current)
    }

    override suspend fun setEqualizerEnabled(enabled: Boolean) {
        current = current.copy(equalizerEnabled = enabled)
        _userData.tryEmit(current)
    }

    override suspend fun setEqualizerBandLevels(levels: List<Short>) {
        current = current.copy(equalizerBandLevels = levels)
        _userData.tryEmit(current)
    }

    override suspend fun setEqualizerPreset(preset: Int) {
        current = current.copy(equalizerPreset = preset)
        _userData.tryEmit(current)
    }
}
