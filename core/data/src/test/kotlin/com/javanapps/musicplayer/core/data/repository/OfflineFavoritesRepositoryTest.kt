package com.javanapps.musicplayer.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.javanapps.musicplayer.core.data.source.MediaStoreDataSource
import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.core.testing.data.FakeFavoriteDao
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class OfflineFavoritesRepositoryTest {
    private lateinit var favoriteDao: FakeFavoriteDao
    private lateinit var mediaStoreDataSource: MediaStoreDataSource
    private lateinit var repository: OfflineFavoritesRepository

    @Before
    fun setup() {
        favoriteDao = FakeFavoriteDao()
        mediaStoreDataSource = mockk()
        repository = OfflineFavoritesRepository(favoriteDao, mediaStoreDataSource, UnconfinedTestDispatcher())
    }

    @Test
    fun getFavoriteSongs_filtersMediaStoreSongs() =
        runTest {
            val song1 = createSong(1L)
            val song2 = createSong(2L)
            val song3 = createSong(3L)

            every { mediaStoreDataSource.getSongs() } returns flowOf(listOf(song1, song2, song3))

            favoriteDao.insertFavorite(
                com.javanapps.musicplayer.core.database.model
                    .FavoriteEntity(1L),
            )
            favoriteDao.insertFavorite(
                com.javanapps.musicplayer.core.database.model
                    .FavoriteEntity(3L),
            )

            val result = repository.getFavoriteSongs().first()

            assertThat(result).hasSize(2)
            assertThat(result.map { it.id }).containsExactly(1L, 3L)
        }

    @Test
    fun toggleFavorite_updatesDao() =
        runTest {
            repository.toggleFavorite(1L)
            assertThat(repository.isFavorite(1L).first()).isTrue()

            repository.toggleFavorite(1L)
            assertThat(repository.isFavorite(1L).first()).isFalse()
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
