package com.javanapps.musicplayer.core.testing.repository

import com.javanapps.musicplayer.core.domain.repository.AnalysisRepository
import com.javanapps.musicplayer.core.model.SmartPlaylist
import com.javanapps.musicplayer.core.model.SongTag
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

class FakeAnalysisRepository : AnalysisRepository {
    private val tags = mutableListOf<SongTag>()
    private val flow = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        flow.tryEmit(Unit)
    }

    fun setTags(newTags: List<SongTag>) {
        tags.clear()
        tags.addAll(newTags)
        flow.tryEmit(Unit)
    }

    override suspend fun analyzeSong(
        songId: Long,
        mediaUri: String,
    ) = Unit

    override suspend fun analyzedSongIds(): Set<Long> = tags.map { it.songId }.toSet()

    override fun observeSmartPlaylists(minConfidence: Float): Flow<List<SmartPlaylist>> =
        flow.map {
            tags
                .filter { it.confidence >= minConfidence }
                .groupBy { it.label }
                .map { (label, rows) -> SmartPlaylist(label, rows.map { it.songId }.distinct().size) }
                .sortedBy { it.label }
        }

    override fun observeLabels(): Flow<List<String>> = flow.map { tags.map { it.label }.distinct().sorted() }

    override fun observeSongIdsForLabel(
        label: String,
        minConfidence: Float,
    ): Flow<List<Long>> =
        flow.map {
            tags.filter { it.label == label && it.confidence >= minConfidence }.map { it.songId }
        }

    override suspend fun clearAll() {
        tags.clear()
        flow.tryEmit(Unit)
    }
}
