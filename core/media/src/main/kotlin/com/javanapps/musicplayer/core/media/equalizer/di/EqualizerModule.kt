package com.javanapps.musicplayer.core.media.equalizer.di

import com.javanapps.musicplayer.core.domain.equalizer.EqualizerManager
import com.javanapps.musicplayer.core.media.equalizer.DefaultEqualizerManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EqualizerModule {
    @Binds
    @Singleton
    abstract fun bindEqualizerManager(equalizerManager: DefaultEqualizerManager): EqualizerManager
}
