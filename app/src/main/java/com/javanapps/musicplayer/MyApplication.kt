package com.javanapps.musicplayer

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.StrictMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.javanapps.musicplayer.core.data.worker.AnalysisScheduler
import com.javanapps.musicplayer.util.ProfileVerifierLogger
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication :
    Application(),
    SingletonImageLoader.Factory,
    Configuration.Provider {
    @Inject
    lateinit var imageLoader: dagger.Lazy<ImageLoader>

    @Inject
    lateinit var profileVerifierLogger: ProfileVerifierLogger

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var analysisScheduler: AnalysisScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        setStrictModePolicy()
        profileVerifierLogger()
        analysisScheduler.enqueue()
        analysisScheduler.enqueuePeriodic()
    }

    override fun newImageLoader(context: Context): ImageLoader = imageLoader.get()

    private fun isDebuggable(): Boolean = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE

    private fun setStrictModePolicy() {
        if (isDebuggable()) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy
                    .Builder()
                    .detectAll()
                    .penaltyLog()
                    .build(),
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy
                    .Builder()
                    .detectAll()
                    .penaltyLog()
                    .build(),
            )
        }
    }
}
