package com.javanapps.musicplayer.feature.playlists

import androidx.lifecycle.SavedStateHandle
import com.javanapps.musicplayer.core.model.Playlist
import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.core.testing.controller.FakePlayerController
import com.javanapps.musicplayer.core.testing.repository.FakePlaylistRepository
import com.javanapps.musicplayer.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PlaylistDetailViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: PlaylistDetailViewModel
    private lateinit var playlistRepository: FakePlaylistRepository
    private lateinit var playerController: FakePlayerController

    @Before
    fun setup() {
        playlistRepository = FakePlaylistRepository()
        playerController = FakePlayerController()

        // Mock SavedStateHandle with the expected route property name
        val savedStateHandle = SavedStateHandle(mapOf("playlistId" to 1L))

        viewModel =
            PlaylistDetailViewModel(
                savedStateHandle = savedStateHandle,
                playlistRepository = playlistRepository,
                playerController = playerController,
            )
    }

    @Test
    fun uiState_loadsPlaylistAndSongs() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            val playlist = Playlist(1L, "My Playlist", 1)
            val song = Song(1L, "1", "Title", "Artist", 1L, "Album", 1L, 1000, null, "", 0)
            playlistRepository.addPlaylistWithSongs(playlist, listOf(song))

            val state = viewModel.uiState.value
            assertTrue(state is PlaylistDetailUiState.Success)
            assertEquals("My Playlist", state.playlist.name)
            assertEquals(1, state.songs.size)
        }

    @Test
    fun removeSong_callsRepository() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            val playlist = Playlist(1L, "My Playlist", 1)
            val song = Song(1L, "1", "Title", "Artist", 1L, "Album", 1L, 1000, null, "", 0)
            playlistRepository.addPlaylistWithSongs(playlist, listOf(song))

            viewModel.removeSong(1L)

            val state = viewModel.uiState.value as PlaylistDetailUiState.Success
            assertEquals(0, state.songs.size)
        }
}
