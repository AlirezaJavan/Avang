package com.javanapps.musicplayer.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
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
            val analyzed = analysisRepository.analyzedSongIds()
            val pending = songsRepository.getSongs().first().filterNot { analyzed.contains(it.id) }
            pending.forEachIndexed { index, song ->
                if (isStopped) return Result.success()
                runCatching { analysisRepository.analyzeSong(song.id, song.mediaUri) }
                setProgress(workDataOf(KEY_DONE to index + 1, KEY_TOTAL to pending.size))
            }
            return Result.success()
        }

        companion object {
            const val UNIQUE_NAME = "analyze_library"
            const val KEY_DONE = "done"
            const val KEY_TOTAL = "total"
        }
    }
