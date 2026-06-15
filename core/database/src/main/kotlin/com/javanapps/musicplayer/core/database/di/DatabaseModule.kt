package com.javanapps.musicplayer.core.database.di

import android.content.Context
import androidx.room.Room
import com.javanapps.musicplayer.core.database.MIGRATION_1_2
import com.javanapps.musicplayer.core.database.MusicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providesMusicDatabase(
        @ApplicationContext context: Context,
    ): MusicDatabase =
        Room
            .databaseBuilder(
                context,
                MusicDatabase::class.java,
                "music-database",
            ).addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun providesPlaylistDao(database: MusicDatabase) = database.playlistDao()

    @Provides
    fun providesFavoriteDao(database: MusicDatabase) = database.favoriteDao()

    @Provides
    fun providesSongNoteDao(database: MusicDatabase) = database.songNoteDao()

    @Provides
    fun providesPlayCountDao(database: MusicDatabase) = database.playCountDao()

    @Provides
    fun providesSongTagDao(database: MusicDatabase) = database.songTagDao()
}
