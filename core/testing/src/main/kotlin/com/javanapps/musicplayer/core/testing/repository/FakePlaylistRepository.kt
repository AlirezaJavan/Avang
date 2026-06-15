package com.javanapps.musicplayer.core.testing.repository

import com.javanapps.musicplayer.core.domain.repository.PlaylistRepository
import com.javanapps.musicplayer.core.model.Playlist
import com.javanapps.musicplayer.core.model.Song
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

class FakePlaylistRepository : PlaylistRepository {
    private var nextId = 1L
    private val playlists = mutableMapOf<Long, Playlist>()
    private val playlistSongs = mutableMapOf<Long, MutableList<Long>>()
    private val flow = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val allSongs = mutableListOf<Song>()

    init {
        flow.tryEmit(Unit)
    }

    override fun getPlaylists(): Flow<List<Playlist>> =
        flow.map {
            playlists.values.map { p ->
                p.copy(songCount = playlistSongs[p.id]?.size ?: 0)
            }
        }

    override fun getPlaylist(id: Long): Flow<Playlist?> =
        flow.map {
            playlists[id]?.copy(songCount = playlistSongs[id]?.size ?: 0)
        }

    override fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> =
        flow.map { ids ->
            val songIds = playlistSongs[playlistId] ?: emptyList()
            allSongs.filter { it.id in songIds }
        }

    override suspend fun createPlaylist(name: String): Long {
        val id = nextId++
        playlists[id] = Playlist(id = id, name = name, songCount = 0)
        playlistSongs[id] = mutableListOf()
        flow.tryEmit(Unit)
        return id
    }

    override suspend fun renamePlaylist(
        id: Long,
        name: String,
    ) {
        playlists[id] = playlists[id]?.copy(name = name) ?: return
        flow.tryEmit(Unit)
    }

    override suspend fun deletePlaylist(id: Long) {
        playlists.remove(id)
        playlistSongs.remove(id)
        flow.tryEmit(Unit)
    }

    override suspend fun addSongToPlaylist(
        playlistId: Long,
        songId: Long,
    ) {
        playlistSongs.getOrPut(playlistId) { mutableListOf() }.add(songId)
        flow.tryEmit(Unit)
    }

    override suspend fun removeSongFromPlaylist(
        playlistId: Long,
        songId: Long,
    ) {
        playlistSongs[playlistId]?.remove(songId)
        flow.tryEmit(Unit)
    }

    override suspend fun clearPlaylist(playlistId: Long) {
        playlistSongs[playlistId]?.clear()
        flow.tryEmit(Unit)
    }

    fun setSongs(songs: List<Song>) {
        allSongs.clear()
        allSongs.addAll(songs)
    }

    fun addPlaylistWithSongs(
        playlist: Playlist,
        songs: List<Song>,
    ) {
        playlists[playlist.id] = playlist
        playlistSongs[playlist.id] = songs.map { it.id }.toMutableList()
        allSongs.addAll(songs)
        flow.tryEmit(Unit)
    }
}
