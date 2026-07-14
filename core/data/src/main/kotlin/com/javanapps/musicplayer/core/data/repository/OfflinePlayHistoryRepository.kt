package com.javanapps.musicplayer.core.data.repository

import com.javanapps.musicplayer.core.common.dispatcher.Dispatcher
import com.javanapps.musicplayer.core.common.dispatcher.MusicPlayerDispatchers.Default
import com.javanapps.musicplayer.core.database.dao.PlayCountDao
import com.javanapps.musicplayer.core.domain.repository.PlayHistoryRepository
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.core.model.Song
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class OfflinePlayHistoryRepository
    @Inject
    constructor(
        private val playCountDao: PlayCountDao,
        private val songsRepository: SongsRepository,
        @Dispatcher(Default) private val defaultDispatcher: CoroutineDispatcher,
    ) : PlayHistoryRepository {
        override fun observeRecentlyPlayed(limit: Int): Flow<List<Song>> =
            combine(playCountDao.getRecentlyPlayed(), songsRepository.getSongs()) { playCounts, songs ->
                mapToSongs(playCounts.map { it.songId }, songs, limit)
            }.flowOn(defaultDispatcher)

        override fun observeMostPlayed(limit: Int): Flow<List<Song>> =
            combine(playCountDao.getMostPlayed(), songsRepository.getSongs()) { playCounts, songs ->
                mapToSongs(playCounts.map { it.songId }, songs, limit)
            }.flowOn(defaultDispatcher)

        override suspend fun recordPlay(songId: Long) {
            playCountDao.incrementPlayCount(songId)
        }

        private fun mapToSongs(
            orderedSongIds: List<Long>,
            songs: List<Song>,
            limit: Int,
        ): List<Song> {
            val songsById = songs.associateBy { it.id }
            return orderedSongIds
                .mapNotNull { songsById[it] }
                .take(limit)
        }
    }
