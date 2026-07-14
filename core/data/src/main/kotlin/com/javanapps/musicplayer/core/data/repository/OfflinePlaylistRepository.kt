package com.javanapps.musicplayer.core.data.repository

import com.javanapps.musicplayer.core.common.dispatcher.Dispatcher
import com.javanapps.musicplayer.core.common.dispatcher.MusicPlayerDispatchers.Default
import com.javanapps.musicplayer.core.data.source.MediaStoreDataSource
import com.javanapps.musicplayer.core.database.dao.PlaylistDao
import com.javanapps.musicplayer.core.database.model.PlaylistEntity
import com.javanapps.musicplayer.core.database.model.PlaylistSongCrossRef
import com.javanapps.musicplayer.core.database.model.asExternalModel
import com.javanapps.musicplayer.core.domain.repository.PlaylistRepository
import com.javanapps.musicplayer.core.model.Playlist
import com.javanapps.musicplayer.core.model.Song
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflinePlaylistRepository
    @Inject
    constructor(
        private val playlistDao: PlaylistDao,
        private val mediaStoreDataSource: MediaStoreDataSource,
        @Dispatcher(Default) private val defaultDispatcher: CoroutineDispatcher,
    ) : PlaylistRepository {
        override fun getPlaylists(): Flow<List<Playlist>> =
            playlistDao
                .getPlaylists()
                .map { entities ->
                    entities.map { it.asExternalModel() }
                }.flowOn(defaultDispatcher)

        override fun getPlaylist(id: Long): Flow<Playlist?> =
            playlistDao.getPlaylist(id).map { it?.asExternalModel() }.flowOn(defaultDispatcher)

        override fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> =
            playlistDao
                .getSongIdsInPlaylist(playlistId)
                .flatMapLatest { ids ->
                    mediaStoreDataSource.getSongs().map { songs ->
                        val idSet = ids.toSet()
                        songs.filter { idSet.contains(it.id) }
                    }
                }.flowOn(defaultDispatcher)

        override suspend fun createPlaylist(name: String): Long = playlistDao.insertPlaylist(PlaylistEntity(name = name))

        override suspend fun renamePlaylist(
            id: Long,
            name: String,
        ) = playlistDao.updatePlaylist(PlaylistEntity(id = id, name = name))

        override suspend fun deletePlaylist(id: Long) = playlistDao.deletePlaylist(PlaylistEntity(id = id, name = ""))

        override suspend fun addSongToPlaylist(
            playlistId: Long,
            songId: Long,
        ) = playlistDao.insertPlaylistSongCrossRef(
            PlaylistSongCrossRef(playlistId = playlistId, songId = songId),
        )

        override suspend fun removeSongFromPlaylist(
            playlistId: Long,
            songId: Long,
        ) = playlistDao.deletePlaylistSongCrossRef(
            PlaylistSongCrossRef(playlistId = playlistId, songId = songId),
        )

        override suspend fun clearPlaylist(playlistId: Long) = playlistDao.clearPlaylist(playlistId)
    }
