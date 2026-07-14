package com.javanapps.musicplayer.ui

import com.javanapps.musicplayer.core.testing.controller.FakePlayerController
import com.javanapps.musicplayer.core.testing.repository.FakeSongsRepository
import com.javanapps.musicplayer.core.testing.repository.FakeUserDataRepository
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
class MainViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: MainViewModel
    private val userDataRepository = FakeUserDataRepository()
    private val playerController = FakePlayerController()
    private val songsRepository = FakeSongsRepository()

    @Before
    fun setup() {
        viewModel =
            MainViewModel(
                userDataRepository = userDataRepository,
                playerController = playerController,
                songsRepository = songsRepository,
            )
    }

    @Test
    fun uiState_initialStateIsLoading() =
        runTest {
            assertEquals(MainUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun uiState_success_whenUserDataLoaded() =
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

            userDataRepository.setDynamicColor(true)

            val state = viewModel.uiState.value
            assertTrue(state is MainUiState.Success)
            assertTrue(state.shouldUseDynamicColor)
        }

    @Test
    fun playerState_isObserved() =
        runTest {
            assertEquals(playerController.playerState.value, viewModel.playerState.value)
        }
}
