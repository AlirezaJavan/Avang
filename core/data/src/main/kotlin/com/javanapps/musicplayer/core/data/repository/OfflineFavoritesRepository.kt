package com.javanapps.musicplayer.core.data.repository

import com.javanapps.musicplayer.core.common.dispatcher.Dispatcher
import com.javanapps.musicplayer.core.common.dispatcher.MusicPlayerDispatchers.Default
import com.javanapps.musicplayer.core.data.source.MediaStoreDataSource
import com.javanapps.musicplayer.core.database.dao.FavoriteDao
import com.javanapps.musicplayer.core.database.model.FavoriteEntity
import com.javanapps.musicplayer.core.domain.repository.FavoritesRepository
import com.javanapps.musicplayer.core.model.Song
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineFavoritesRepository
    @Inject
    constructor(
        private val favoriteDao: FavoriteDao,
        private val mediaStoreDataSource: MediaStoreDataSource,
        @Dispatcher(Default) private val defaultDispatcher: CoroutineDispatcher,
    ) : FavoritesRepository {
        override fun getFavoriteSongs(): Flow<List<Song>> =
            favoriteDao
                .getFavoriteSongIds()
                .flatMapLatest { ids ->
                    mediaStoreDataSource.getSongs().map { songs ->
                        val idSet = ids.toSet()
                        songs.filter { idSet.contains(it.id) }
                    }
                }.flowOn(defaultDispatcher)

        override fun getFavoriteSongIds(): Flow<Set<Long>> = favoriteDao.getFavoriteSongIds().map { it.toSet() }.flowOn(defaultDispatcher)

        override fun isFavorite(songId: Long): Flow<Boolean> = favoriteDao.isFavorite(songId).flowOn(defaultDispatcher)

        override suspend fun toggleFavorite(songId: Long) {
            val isFav = favoriteDao.isFavorite(songId).first()
            if (isFav) {
                favoriteDao.deleteFavorite(FavoriteEntity(songId))
            } else {
                favoriteDao.insertFavorite(FavoriteEntity(songId))
            }
        }
    }
