package com.javanapps.musicplayer.core.analysis.classify

import android.content.Context
import android.util.Log
import com.javanapps.musicplayer.core.analysis.decode.PcmAudio
import com.javanapps.musicplayer.core.analysis.dsp.Resampler
import com.javanapps.musicplayer.core.analysis.metadata.TrackMetadata
import com.javanapps.musicplayer.core.model.SongTag
import com.javanapps.musicplayer.core.model.TagSource
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import javax.inject.Inject

class TfLiteTagClassifier
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : TagClassifier {
        // The library-wide analysis worker analyzes several songs concurrently. A pool of
        // interpreters (rather than one shared instance behind a lock) lets their model
        // inference actually run in parallel instead of queuing up on a single thread.
        private val interpreterPool: BlockingQueue<Interpreter>? by lazy { loadInterpreterPool() }
        private val labels: List<String> by lazy { loadLabels() }

        override fun classify(
            songId: Long,
            pcm: PcmAudio,
            metadata: TrackMetadata,
        ): List<SongTag> {
            if (labels.isEmpty()) {
                Log.e(TAG, "Song $songId: labels failed to load from $LABELS_ASSET, skipping classification")
                return emptyList()
            }
            val probabilities = runModel(pcm)
            if (probabilities == null) {
                Log.w(TAG, "Song $songId: model produced no output (interpreter pool unavailable or clip too short?)")
                return emptyList()
            }
            logTopScores(songId, probabilities)
            return modelTags(songId, probabilities)
        }

        // Logged unconditionally (top-N regardless of LabelMap coverage) so a "nothing detected"
        // report can be diagnosed from logcat: are scores near zero (broken model input), or is
        // the clip confidently non-music (e.g. "Speech" outscoring "Music")?
        private fun logTopScores(
            songId: Long,
            probabilities: FloatArray,
        ) {
            val top =
                probabilities
                    .take(labels.size)
                    .withIndex()
                    .sortedByDescending { it.value }
                    .take(TOP_SCORES_LOGGED)
                    .joinToString { (index, score) -> "${labels[index]}=${"%.3f".format(score)}" }
            Log.d(TAG, "Song $songId top scores: $top")
        }

        // YAMNet's multi-label sigmoid gives "Music" 0.7-0.98 on real tracks, dwarfing every
        // genre subclass (logged production data shows real genre signal landing as low as
        // 0.03-0.2). Trying to threshold genre subclasses directly — as if this were a dedicated
        // genre classifier — filters out real hits along with the noise. Instead: confirm the
        // clip is music at all (its top-scoring class must be "Music"), then trust the model's
        // own ranking and take whichever genre subclass it scored highest, with no floor — among
        // ~500 mostly-irrelevant classes, "the one genre label that beat all other genre labels"
        // is already a meaningful signal even at a low absolute score.
        private fun modelTags(
            songId: Long,
            probabilities: FloatArray,
        ): List<SongTag> {
            val scored = probabilities.take(labels.size)
            val topIndex = scored.indices.maxByOrNull { scored[it] }
            if (topIndex == null || labels[topIndex] != MUSIC_LABEL) {
                Log.d(TAG, "Song $songId: not confidently music (top class: ${topIndex?.let { labels[it] }}), skipping")
                return emptyList()
            }

            val bestGenre =
                scored
                    .withIndex()
                    .mapNotNull { (index, probability) -> LabelMap.displayName(labels[index])?.let { it to probability } }
                    .maxByOrNull { (_, probability) -> probability }

            val tags =
                bestGenre
                    ?.let { (label, probability) ->
                        listOf(SongTag(songId, label, probability.coerceIn(0f, 1f), TagSource.AUDIO_MODEL))
                    }.orEmpty()
            Log.d(TAG, "Song $songId classified as: ${tags.joinToString { "${it.label}=${it.confidence}" }.ifEmpty { "(none)" }}")
            return tags
        }

        // YAMNet's TFLite graph takes a raw 16kHz mono waveform and computes its own log-mel
        // spectrogram internally, so no external feature extraction is needed here.
        private fun runModel(pcm: PcmAudio): FloatArray? =
            try {
                val waveform = Resampler.resample(pcm.samples, pcm.sampleRate, TARGET_SAMPLE_RATE)
                val pool = interpreterPool
                if (pool == null) {
                    Log.e(TAG, "Interpreter pool is null, model asset ($MODEL_ASSET) likely failed to load")
                    return null
                }
                val interpreter = pool.take()
                try {
                    val windowSize = interpreter.getInputTensor(0).shape().single()
                    val labelCount = interpreter.getOutputTensor(0).shape().last()
                    if (waveform.size < windowSize) {
                        Log.w(
                            TAG,
                            "Waveform too short for a single window: ${waveform.size} samples < $windowSize required",
                        )
                        null
                    } else {
                        inferAveraged(interpreter, waveform, windowSize, labelCount)
                    }
                } finally {
                    pool.put(interpreter)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Model inference threw", t)
                null
            }

        private fun inferAveraged(
            interpreter: Interpreter,
            waveform: FloatArray,
            windowSize: Int,
            labelCount: Int,
        ): FloatArray? {
            val accumulator = FloatArray(labelCount)
            var windows = 0
            var start = 0
            val inputBuffer = ByteBuffer.allocateDirect(windowSize * 4).order(ByteOrder.nativeOrder())
            while (start + windowSize <= waveform.size) {
                val output = Array(1) { FloatArray(labelCount) }
                inputBuffer.clear()
                for (i in 0 until windowSize) inputBuffer.putFloat(waveform[start + i])
                inputBuffer.rewind()
                interpreter.run(inputBuffer, output)
                for (i in 0 until labelCount) accumulator[i] += output[0][i]
                windows++
                start += windowSize
            }
            if (windows == 0) return null
            return FloatArray(labelCount) { accumulator[it] / windows }
        }

        private fun loadInterpreterPool(): BlockingQueue<Interpreter>? =
            try {
                val bytes = context.assets.open(MODEL_ASSET).use { it.readBytes() }
                val threads = interpreterThreadCount()
                val pool = ArrayBlockingQueue<Interpreter>(INTERPRETER_POOL_SIZE)
                repeat(INTERPRETER_POOL_SIZE) {
                    val buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
                    buffer.put(bytes).rewind()
                    val options = Interpreter.Options().setNumThreads(threads)
                    pool.put(Interpreter(buffer, options))
                }
                Log.d(TAG, "Loaded interpreter pool: $INTERPRETER_POOL_SIZE interpreters x $threads threads")
                pool
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to load $MODEL_ASSET", t)
                null
            }

        // Threads are split across the pool instead of given fully to each interpreter, since
        // up to INTERPRETER_POOL_SIZE interpreters now run inference on different cores at once.
        private fun interpreterThreadCount(): Int =
            (Runtime.getRuntime().availableProcessors() / INTERPRETER_POOL_SIZE).coerceIn(1, MAX_INTERPRETER_THREADS)

        private fun loadLabels(): List<String> =
            try {
                context.assets
                    .open(LABELS_ASSET)
                    .bufferedReader()
                    .useLines { lines ->
                        lines.map { it.trim() }.filter { it.isNotEmpty() }.toList()
                    }.also { Log.d(TAG, "Loaded ${it.size} labels from $LABELS_ASSET") }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to load $LABELS_ASSET", t)
                emptyList()
            }

        private companion object {
            private const val TAG = "TfLiteTagClassifier"
            private const val TOP_SCORES_LOGGED = 5

            const val MODEL_ASSET = "autotag.tflite"
            const val LABELS_ASSET = "autotag_labels.txt"
            const val MUSIC_LABEL = "Music"
            const val TARGET_SAMPLE_RATE = 16_000

            // Matches AnalyzeLibraryWorker.MAX_CONCURRENT_ANALYSES, the number of songs the
            // library-wide analysis worker analyzes at the same time.
            const val INTERPRETER_POOL_SIZE = 3
            const val MAX_INTERPRETER_THREADS = 2
        }
    }
