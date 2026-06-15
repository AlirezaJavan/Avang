package com.javanapps.musicplayer.core.testing.repository

import com.javanapps.musicplayer.core.domain.repository.FavoritesRepository
import com.javanapps.musicplayer.core.model.Song
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

class FakeFavoritesRepository : FavoritesRepository {
    private val favoriteSongIds = mutableSetOf<Long>()
    private val songsFlow = MutableSharedFlow<List<Song>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        songsFlow.tryEmit(emptyList())
    }

    override fun getFavoriteSongs(): Flow<List<Song>> = songsFlow.map { songs -> songs.filter { it.id in favoriteSongIds } }

    override fun getFavoriteSongIds(): Flow<Set<Long>> = songsFlow.map { favoriteSongIds.toSet() }

    override fun isFavorite(songId: Long): Flow<Boolean> = songsFlow.map { favoriteSongIds.contains(songId) }

    override suspend fun toggleFavorite(songId: Long) {
        if (songId in favoriteSongIds) favoriteSongIds.remove(songId) else favoriteSongIds.add(songId)
        songsFlow.tryEmit(songsFlow.replayCache.firstOrNull() ?: emptyList())
    }

    fun setSongs(songs: List<Song>) {
        songsFlow.tryEmit(songs)
    }
}
