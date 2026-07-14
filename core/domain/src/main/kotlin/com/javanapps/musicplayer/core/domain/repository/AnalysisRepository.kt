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

    /** Deletes every auto-generated playlist's tags and re-analyzes the whole library from scratch. */
    suspend fun rescanAll()

    companion object {
        // TfLiteTagClassifier persists at most one tag per song: whichever genre subclass it
        // scored highest, once "Music" is confirmed as the clip's top class overall. There's no
        // per-tag confidence floor at classification time, so none is applied for display either
        // — a low absolute score there is still the model's best (and only) guess for that song.
        const val DEFAULT_MIN_CONFIDENCE = 0f
    }
}
