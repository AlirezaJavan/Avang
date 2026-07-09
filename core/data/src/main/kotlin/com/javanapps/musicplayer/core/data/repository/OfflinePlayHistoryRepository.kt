package com.javanapps.musicplayer.core.data.repository

import com.javanapps.musicplayer.core.database.dao.PlayCountDao
import com.javanapps.musicplayer.core.domain.repository.PlayHistoryRepository
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class OfflinePlayHistoryRepository
    @Inject
    constructor(
        private val playCountDao: PlayCountDao,
        private val songsRepository: SongsRepository,
    ) : PlayHistoryRepository {
        override fun observeRecentlyPlayed(limit: Int): Flow<List<Song>> =
            combine(playCountDao.getRecentlyPlayed(), songsRepository.getSongs()) { playCounts, songs ->
                mapToSongs(playCounts.map { it.songId }, songs, limit)
            }

        override fun observeMostPlayed(limit: Int): Flow<List<Song>> =
            combine(playCountDao.getMostPlayed(), songsRepository.getSongs()) { playCounts, songs ->
                mapToSongs(playCounts.map { it.songId }, songs, limit)
            }

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
