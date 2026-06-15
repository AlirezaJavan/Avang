package com.javanapps.musicplayer.core.analysis.di

import com.javanapps.musicplayer.core.analysis.classify.CompositeTagClassifier
import com.javanapps.musicplayer.core.analysis.classify.TagClassifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AnalysisModule {
    @Binds
    fun bindTagClassifier(classifier: CompositeTagClassifier): TagClassifier
}
