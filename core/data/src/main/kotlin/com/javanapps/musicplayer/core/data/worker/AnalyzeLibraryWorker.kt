package com.javanapps.musicplayer.core.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.javanapps.musicplayer.core.data.R
import com.javanapps.musicplayer.core.domain.repository.AnalysisRepository
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class AnalyzeLibraryWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted parameters: WorkerParameters,
        private val songsRepository: SongsRepository,
        private val analysisRepository: AnalysisRepository,
    ) : CoroutineWorker(context, parameters) {
        override suspend fun doWork(): Result {
            setForeground(createForegroundInfo(done = 0, total = 0))

            val analyzed = analysisRepository.analyzedSongIds()
            val pending = songsRepository.getSongs().first().filterNot { analyzed.contains(it.id) }
            pending.forEachIndexed { index, song ->
                if (isStopped) return Result.success()
                runCatching { analysisRepository.analyzeSong(song.id, song.mediaUri) }
                val done = index + 1
                setProgress(workDataOf(KEY_DONE to done, KEY_TOTAL to pending.size))
                setForeground(createForegroundInfo(done, pending.size))
            }
            return Result.success()
        }

        override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo(done = 0, total = 0)

        private fun createForegroundInfo(
            done: Int,
            total: Int,
        ): ForegroundInfo {
            ensureNotificationChannel()

            val notification =
                NotificationCompat
                    .Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_notify_sync)
                    .setContentTitle(applicationContext.getString(R.string.core_data_notification_analyzing_library))
                    .setContentText(
                        if (total > 0) {
                            applicationContext.getString(
                                R.string.core_data_notification_analyzing_library_progress,
                                done,
                                total,
                            )
                        } else {
                            null
                        },
                    ).setProgress(total, done, total == 0)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .build()

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                ForegroundInfo(NOTIFICATION_ID, notification)
            }
        }

        private fun ensureNotificationChannel() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = applicationContext.getSystemService(NotificationManager::class.java) ?: return
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    applicationContext.getString(R.string.core_data_notification_channel_library_sync),
                    NotificationManager.IMPORTANCE_LOW,
                ),
            )
        }

        companion object {
            const val UNIQUE_NAME = "analyze_library"
            const val PERIODIC_NAME = "analyze_library_periodic"
            const val KEY_DONE = "done"
            const val KEY_TOTAL = "total"
            private const val CHANNEL_ID = "library_sync"
            private const val NOTIFICATION_ID = 4201
        }
    }
