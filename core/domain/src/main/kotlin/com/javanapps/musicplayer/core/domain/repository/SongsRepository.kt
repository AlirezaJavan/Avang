package com.javanapps.musicplayer.core.domain.repository

import com.javanapps.musicplayer.core.model.Album
import com.javanapps.musicplayer.core.model.Artist
import com.javanapps.musicplayer.core.model.Song
import kotlinx.coroutines.flow.Flow

interface SongsRepository {
    fun getSongs(): Flow<List<Song>>

    fun getSong(mediaId: String): Flow<Song?>

    fun getAlbums(): Flow<List<Album>>

    fun getArtists(): Flow<List<Artist>>

    fun getSongsByArtist(artistId: Long): Flow<List<Song>>

    fun getSongsByAlbum(albumId: Long): Flow<List<Song>>
}
