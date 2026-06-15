package com.javanapps.musicplayer.core.testing.repository

import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.core.model.Album
import com.javanapps.musicplayer.core.model.Artist
import com.javanapps.musicplayer.core.model.Song
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

class FakeSongsRepository : SongsRepository {
    private val songsFlow = MutableSharedFlow<List<Song>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun getSongs(): Flow<List<Song>> = songsFlow

    override fun getSong(mediaId: String): Flow<Song?> = songsFlow.map { songs -> songs.find { it.mediaId == mediaId } }

    override fun getAlbums(): Flow<List<Album>> =
        songsFlow.map { songs ->
            songs.groupBy { it.album }.map { (title, songs) ->
                Album(id = 0, title = title, artist = songs.first().artist, artworkUri = songs.first().artworkUri, songCount = songs.size)
            }
        }

    override fun getArtists(): Flow<List<Artist>> =
        songsFlow.map { songs ->
            songs.groupBy { it.artist }.map { (name, songs) ->
                Artist(id = 0, name = name, songCount = songs.size, albumCount = songs.distinctBy { it.album }.size)
            }
        }

    override fun getSongsByArtist(artistId: Long): Flow<List<Song>> = songsFlow

    override fun getSongsByAlbum(albumId: Long): Flow<List<Song>> = songsFlow

    fun setSongs(songs: List<Song>) {
        songsFlow.tryEmit(songs)
    }
}
