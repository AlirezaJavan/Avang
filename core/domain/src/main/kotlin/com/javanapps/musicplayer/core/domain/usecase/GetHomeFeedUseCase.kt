package com.javanapps.musicplayer.core.domain.usecase

import com.javanapps.musicplayer.core.domain.repository.PlayHistoryRepository
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.core.model.HomeFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetHomeFeedUseCase(
    private val songsRepository: SongsRepository,
    private val playHistoryRepository: PlayHistoryRepository,
) {
    operator fun invoke(): Flow<HomeFeed> =
        combine(
            playHistoryRepository.observeRecentlyPlayed(),
            playHistoryRepository.observeMostPlayed(),
            songsRepository.observeRecentlyAdded(),
        ) { recentlyPlayed, mostPlayed, recentlyAdded ->
            HomeFeed(
                recentlyPlayed = recentlyPlayed,
                mostPlayed = mostPlayed,
                recentlyAdded = recentlyAdded,
            )
        }
}
