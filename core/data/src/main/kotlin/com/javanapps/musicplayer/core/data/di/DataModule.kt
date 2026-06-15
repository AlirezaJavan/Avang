package com.javanapps.musicplayer.core.data.di

import android.content.ContentResolver
import android.content.Context
import com.javanapps.musicplayer.core.data.repository.OfflineAnalysisRepository
import com.javanapps.musicplayer.core.data.repository.OfflineFavoritesRepository
import com.javanapps.musicplayer.core.data.repository.OfflineNotesRepository
import com.javanapps.musicplayer.core.data.repository.OfflinePlaylistRepository
import com.javanapps.musicplayer.core.data.repository.OfflineSongsRepository
import com.javanapps.musicplayer.core.data.repository.OfflineUserDataRepository
import com.javanapps.musicplayer.core.domain.repository.AnalysisRepository
import com.javanapps.musicplayer.core.domain.repository.FavoritesRepository
import com.javanapps.musicplayer.core.domain.repository.NotesRepository
import com.javanapps.musicplayer.core.domain.repository.PlaylistRepository
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.core.domain.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindsSongsRepository(songsRepository: OfflineSongsRepository): SongsRepository

    @Binds
    fun bindsPlaylistRepository(playlistRepository: OfflinePlaylistRepository): PlaylistRepository

    @Binds
    fun bindsFavoritesRepository(favoritesRepository: OfflineFavoritesRepository): FavoritesRepository

    @Binds
    fun bindsNotesRepository(notesRepository: OfflineNotesRepository): NotesRepository

    @Binds
    fun bindsUserDataRepository(userDataRepository: OfflineUserDataRepository): UserDataRepository

    @Binds
    fun bindsAnalysisRepository(analysisRepository: OfflineAnalysisRepository): AnalysisRepository

    companion object {
        @Provides
        @Singleton
        fun providesContentResolver(
            @ApplicationContext context: Context,
        ): ContentResolver = context.contentResolver
    }
}
