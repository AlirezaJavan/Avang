package com.javanapps.musicplayer.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.javanapps.musicplayer.core.data.source.MediaStoreDataSource
import com.javanapps.musicplayer.core.database.dao.PlaylistDao
import com.javanapps.musicplayer.core.database.model.PlaylistEntityWithCount
import com.javanapps.musicplayer.core.model.Song
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class OfflinePlaylistRepositoryTest {
    private lateinit var playlistDao: PlaylistDao
    private lateinit var mediaStoreDataSource: MediaStoreDataSource
    private lateinit var repository: OfflinePlaylistRepository

    @Before
    fun setup() {
        playlistDao = mockk()
        mediaStoreDataSource = mockk()
        repository = OfflinePlaylistRepository(playlistDao, mediaStoreDataSource, UnconfinedTestDispatcher())
    }

    @Test
    fun getPlaylists_returnsMappedPlaylists() =
        runTest {
            val entities =
                listOf(
                    PlaylistEntityWithCount(id = 1L, name = "Favorites", songCount = 5),
                    PlaylistEntityWithCount(id = 2L, name = "Gym", songCount = 10),
                )
            every { playlistDao.getPlaylists() } returns flowOf(entities)

            val result = repository.getPlaylists().first()

            assertThat(result).hasSize(2)
            assertThat(result[0].id).isEqualTo(1L)
            assertThat(result[0].name).isEqualTo("Favorites")
            assertThat(result[0].songCount).isEqualTo(5)
        }

    @Test
    fun createPlaylist_callsDao() =
        runTest {
            val playlistName = "New Playlist"
            coEvery { playlistDao.insertPlaylist(any()) } returns 1L

            val id = repository.createPlaylist(playlistName)

            assertThat(id).isEqualTo(1L)
        }

    @Test
    fun getSongsInPlaylist_filtersMediaStoreSongs() =
        runTest {
            val playlistId = 1L
            val songIds = listOf(101L, 102L)
            val allSongs =
                listOf(
                    createSong(101L),
                    createSong(102L),
                    createSong(103L),
                )

            every { playlistDao.getSongIdsInPlaylist(playlistId) } returns flowOf(songIds)
            every { mediaStoreDataSource.getSongs() } returns flowOf(allSongs)

            val result = repository.getSongsInPlaylist(playlistId).first()

            assertThat(result).hasSize(2)
            assertThat(result.map { it.id }).containsExactly(101L, 102L)
        }

    private fun createSong(id: Long) =
        Song(
            id = id,
            mediaId = id.toString(),
            title = "Song $id",
            artist = "Artist",
            artistId = 0L,
            album = "Album",
            albumId = 0L,
            duration = 3000L,
            artworkUri = null,
            mediaUri = "",
            dateAdded = 0L,
        )
}
