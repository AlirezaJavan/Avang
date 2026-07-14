package com.javanapps.musicplayer.core.data.repository

import com.javanapps.musicplayer.core.common.dispatcher.Dispatcher
import com.javanapps.musicplayer.core.common.dispatcher.MusicPlayerDispatchers.Default
import com.javanapps.musicplayer.core.common.dispatcher.di.ApplicationScope
import com.javanapps.musicplayer.core.data.source.MediaStoreDataSource
import com.javanapps.musicplayer.core.data.worker.AnalysisScheduler
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.core.model.Album
import com.javanapps.musicplayer.core.model.Artist
import com.javanapps.musicplayer.core.model.Song
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineSongsRepository
    @Inject
    constructor(
        private val mediaStoreDataSource: MediaStoreDataSource,
        private val analysisScheduler: AnalysisScheduler,
        @Dispatcher(Default) private val defaultDispatcher: CoroutineDispatcher,
        @ApplicationScope private val applicationScope: CoroutineScope,
    ) : SongsRepository {
        // Shared for the lifetime of the app: the MediaStore query + ContentObserver run once
        // and are reused by every screen, instead of every Library visit re-querying MediaStore.
        // `by lazy` defers the first call to mediaStoreDataSource until actually needed.
        private val songsFlow: Flow<List<Song>> by lazy {
            mediaStoreDataSource.getSongs().shareIn(applicationScope, SharingStarted.Lazily, replay = 1)
        }
        private val albumsFlow: Flow<List<Album>> by lazy {
            mediaStoreDataSource.getAlbums().shareIn(applicationScope, SharingStarted.Lazily, replay = 1)
        }
        private val artistsFlow: Flow<List<Artist>> by lazy {
            mediaStoreDataSource.getArtists().shareIn(applicationScope, SharingStarted.Lazily, replay = 1)
        }

        override fun getSongs(): Flow<List<Song>> = songsFlow

        override fun getSong(mediaId: String): Flow<Song?> =
            songsFlow
                .map { songs ->
                    songs.find { it.mediaId == mediaId }
                }.flowOn(defaultDispatcher)

        override fun getAlbums(): Flow<List<Album>> = albumsFlow

        override fun getArtists(): Flow<List<Artist>> = artistsFlow

        override fun getSongsByArtist(artistId: Long): Flow<List<Song>> =
            songsFlow
                .map { songs ->
                    songs.filter { it.artistId == artistId }
                }.flowOn(defaultDispatcher)

        override fun getSongsByAlbum(albumId: Long): Flow<List<Song>> =
            songsFlow
                .map { songs ->
                    songs.filter { it.albumId == albumId }
                }.flowOn(defaultDispatcher)

        override fun observeRecentlyAdded(limit: Int): Flow<List<Song>> =
            songsFlow
                .map { songs ->
                    songs.sortedByDescending { it.dateAdded }.take(limit)
                }.flowOn(defaultDispatcher)

        override suspend fun refresh() {
            withContext(defaultDispatcher) {
                mediaStoreDataSource.triggerMediaScan()
                analysisScheduler.enqueue()
            }
        }
    }
