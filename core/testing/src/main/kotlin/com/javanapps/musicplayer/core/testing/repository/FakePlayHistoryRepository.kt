package com.javanapps.musicplayer.core.testing.repository

import com.javanapps.musicplayer.core.domain.repository.PlayHistoryRepository
import com.javanapps.musicplayer.core.model.Song
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

class FakePlayHistoryRepository : PlayHistoryRepository {
    private val recentlyPlayedFlow = MutableSharedFlow<List<Song>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val mostPlayedFlow = MutableSharedFlow<List<Song>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        recentlyPlayedFlow.tryEmit(emptyList())
        mostPlayedFlow.tryEmit(emptyList())
    }

    override fun observeRecentlyPlayed(limit: Int): Flow<List<Song>> = recentlyPlayedFlow.map { it.take(limit) }

    override fun observeMostPlayed(limit: Int): Flow<List<Song>> = mostPlayedFlow.map { it.take(limit) }

    val recordedPlayIds = mutableListOf<Long>()

    override suspend fun recordPlay(songId: Long) {
        recordedPlayIds.add(songId)
    }

    fun setRecentlyPlayed(songs: List<Song>) {
        recentlyPlayedFlow.tryEmit(songs)
    }

    fun setMostPlayed(songs: List<Song>) {
        mostPlayedFlow.tryEmit(songs)
    }
}
