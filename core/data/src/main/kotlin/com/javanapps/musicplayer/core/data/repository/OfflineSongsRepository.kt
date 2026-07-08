package com.javanapps.musicplayer.core.data.repository

import com.javanapps.musicplayer.core.data.source.MediaStoreDataSource
import com.javanapps.musicplayer.core.data.worker.AnalysisScheduler
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.core.model.Album
import com.javanapps.musicplayer.core.model.Artist
import com.javanapps.musicplayer.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineSongsRepository
    @Inject
    constructor(
        private val mediaStoreDataSource: MediaStoreDataSource,
        private val analysisScheduler: AnalysisScheduler,
    ) : SongsRepository {
        override fun getSongs(): Flow<List<Song>> = mediaStoreDataSource.getSongs()

        override fun getSong(mediaId: String): Flow<Song?> =
            mediaStoreDataSource.getSongs().map { songs ->
                songs.find { it.mediaId == mediaId }
            }

        override fun getAlbums(): Flow<List<Album>> = mediaStoreDataSource.getAlbums()

        override fun getArtists(): Flow<List<Artist>> = mediaStoreDataSource.getArtists()

        override fun getSongsByArtist(artistId: Long): Flow<List<Song>> =
            mediaStoreDataSource.getSongs().map { songs ->
                songs.filter { it.artistId == artistId }
            }

        override fun getSongsByAlbum(albumId: Long): Flow<List<Song>> =
            mediaStoreDataSource.getSongs().map { songs ->
                songs.filter { it.albumId == albumId }
            }

        override fun observeRecentlyAdded(limit: Int): Flow<List<Song>> =
            mediaStoreDataSource.getSongs().map { songs ->
                songs.sortedByDescending { it.dateAdded }.take(limit)
            }

        override suspend fun refresh() {
            mediaStoreDataSource.triggerMediaScan()
            analysisScheduler.enqueue()
        }
    }
