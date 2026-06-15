package com.javanapps.musicplayer.feature.equalizer

import com.javanapps.musicplayer.core.testing.media.FakeEqualizerManager
import com.javanapps.musicplayer.core.testing.repository.FakeUserDataRepository
import com.javanapps.musicplayer.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EqualizerViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val equalizerManager = FakeEqualizerManager()
    private val userDataRepository = FakeUserDataRepository()
    private lateinit var viewModel: EqualizerViewModel

    @Before
    fun setup() {
        viewModel =
            EqualizerViewModel(
                equalizerManager = equalizerManager,
                userDataRepository = userDataRepository,
            )
    }

    @Test
    fun setEnabled_updatesManagerAndRepository() =
        runTest {
            viewModel.setEnabled(true)
            assertTrue(equalizerManager.state.value.enabled)
            assertTrue(userDataRepository.userData.first().equalizerEnabled)
        }

    @Test
    fun setBandLevel_updatesManagerAndRepository() =
        runTest {
            viewModel.setBandLevel(0, 500)
            assertEquals(500, equalizerManager.state.value.bandLevels[0])
            assertEquals(500.toShort(), userDataRepository.userData.first().equalizerBandLevels[0])
            assertEquals(-1, userDataRepository.userData.first().equalizerPreset)
        }

    @Test
    fun applyPreset_updatesManagerAndRepository() =
        runTest {
            viewModel.applyPreset(2)
            assertEquals(2, equalizerManager.state.value.currentPreset)
            assertEquals(2, userDataRepository.userData.first().equalizerPreset)
        }
}
