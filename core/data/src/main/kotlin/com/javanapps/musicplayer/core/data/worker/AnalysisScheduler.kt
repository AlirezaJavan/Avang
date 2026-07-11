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
        /**
         * Analyzes right away, e.g. right after a manual library refresh.
         *
         * [replace] cancels any in-flight analysis and starts a fresh run instead of leaving it
         * be — needed for an explicit "start over" action, where a stale in-flight run would
         * otherwise have already computed its pending-songs list before a rescan cleared it.
         */
        fun enqueue(replace: Boolean = false) {
            val request =
                OneTimeWorkRequestBuilder<AnalyzeLibraryWorker>()
                    .setConstraints(analysisConstraints())
                    .build()
            val policy = if (replace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(AnalyzeLibraryWorker.UNIQUE_NAME, policy, request)
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
