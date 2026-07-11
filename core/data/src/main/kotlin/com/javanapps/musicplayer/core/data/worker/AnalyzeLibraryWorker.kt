package com.javanapps.musicplayer.core.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.javanapps.musicplayer.core.data.R
import com.javanapps.musicplayer.core.domain.repository.AnalysisRepository
import com.javanapps.musicplayer.core.domain.repository.SongsRepository
import com.javanapps.musicplayer.core.model.Song
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.atomic.AtomicInteger

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
            Log.d(TAG, "Starting analysis: ${pending.size} pending of ${analyzed.size + pending.size} total songs")
            analyzeConcurrently(pending)
            return Result.success()
        }

        // Decode+analyze is CPU/IO-bound per song and independent across songs, so a bounded
        // number run concurrently instead of one full track at a time.
        private suspend fun analyzeConcurrently(pending: List<Song>) =
            coroutineScope {
                val semaphore = Semaphore(MAX_CONCURRENT_ANALYSES)
                val done = AtomicInteger(0)
                pending
                    .map { song ->
                        async {
                            if (isStopped) return@async
                            semaphore.withPermit {
                                if (isStopped) return@withPermit
                                runCatching { analysisRepository.analyzeSong(song.id, song.mediaUri) }
                                    .onFailure { Log.e(TAG, "Failed to analyze song ${song.id} (${song.mediaUri})", it) }
                                val completed = done.incrementAndGet()
                                setProgress(workDataOf(KEY_DONE to completed, KEY_TOTAL to pending.size))
                                setForeground(createForegroundInfo(completed, pending.size))
                            }
                        }
                    }.forEach { it.await() }
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
            private const val TAG = "AnalyzeLibraryWorker"
            private const val CHANNEL_ID = "library_sync"
            private const val NOTIFICATION_ID = 4201
            private const val MAX_CONCURRENT_ANALYSES = 3
        }
    }
