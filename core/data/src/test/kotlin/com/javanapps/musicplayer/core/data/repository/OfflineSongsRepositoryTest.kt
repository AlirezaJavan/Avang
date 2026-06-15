package com.javanapps.musicplayer.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.javanapps.musicplayer.core.data.source.MediaStoreDataSource
import com.javanapps.musicplayer.core.model.Song
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class OfflineSongsRepositoryTest {
    private lateinit var mediaStoreDataSource: MediaStoreDataSource
    private lateinit var repository: OfflineSongsRepository

    @Before
    fun setup() {
        mediaStoreDataSource = mockk()
        repository = OfflineSongsRepository(mediaStoreDataSource)
    }

    @Test
    fun getSongs_returnsDataSourceSongs() =
        runTest {
            val songs = listOf(createSong(1L), createSong(2L))
            every { mediaStoreDataSource.getSongs() } returns flowOf(songs)

            val result = repository.getSongs().first()

            assertThat(result).isEqualTo(songs)
        }

    @Test
    fun getSong_returnsCorrectSong() =
        runTest {
            val songs = listOf(createSong(1L), createSong(2L))
            every { mediaStoreDataSource.getSongs() } returns flowOf(songs)

            val result = repository.getSong("1").first()

            assertThat(result?.id).isEqualTo(1L)
        }

    @Test
    fun getSongsByArtist_filtersCorrectly() =
        runTest {
            val songs =
                listOf(
                    createSong(1L, artistId = 10L),
                    createSong(2L, artistId = 20L),
                    createSong(3L, artistId = 10L),
                )
            every { mediaStoreDataSource.getSongs() } returns flowOf(songs)

            val result = repository.getSongsByArtist(10L).first()

            assertThat(result).hasSize(2)
            assertThat(result.map { it.id }).containsExactly(1L, 3L)
        }

    private fun createSong(
        id: Long,
        artistId: Long = 0L,
    ) = Song(
        id = id,
        mediaId = id.toString(),
        title = "Song $id",
        artist = "Artist",
        artistId = artistId,
        album = "Album",
        albumId = 0L,
        duration = 3000L,
        artworkUri = null,
        mediaUri = "",
        dateAdded = 0L,
    )
}
