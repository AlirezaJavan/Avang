package com.javanapps.musicplayer.feature.home

import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.core.testing.controller.FakePlayerController
import com.javanapps.musicplayer.core.testing.repository.FakePlayHistoryRepository
import com.javanapps.musicplayer.core.testing.repository.FakeSongsRepository
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
import kotlin.test.assertTrue

class HomeViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val songsRepository = FakeSongsRepository()
    private val playHistoryRepository = FakePlayHistoryRepository()
    private val playerController = FakePlayerController()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        viewModel =
            HomeViewModel(
                songsRepository = songsRepository,
                playHistoryRepository = playHistoryRepository,
                playerController = playerController,
            )
    }

    @Test
    fun uiState_initiallyLoading() =
        runTest {
            assertEquals(HomeUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun uiState_isEmpty_whenNoDataAndNothingPlaying() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            val state = viewModel.uiState.value
            assertIs<HomeUiState.Success>(state)
            assertTrue(state.isEmpty)
        }

    @Test
    fun uiState_combinesFeedFromRepositories() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            val song = testSong(1L)
            songsRepository.setSongs(listOf(song))
            playHistoryRepository.setRecentlyPlayed(listOf(song))
            playHistoryRepository.setMostPlayed(listOf(song))

            val state = viewModel.uiState.value
            assertIs<HomeUiState.Success>(state)
            assertEquals(listOf(song), state.feed.recentlyPlayed)
            assertEquals(listOf(song), state.feed.mostPlayed)
            assertEquals(listOf(song), state.feed.recentlyAdded)
            assertTrue(!state.isEmpty)
        }

    @Test
    fun playFromShelf_callsPlayerController() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
            val songs = listOf(testSong(1L), testSong(2L))

            viewModel.playFromShelf(songs, 1)

            assertEquals(1, playerController.playCalls.size)
            assertEquals(songs to 1, playerController.playCalls[0])
        }

    private fun testSong(id: Long) =
        Song(
            id = id,
            mediaId = id.toString(),
            title = "Song $id",
            artist = "Artist",
            artistId = 0L,
            album = "Album",
            albumId = 0L,
            duration = 1000L,
            artworkUri = null,
            mediaUri = "",
            dateAdded = id,
        )
}
