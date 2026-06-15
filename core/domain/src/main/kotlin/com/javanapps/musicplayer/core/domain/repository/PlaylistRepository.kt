package com.javanapps.musicplayer.core.domain.repository

import com.javanapps.musicplayer.core.model.Playlist
import com.javanapps.musicplayer.core.model.Song
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylists(): Flow<List<Playlist>>

    fun getPlaylist(id: Long): Flow<Playlist?>

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>>

    suspend fun createPlaylist(name: String): Long

    suspend fun renamePlaylist(
        id: Long,
        name: String,
    )

    suspend fun deletePlaylist(id: Long)

    suspend fun addSongToPlaylist(
        playlistId: Long,
        songId: Long,
    )

    suspend fun removeSongFromPlaylist(
        playlistId: Long,
        songId: Long,
    )

    suspend fun clearPlaylist(playlistId: Long)
}
