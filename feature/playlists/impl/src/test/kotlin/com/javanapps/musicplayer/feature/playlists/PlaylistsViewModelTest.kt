package com.javanapps.musicplayer.feature.playlists

import com.javanapps.musicplayer.core.model.Playlist
import com.javanapps.musicplayer.core.model.SongTag
import com.javanapps.musicplayer.core.model.TagSource
import com.javanapps.musicplayer.core.testing.repository.FakeAnalysisRepository
import com.javanapps.musicplayer.core.testing.repository.FakePlaylistRepository
import com.javanapps.musicplayer.core.testing.repository.FakeSongsRepository
import com.javanapps.musicplayer.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistsViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: PlaylistsViewModel
    private lateinit var playlistRepository: FakePlaylistRepository
    private lateinit var songsRepository: FakeSongsRepository
    private lateinit var analysisRepository: FakeAnalysisRepository

    @Before
    fun setup() {
        playlistRepository = FakePlaylistRepository()
        songsRepository = FakeSongsRepository()
        analysisRepository = FakeAnalysisRepository()
        viewModel =
            PlaylistsViewModel(
                playlistRepository = playlistRepository,
                songsRepository = songsRepository,
                analysisRepository = analysisRepository,
            )
    }

    @Test
    fun uiState_initialStateIsLoading() =
        runTest {
            assertEquals(PlaylistsUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun uiState_success_whenPlaylistsLoaded() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            val playlists =
                listOf(
                    Playlist(1L, "Favorites", 0),
                    Playlist(2L, "Gym", 5),
                )
            playlists.forEach { playlistRepository.createPlaylist(it.name) }

            val state = viewModel.uiState.value
            assertTrue(state is PlaylistsUiState.Success)
            assertEquals(2, state.playlists.size)
            assertEquals("Favorites", state.playlists[0].name)
        }

    @Test
    fun uiState_surfacesSmartPlaylists() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            analysisRepository.setTags(
                listOf(
                    SongTag(1L, "Jazz", 0.9f, TagSource.AUDIO_MODEL),
                    SongTag(2L, "Jazz", 0.9f, TagSource.AUDIO_MODEL),
                ),
            )

            val state = viewModel.uiState.value as PlaylistsUiState.Success
            assertEquals(1, state.smartPlaylists.size)
            assertEquals("Jazz", state.smartPlaylists[0].label)
            assertEquals(2, state.smartPlaylists[0].songCount)
        }

    @Test
    fun uiState_filtersOutNonCuratedSmartPlaylists() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            // Only the curated genre labels should surface; everything else is hidden.
            analysisRepository.setTags(
                listOf(
                    SongTag(1L, "Jazz", 0.9f, TagSource.AUDIO_MODEL),
                    SongTag(2L, "Reggae", 0.9f, TagSource.AUDIO_MODEL),
                    SongTag(3L, "Sad", 0.9f, TagSource.AUDIO_MODEL),
                ),
            )

            val state = viewModel.uiState.value as PlaylistsUiState.Success
            assertEquals(2, state.smartPlaylists.size)
            assertTrue(state.smartPlaylists.none { it.label == "Sad" })
        }

    @Test
    fun createPlaylist_callsRepository() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            viewModel.createPlaylist("New Playlist")

            val state = viewModel.uiState.value as PlaylistsUiState.Success
            assertEquals(1, state.playlists.size)
            assertEquals("New Playlist", state.playlists[0].name)
        }

    @Test
    fun deletePlaylist_callsRepository() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            playlistRepository.createPlaylist("To Delete")
            val id = (viewModel.uiState.value as PlaylistsUiState.Success).playlists[0].id

            viewModel.deletePlaylist(id)

            val state = viewModel.uiState.value as PlaylistsUiState.Success
            assertTrue(state.playlists.isEmpty())
        }

    @Test
    fun renamePlaylist_callsRepository() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            playlistRepository.createPlaylist("Old Name")
            val id = (viewModel.uiState.value as PlaylistsUiState.Success).playlists[0].id

            viewModel.renamePlaylist(id, "New Name")

            val state = viewModel.uiState.value as PlaylistsUiState.Success
            assertEquals("New Name", state.playlists[0].name)
        }
}
