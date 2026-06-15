package com.javanapps.musicplayer.core.data.repository

import android.net.Uri
import com.javanapps.musicplayer.core.analysis.SongAnalyzer
import com.javanapps.musicplayer.core.database.dao.SongTagDao
import com.javanapps.musicplayer.core.database.model.asEntity
import com.javanapps.musicplayer.core.domain.repository.AnalysisRepository
import com.javanapps.musicplayer.core.model.SmartPlaylist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineAnalysisRepository
    @Inject
    constructor(
        private val songAnalyzer: SongAnalyzer,
        private val songTagDao: SongTagDao,
    ) : AnalysisRepository {
        override suspend fun analyzeSong(
            songId: Long,
            mediaUri: String,
        ) {
            val tags = songAnalyzer.analyze(songId, Uri.parse(mediaUri))
            val analyzedAt = System.currentTimeMillis()
            songTagDao.replaceForSong(songId, tags.map { it.asEntity(analyzedAt) })
        }

        override suspend fun analyzedSongIds(): Set<Long> = songTagDao.getAnalyzedSongIds().toSet()

        override fun observeSmartPlaylists(minConfidence: Float): Flow<List<SmartPlaylist>> =
            songTagDao.getLabelCounts(minConfidence).map { rows ->
                rows.map { SmartPlaylist(label = it.label, songCount = it.songCount) }
            }

        override fun observeLabels(): Flow<List<String>> = songTagDao.getAllLabels()

        override fun observeSongIdsForLabel(
            label: String,
            minConfidence: Float,
        ): Flow<List<Long>> = songTagDao.getTagsByLabel(label, minConfidence).map { rows -> rows.map { it.songId } }

        override suspend fun clearAll() = songTagDao.clearAll()
    }
