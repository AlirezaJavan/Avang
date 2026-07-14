package com.javanapps.musicplayer

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.StrictMode
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.javanapps.musicplayer.core.common.dispatcher.di.ApplicationScope
import com.javanapps.musicplayer.core.data.worker.AnalysisScheduler
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.util.ProfileVerifierLogger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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

    @Inject
    lateinit var songsRepository: SongsRepository

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        setStrictModePolicy()
        profileVerifierLogger()
        applicationScope.launch {
            analysisScheduler.enqueuePeriodic()
        }
        // The library rescan is intentionally tied to the process lifecycle (app open,
        // background -> foreground) rather than to any screen being visited, so it runs
        // exactly once per app-visible session instead of on every Library screen entry.
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    if (hasAudioPermission()) {
                        applicationScope.launch { songsRepository.refresh() }
                    }
                }
            },
        )
    }

    private fun hasAudioPermission(): Boolean {
        val permission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun newImageLoader(context: Context): ImageLoader = imageLoader.get()

    private fun isDebuggable(): Boolean = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE

    private fun setStrictModePolicy() {
        if (isDebuggable()) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy
                    .Builder()
                    .detectAll()
                    .permitDiskReads() // Workaround for Media3/MediaSession disk read violations during metadata updates
                    .penaltyLog()
                    .penaltyFlashScreen()
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
