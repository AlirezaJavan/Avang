package com.javanapps.musicplayer.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.javanapps.musicplayer.core.data.source.MediaStoreDataSource
import com.javanapps.musicplayer.core.data.worker.AnalysisScheduler
import com.javanapps.musicplayer.core.model.Song
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class OfflineSongsRepositoryTest {
    private lateinit var mediaStoreDataSource: MediaStoreDataSource
    private lateinit var analysisScheduler: AnalysisScheduler
    private lateinit var repository: OfflineSongsRepository

    @Before
    fun setup() {
        mediaStoreDataSource = mockk()
        analysisScheduler = mockk()
        val testDispatcher = UnconfinedTestDispatcher()
        repository =
            OfflineSongsRepository(
                mediaStoreDataSource,
                analysisScheduler,
                testDispatcher,
                CoroutineScope(testDispatcher),
            )
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

    @Test
    fun observeRecentlyAdded_sortsNewestFirstAndRespectsLimit() =
        runTest {
            val songs =
                listOf(
                    createSong(1L, dateAdded = 100L),
                    createSong(2L, dateAdded = 300L),
                    createSong(3L, dateAdded = 200L),
                )
            every { mediaStoreDataSource.getSongs() } returns flowOf(songs)

            val result = repository.observeRecentlyAdded(limit = 2).first()

            assertThat(result.map { it.id }).containsExactly(2L, 3L).inOrder()
        }

    @Test
    fun refresh_triggersScanAndEnqueueAnalysis() =
        runTest {
            every { mediaStoreDataSource.triggerMediaScan() } returns Unit
            every { analysisScheduler.enqueue() } returns Unit

            repository.refresh()

            verify { mediaStoreDataSource.triggerMediaScan() }
            verify { analysisScheduler.enqueue() }
        }

    private fun createSong(
        id: Long,
        artistId: Long = 0L,
        dateAdded: Long = 0L,
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
        dateAdded = dateAdded,
    )
}
