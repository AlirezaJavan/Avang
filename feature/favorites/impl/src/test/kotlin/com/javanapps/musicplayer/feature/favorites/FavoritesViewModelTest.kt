package com.javanapps.musicplayer.feature.favorites

import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.core.testing.controller.FakePlayerController
import com.javanapps.musicplayer.core.testing.repository.FakeFavoritesRepository
import com.javanapps.musicplayer.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FavoritesViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val favoritesRepository = FakeFavoritesRepository()
    private val playerController = FakePlayerController()
    private lateinit var viewModel: FavoritesViewModel

    @Before
    fun setup() {
        viewModel =
            FavoritesViewModel(
                favoritesRepository = favoritesRepository,
                playerController = playerController,
            )
    }

    @Test
    fun uiState_initiallyLoading() =
        runTest {
            assertEquals(FavoritesUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun uiState_loadsFavorites() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            val song = Song(1L, "1", "Song 1", "Artist 1", 1L, "Album 1", 1L, 1000, null, "", 0)
            favoritesRepository.setSongs(listOf(song))
            favoritesRepository.toggleFavorite(1L)

            val state = viewModel.uiState.value
            assertIs<FavoritesUiState.Success>(state)
            assertEquals(1, state.songs.size)
            assertEquals("Song 1", state.songs[0].title)
        }

    @Test
    fun play_callsPlayerController() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            val song = Song(1L, "1", "Song 1", "Artist 1", 1L, "Album 1", 1L, 1000, null, "", 0)
            favoritesRepository.setSongs(listOf(song))
            favoritesRepository.toggleFavorite(1L)

            viewModel.play("1")

            assertEquals(1, playerController.playCalls.size)
            assertEquals("1", playerController.playCalls[0].first[0].mediaId)
        }

    @Test
    fun toggleFavorite_callsRepository() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
            val song = Song(1L, "1", "Song 1", "Artist 1", 1L, "Album 1", 1L, 1000, null, "", 0)
            favoritesRepository.setSongs(listOf(song))

            viewModel.toggleFavorite(1L)

            val state = viewModel.uiState.value
            assertIs<FavoritesUiState.Success>(state)
            assertEquals(1, state.songs.size)
        }
}
