package com.javanapps.musicplayer.core.domain.repository

import com.javanapps.musicplayer.core.model.SmartPlaylist
import kotlinx.coroutines.flow.Flow

interface AnalysisRepository {
    suspend fun analyzeSong(
        songId: Long,
        mediaUri: String,
    )

    suspend fun analyzedSongIds(): Set<Long>

    fun observeSmartPlaylists(minConfidence: Float = DEFAULT_MIN_CONFIDENCE): Flow<List<SmartPlaylist>>

    fun observeLabels(): Flow<List<String>>

    fun observeSongIdsForLabel(
        label: String,
        minConfidence: Float = DEFAULT_MIN_CONFIDENCE,
    ): Flow<List<Long>>

    suspend fun clearAll()

    companion object {
        const val DEFAULT_MIN_CONFIDENCE = 0.5f
    }
}
