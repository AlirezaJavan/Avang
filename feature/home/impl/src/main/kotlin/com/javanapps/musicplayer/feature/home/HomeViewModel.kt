package com.javanapps.musicplayer.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javanapps.musicplayer.core.common.dispatcher.Dispatcher
import com.javanapps.musicplayer.core.common.dispatcher.MusicPlayerDispatchers.Default
import com.javanapps.musicplayer.core.domain.controller.PlayerController
import com.javanapps.musicplayer.core.domain.repository.PlayHistoryRepository
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.core.domain.repository.UserDataRepository
import com.javanapps.musicplayer.core.domain.usecase.GetHomeFeedUseCase
import com.javanapps.musicplayer.core.model.HomeFeed
import com.javanapps.musicplayer.core.model.PlayerState
import com.javanapps.musicplayer.core.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        songsRepository: SongsRepository,
        playHistoryRepository: PlayHistoryRepository,
        userDataRepository: UserDataRepository,
        @Dispatcher(Default) defaultDispatcher: CoroutineDispatcher,
        private val playerController: PlayerController,
    ) : ViewModel() {
        private val getHomeFeedUseCase =
            GetHomeFeedUseCase(songsRepository, playHistoryRepository, defaultDispatcher)

        val uiState: StateFlow<HomeUiState> =
            combine(
                getHomeFeedUseCase(),
                playerController.playerState
                    .map { it.copy(currentPosition = 0) }
                    .distinctUntilChanged(),
                userDataRepository.userData,
            ) { feed, playerState, userData ->
                HomeUiState.Success(feed, playerState, userData.useAnimations)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState.Loading,
            )

        fun playFromShelf(
            songs: List<Song>,
            index: Int,
        ) {
            playerController.play(songs, index)
        }

        fun resume() = playerController.resume()

        fun pause() = playerController.pause()
    }

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val feed: HomeFeed,
        val playerState: PlayerState,
        val useAnimations: Boolean,
    ) : HomeUiState {
        val isEmpty: Boolean
            get() =
                feed.recentlyPlayed.isEmpty() &&
                    feed.mostPlayed.isEmpty() &&
                    feed.recentlyAdded.isEmpty()
    }
}
