package com.javanapps.musicplayer.core.testing.data

import com.javanapps.musicplayer.core.database.dao.FavoriteDao
import com.javanapps.musicplayer.core.database.model.FavoriteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFavoriteDao : FavoriteDao {
    private val favorites = MutableStateFlow<Set<Long>>(emptySet())

    override fun getFavoriteSongIds(): Flow<List<Long>> = favorites.map { it.toList() }

    override suspend fun insertFavorite(favorite: FavoriteEntity) {
        favorites.value = favorites.value + favorite.songId
    }

    override suspend fun deleteFavorite(favorite: FavoriteEntity) {
        favorites.value = favorites.value - favorite.songId
    }

    override fun isFavorite(songId: Long): Flow<Boolean> = favorites.map { it.contains(songId) }
}
