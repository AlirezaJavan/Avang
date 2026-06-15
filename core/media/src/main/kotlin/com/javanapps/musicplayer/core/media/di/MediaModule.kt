package com.javanapps.musicplayer.core.media.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.javanapps.musicplayer.core.domain.controller.PlayerController
import com.javanapps.musicplayer.core.media.controller.Media3PlayerController
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface MediaModule {
    @Binds
    fun bindsPlayerController(playerController: Media3PlayerController): PlayerController

    companion object {
        @Provides
        @Singleton
        fun providesExoPlayer(
            @ApplicationContext context: Context,
        ): ExoPlayer =
            ExoPlayer
                .Builder(context)
                .setHandleAudioBecomingNoisy(true)
                .build()
    }
}
