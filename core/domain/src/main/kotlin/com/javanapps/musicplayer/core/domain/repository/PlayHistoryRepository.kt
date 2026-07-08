package com.javanapps.musicplayer.core.domain.repository

import com.javanapps.musicplayer.core.model.Song
import kotlinx.coroutines.flow.Flow

interface PlayHistoryRepository {
    fun observeRecentlyPlayed(limit: Int = 25): Flow<List<Song>>

    fun observeMostPlayed(limit: Int = 25): Flow<List<Song>>

    suspend fun recordPlay(songId: Long)
}
