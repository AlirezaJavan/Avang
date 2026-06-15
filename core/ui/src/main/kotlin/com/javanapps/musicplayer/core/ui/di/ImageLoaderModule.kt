package com.javanapps.musicplayer.core.ui.di

import android.content.Context
import coil3.ImageLoader
import coil3.util.DebugLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageLoaderModule {
    @Provides
    @Singleton
    fun providesImageLoader(
        @ApplicationContext context: Context,
    ): ImageLoader =
        ImageLoader
            .Builder(context)
            .apply {
                // Since it's an offline player, we might want to configure caching or other things
                // But default is usually fine for local URIs
                logger(DebugLogger())
            }.build()
}
