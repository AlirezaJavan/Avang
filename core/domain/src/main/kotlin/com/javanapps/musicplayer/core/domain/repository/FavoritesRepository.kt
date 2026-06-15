package com.javanapps.musicplayer.core.domain.repository

import com.javanapps.musicplayer.core.model.Song
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun getFavoriteSongs(): Flow<List<Song>>

    fun getFavoriteSongIds(): Flow<Set<Long>>

    fun isFavorite(songId: Long): Flow<Boolean>

    suspend fun toggleFavorite(songId: Long)
}
