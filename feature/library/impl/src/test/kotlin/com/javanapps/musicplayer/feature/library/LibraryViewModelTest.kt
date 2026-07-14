package com.javanapps.musicplayer.feature.library

import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.core.testing.controller.FakePlayerController
import com.javanapps.musicplayer.core.testing.repository.FakeFavoritesRepository
import com.javanapps.musicplayer.core.testing.repository.FakeNotesRepository
import com.javanapps.musicplayer.core.testing.repository.FakePlaylistRepository
import com.javanapps.musicplayer.core.testing.repository.FakeSongsRepository
import com.javanapps.musicplayer.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: LibraryViewModel
    private lateinit var songsRepository: FakeSongsRepository
    private lateinit var favoritesRepository: FakeFavoritesRepository
    private lateinit var playlistRepository: FakePlaylistRepository
    private lateinit var playerController: FakePlayerController
    private lateinit var notesRepository: FakeNotesRepository

    @Before
    fun setup() {
        songsRepository = FakeSongsRepository()
        favoritesRepository = FakeFavoritesRepository()
        playlistRepository = FakePlaylistRepository()
        playerController = FakePlayerController()
        notesRepository = FakeNotesRepository()

        viewModel =
            LibraryViewModel(
                songsRepository = songsRepository,
                favoritesRepository = favoritesRepository,
                playlistRepository = playlistRepository,
                playerController = playerController,
                notesRepository = notesRepository,
                defaultDispatcher = UnconfinedTestDispatcher(),
            )
    }

    @Test
    fun uiState_initiallyLoading() =
        runTest {
            assertEquals(LibraryUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun uiState_loadsSongs() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            val song =
                Song(
                    id = 1L,
                    mediaId = "1",
                    title = "Song 1",
                    artist = "Artist 1",
                    artistId = 1L,
                    album = "Album 1",
                    albumId = 1L,
                    duration = 1000,
                    artworkUri = null,
                    mediaUri = "",
                    dateAdded = 0,
                )
            songsRepository.setSongs(listOf(song))

            val state = viewModel.uiState.value
            assertTrue(state is LibraryUiState.Success)
            assertEquals(1, state.songs.size)
            assertEquals("Song 1", state.songs[0].title)
        }

    @Test
    fun play_callsPlayerController() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            val song = Song(1L, "1", "Title", "Artist", 1L, "Album", 1L, 1000, null, "", 0)
            songsRepository.setSongs(listOf(song))

            viewModel.play("1")

            assertEquals(1, playerController.playCalls.size)
            assertEquals("1", playerController.playCalls[0].first[0].mediaId)
        }

    @Test
    fun onSortOrderChanged_sortsSongs() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            val song1 = Song(1L, "1", "Banana", "B", 1L, "Album", 1L, 1000, null, "", 0)
            val song2 = Song(2L, "2", "Apple", "A", 2L, "Album", 1L, 1000, null, "", 0)
            songsRepository.setSongs(listOf(song1, song2))

            viewModel.onSortOrderChanged(SortOrder.TITLE)

            val state = viewModel.uiState.value as LibraryUiState.Success
            assertEquals("Apple", state.songs[0].title)
            assertEquals("Banana", state.songs[1].title)
        }

    @Test
    fun toggleFavorite_callsRepository() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { favoritesRepository.isFavorite(1L).collect() }
            viewModel.toggleFavorite(1L)
            assertTrue(favoritesRepository.isFavorite(1L).first())
        }
}
