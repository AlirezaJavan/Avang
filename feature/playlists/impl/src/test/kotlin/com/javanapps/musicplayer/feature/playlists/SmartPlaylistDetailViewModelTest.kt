package com.javanapps.musicplayer.feature.playlists

import androidx.lifecycle.SavedStateHandle
import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.core.model.SongTag
import com.javanapps.musicplayer.core.model.TagSource
import com.javanapps.musicplayer.core.testing.controller.FakePlayerController
import com.javanapps.musicplayer.core.testing.repository.FakeAnalysisRepository
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SmartPlaylistDetailViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var analysisRepository: FakeAnalysisRepository
    private lateinit var songsRepository: FakeSongsRepository
    private lateinit var playerController: FakePlayerController
    private lateinit var viewModel: SmartPlaylistDetailViewModel

    @Before
    fun setup() {
        analysisRepository = FakeAnalysisRepository()
        songsRepository = FakeSongsRepository()
        playerController = FakePlayerController()
        viewModel =
            SmartPlaylistDetailViewModel(
                savedStateHandle = SavedStateHandle(mapOf("label" to LABEL)),
                analysisRepository = analysisRepository,
                songsRepository = songsRepository,
                playerController = playerController,
            )
    }

    @Test
    fun uiState_showsOnlySongsTaggedWithLabel() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
            songsRepository.setSongs(listOf(song(1L), song(2L), song(3L)))
            analysisRepository.setTags(
                listOf(
                    SongTag(1L, LABEL, 0.9f, TagSource.AUDIO_RULES),
                    SongTag(3L, LABEL, 0.9f, TagSource.AUDIO_RULES),
                ),
            )

            val state = viewModel.uiState.value as SmartPlaylistDetailUiState.Success
            assertEquals(LABEL, state.label)
            assertEquals(listOf(1L, 3L), state.songs.map { it.id })
        }

    @Test
    fun play_startsPlaybackFromSelectedSong() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
            songsRepository.setSongs(listOf(song(1L), song(3L)))
            analysisRepository.setTags(
                listOf(
                    SongTag(1L, LABEL, 0.9f, TagSource.AUDIO_RULES),
                    SongTag(3L, LABEL, 0.9f, TagSource.AUDIO_RULES),
                ),
            )

            viewModel.play(mediaId = "3")

            assertTrue(playerController.playCalls.isNotEmpty())
            assertEquals(1, playerController.playCalls.first().second)
        }

    private fun song(id: Long) =
        Song(
            id = id,
            mediaId = id.toString(),
            title = "Song $id",
            artist = "Artist",
            artistId = 0L,
            album = "Album",
            albumId = 0L,
            duration = 0L,
            artworkUri = null,
            mediaUri = "uri/$id",
            dateAdded = 0L,
        )

    private companion object {
        const val LABEL = "Calm"
    }
}
