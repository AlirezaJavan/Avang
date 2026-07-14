package com.javanapps.musicplayer.feature.player

import com.javanapps.musicplayer.core.model.RepeatMode
import com.javanapps.musicplayer.core.model.Song
import com.javanapps.musicplayer.core.testing.controller.FakePlayerController
import com.javanapps.musicplayer.core.testing.media.FakeEqualizerManager
import com.javanapps.musicplayer.core.testing.repository.FakeFavoritesRepository
import com.javanapps.musicplayer.core.testing.repository.FakeNotesRepository
import com.javanapps.musicplayer.core.testing.repository.FakeUserDataRepository
import com.javanapps.musicplayer.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val playerController = FakePlayerController()
    private val favoritesRepository = FakeFavoritesRepository()
    private val notesRepository = FakeNotesRepository()
    private val userDataRepository = FakeUserDataRepository()
    private val equalizerManager = FakeEqualizerManager()
    private lateinit var viewModel: PlayerViewModel

    @Before
    fun setup() {
        viewModel =
            PlayerViewModel(
                playerController = playerController,
                favoritesRepository = favoritesRepository,
                notesRepository = notesRepository,
                userDataRepository = userDataRepository,
                equalizerManager = equalizerManager,
            )
    }

    @Test
    fun playerState_isObserved() =
        runTest {
            val song = Song(1L, "1", "Title", "Artist", 1L, "Album", 1L, 1000, null, "", 0)
            playerController.play(listOf(song), 0)

            assertEquals(song, viewModel.playerState.value.currentSong)
        }

    @Test
    fun toggleShuffle_callsController() =
        runTest {
            viewModel.toggleShuffle()
            assertTrue(playerController.playerState.value.shuffleMode)

            viewModel.toggleShuffle()
            assertFalse(playerController.playerState.value.shuffleMode)
        }

    @Test
    fun toggleRepeat_cyclesModes() =
        runTest {
            assertEquals(RepeatMode.NONE, playerController.playerState.value.repeatMode)

            viewModel.toggleRepeat()
            assertEquals(RepeatMode.ALL, playerController.playerState.value.repeatMode)

            viewModel.toggleRepeat()
            assertEquals(RepeatMode.ONE, playerController.playerState.value.repeatMode)

            viewModel.toggleRepeat()
            assertEquals(RepeatMode.NONE, playerController.playerState.value.repeatMode)
        }

    @Test
    fun toggleFavorite_callsRepository() =
        runTest {
            val song = Song(1L, "1", "Title", "Artist", 1L, "Album", 1L, 1000, null, "", 0)
            playerController.play(listOf(song), 0)

            viewModel.toggleFavorite()
            assertTrue(favoritesRepository.isFavorite(1L).first())
        }
}
