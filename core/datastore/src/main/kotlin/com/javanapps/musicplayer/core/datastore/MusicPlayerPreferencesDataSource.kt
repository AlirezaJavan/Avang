package com.javanapps.musicplayer.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.javanapps.musicplayer.core.model.DarkThemeConfig
import com.javanapps.musicplayer.core.model.RepeatMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class MusicPlayerPreferencesDataSource
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        val userData: Flow<UserPreferences> =
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }.map { preferences ->
                    UserPreferences(
                        shuffleMode = preferences[Keys.SHUFFLE_MODE] ?: false,
                        repeatMode =
                            RepeatMode.valueOf(
                                preferences[Keys.REPEAT_MODE] ?: RepeatMode.NONE.name,
                            ),
                        lastPlayedSongId = preferences[Keys.LAST_PLAYED_SONG_ID],
                        lastPlaybackPosition = preferences[Keys.LAST_PLAYBACK_POSITION] ?: 0L,
                        dynamicColor = preferences[Keys.DYNAMIC_COLOR] ?: false,
                        darkThemeConfig =
                            DarkThemeConfig.valueOf(
                                preferences[Keys.DARK_THEME_CONFIG] ?: DarkThemeConfig.DARK.name,
                            ),
                        lastQueueSongIds =
                            preferences[Keys.LAST_QUEUE_SONG_IDS]
                                ?.split(",")
                                ?.mapNotNull { it.toLongOrNull() }
                                ?: emptyList(),
                        lastQueueIndex = preferences[Keys.LAST_QUEUE_INDEX] ?: 0,
                        language = preferences[Keys.LANGUAGE] ?: BuildConfig.DEFAULT_LANGUAGE,
                        equalizerEnabled = preferences[Keys.EQUALIZER_ENABLED] ?: false,
                        equalizerBandLevels =
                            preferences[Keys.EQUALIZER_BAND_LEVELS]
                                ?.split(",")
                                ?.mapNotNull { it.toShortOrNull() }
                                ?: emptyList(),
                        equalizerPreset = preferences[Keys.EQUALIZER_PRESET] ?: -1,
                        useAnimations = preferences[Keys.USE_ANIMATIONS] ?: false,
                    )
                }

        suspend fun setShuffleMode(shuffleMode: Boolean) {
            dataStore.edit { preferences ->
                preferences[Keys.SHUFFLE_MODE] = shuffleMode
            }
        }

        suspend fun setRepeatMode(repeatMode: RepeatMode) {
            dataStore.edit { preferences ->
                preferences[Keys.REPEAT_MODE] = repeatMode.name
            }
        }

        suspend fun setLastPlayedSong(
            songId: Long,
            position: Long,
        ) {
            dataStore.edit { preferences ->
                preferences[Keys.LAST_PLAYED_SONG_ID] = songId
                preferences[Keys.LAST_PLAYBACK_POSITION] = position
            }
        }

        suspend fun setDynamicColor(dynamicColor: Boolean) {
            dataStore.edit { preferences ->
                preferences[Keys.DYNAMIC_COLOR] = dynamicColor
            }
        }

        suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
            dataStore.edit { preferences ->
                preferences[Keys.DARK_THEME_CONFIG] = darkThemeConfig.name
            }
        }

        suspend fun setLanguage(language: String) {
            dataStore.edit { preferences ->
                preferences[Keys.LANGUAGE] = language
            }
        }

        suspend fun setLastQueue(
            songIds: List<Long>,
            currentIndex: Int,
        ) {
            dataStore.edit { preferences ->
                preferences[Keys.LAST_QUEUE_SONG_IDS] = songIds.joinToString(",")
                preferences[Keys.LAST_QUEUE_INDEX] = currentIndex
            }
        }

        suspend fun setEqualizerEnabled(enabled: Boolean) {
            dataStore.edit { preferences ->
                preferences[Keys.EQUALIZER_ENABLED] = enabled
            }
        }

        suspend fun setEqualizerBandLevels(levels: List<Short>) {
            dataStore.edit { preferences ->
                preferences[Keys.EQUALIZER_BAND_LEVELS] = levels.joinToString(",")
            }
        }

        suspend fun setEqualizerPreset(preset: Int) {
            dataStore.edit { preferences ->
                preferences[Keys.EQUALIZER_PRESET] = preset
            }
        }

        suspend fun setUseAnimations(useAnimations: Boolean) {
            dataStore.edit { preferences ->
                preferences[Keys.USE_ANIMATIONS] = useAnimations
            }
        }

        private object Keys {
            val SHUFFLE_MODE = booleanPreferencesKey("shuffle_mode")
            val REPEAT_MODE = stringPreferencesKey("repeat_mode")
            val LAST_PLAYED_SONG_ID = longPreferencesKey("last_played_song_id")
            val LAST_PLAYBACK_POSITION = longPreferencesKey("last_playback_position")
            val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
            val DARK_THEME_CONFIG = stringPreferencesKey("dark_theme_config")
            val LAST_QUEUE_SONG_IDS = stringPreferencesKey("last_queue_song_ids")
            val LAST_QUEUE_INDEX = intPreferencesKey("last_queue_index")
            val LANGUAGE = stringPreferencesKey("language")
            val EQUALIZER_ENABLED = booleanPreferencesKey("equalizer_enabled")
            val EQUALIZER_BAND_LEVELS = stringPreferencesKey("equalizer_band_levels")
            val EQUALIZER_PRESET = intPreferencesKey("equalizer_preset")
            val USE_ANIMATIONS = booleanPreferencesKey("use_animations")
        }
    }

data class UserPreferences(
    val shuffleMode: Boolean,
    val repeatMode: RepeatMode,
    val lastPlayedSongId: Long?,
    val lastPlaybackPosition: Long,
    val dynamicColor: Boolean,
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.DARK,
    val lastQueueSongIds: List<Long> = emptyList(),
    val lastQueueIndex: Int = 0,
    val language: String = BuildConfig.DEFAULT_LANGUAGE,
    val equalizerEnabled: Boolean = false,
    val equalizerBandLevels: List<Short> = emptyList(),
    val equalizerPreset: Int = -1,
    val useAnimations: Boolean = false,
)
