package com.javanapps.musicplayer.core.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AnalysisScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /** Analyzes right away, e.g. right after a manual library refresh. */
        fun enqueue() {
            val request =
                OneTimeWorkRequestBuilder<AnalyzeLibraryWorker>()
                    .setConstraints(analysisConstraints())
                    .build()
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(AnalyzeLibraryWorker.UNIQUE_NAME, ExistingWorkPolicy.KEEP, request)
        }

        /**
         * Keeps categorizing newly added songs into smart playlists even if the app is never
         * reopened, so the library doesn't silently fall behind after new music is copied in.
         */
        fun enqueuePeriodic() {
            val request =
                PeriodicWorkRequestBuilder<AnalyzeLibraryWorker>(PERIODIC_INTERVAL_HOURS, TimeUnit.HOURS)
                    .setConstraints(analysisConstraints())
                    .build()
            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(
                    AnalyzeLibraryWorker.PERIODIC_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request,
                )
        }

        private fun analysisConstraints(): Constraints =
            Constraints
                .Builder()
                .setRequiresBatteryNotLow(true)
                .build()

        private companion object {
            const val PERIODIC_INTERVAL_HOURS = 12L
        }
    }
