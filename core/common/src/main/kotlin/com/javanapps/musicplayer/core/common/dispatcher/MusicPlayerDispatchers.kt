package com.javanapps.musicplayer.core.common.dispatcher

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(
    val dispatcher: MusicPlayerDispatchers,
)

enum class MusicPlayerDispatchers {
    Default,
    IO,
}
