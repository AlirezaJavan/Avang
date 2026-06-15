package com.javanapps.musicplayer.core.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AnalysisScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun enqueue() {
            val request =
                OneTimeWorkRequestBuilder<AnalyzeLibraryWorker>()
                    .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                    .build()
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(AnalyzeLibraryWorker.UNIQUE_NAME, ExistingWorkPolicy.KEEP, request)
        }
    }
