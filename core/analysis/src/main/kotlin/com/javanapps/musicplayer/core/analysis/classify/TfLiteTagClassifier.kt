package com.javanapps.musicplayer.core.analysis.classify

import android.content.Context
import android.util.Log
import com.javanapps.musicplayer.core.analysis.decode.PcmAudio
import com.javanapps.musicplayer.core.analysis.dsp.MelSpectrogram
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

// Two-stage genre classifier built on Essentia/MTG's genre_discogs400: a discogs-effnet model
// turns each mel-spectrogram patch into a 1280-dim embedding, and a genre_discogs400
// classification head turns that embedding into a 400-way "Genre---Style" sigmoid prediction
// (e.g. "Electronic---Deep House"). Both were trained specifically to discriminate music genres
// (vs. YAMNet, a general sound-event tagger where genre was an incidental side-signal), which is
// why this replaced the previous YAMNet-based classifier.
//
// Both models (discogs_effnet.tflite, genre_discogs400.tflite) are licensed CC BY-NC-SA 4.0 by
// MTG-UPF (https://essentia.upf.edu/models.html) — fine for this non-commercial app as-is, but a
// commercial license would need to be purchased from MTG before ever monetizing the app.
class TfLiteTagClassifier
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : TagClassifier {
        // The library-wide analysis worker analyzes several songs concurrently. A pool of
        // interpreters per model (rather than one shared instance behind a lock) lets their model
        // inference actually run in parallel instead of queuing up on a single thread.
        private val embeddingPool: BlockingQueue<Interpreter>? by lazy { loadInterpreterPool(EMBEDDING_MODEL_ASSET) }
        private val classifierPool: BlockingQueue<Interpreter>? by lazy { loadInterpreterPool(CLASSIFIER_MODEL_ASSET) }
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
            val waveform = Resampler.resample(pcm.samples, pcm.sampleRate, MelSpectrogram.SAMPLE_RATE)
            val patches = MelSpectrogram.patches(waveform)
            if (patches.isEmpty()) {
                Log.w(TAG, "Song $songId: clip too short for a single mel-spectrogram patch (${waveform.size} samples)")
                return emptyList()
            }
            val predictions = classifyPatches(songId, patches)
            if (predictions == null) {
                Log.w(TAG, "Song $songId: model produced no output (interpreter pool unavailable?)")
                return emptyList()
            }
            logTopScores(songId, predictions)
            return modelTags(songId, predictions)
        }

        // Runs both models over every patch and averages the 400-dim sigmoid predictions across
        // patches, then the caller takes the argmax over that average. Averaging predictions
        // (rather than embeddings) is the standard approach for this model and is robust to a
        // clip having a short intro/outro that doesn't represent its overall genre.
        private fun classifyPatches(
            songId: Long,
            patches: List<Array<FloatArray>>,
        ): FloatArray? {
            val embeddingInterpreter = embeddingPool?.take()
            if (embeddingInterpreter == null) {
                Log.e(TAG, "Song $songId: embedding interpreter pool is null, $EMBEDDING_MODEL_ASSET likely failed to load")
                return null
            }
            val classifierInterpreter = classifierPool?.take()
            if (classifierInterpreter == null) {
                embeddingPool?.put(embeddingInterpreter)
                Log.e(TAG, "Song $songId: classifier interpreter pool is null, $CLASSIFIER_MODEL_ASSET likely failed to load")
                return null
            }
            return try {
                val accumulator = FloatArray(labels.size)
                for (patch in patches) {
                    val embedding = runEmbedding(embeddingInterpreter, patch)
                    val prediction = runClassifierHead(classifierInterpreter, embedding)
                    for (i in prediction.indices) accumulator[i] += prediction[i]
                }
                FloatArray(labels.size) { accumulator[it] / patches.size }
            } catch (t: Throwable) {
                Log.e(TAG, "Song $songId: model inference threw", t)
                null
            } finally {
                embeddingPool?.put(embeddingInterpreter)
                classifierPool?.put(classifierInterpreter)
            }
        }

        private fun runEmbedding(
            interpreter: Interpreter,
            patch: Array<FloatArray>,
        ): FloatArray {
            val input = arrayOf(patch)
            val output = Array(1) { FloatArray(EMBEDDING_SIZE) }
            interpreter.run(input, output)
            return output[0]
        }

        private fun runClassifierHead(
            interpreter: Interpreter,
            embedding: FloatArray,
        ): FloatArray {
            val input = arrayOf(embedding)
            val output = Array(1) { FloatArray(labels.size) }
            interpreter.run(input, output)
            return output[0]
        }

        // Logged unconditionally so a "nothing detected" report can be diagnosed from logcat.
        private fun logTopScores(
            songId: Long,
            predictions: FloatArray,
        ) {
            val top =
                predictions
                    .withIndex()
                    .sortedByDescending { it.value }
                    .take(TOP_SCORES_LOGGED)
                    .joinToString { (index, score) -> "${labels[index]}=${"%.3f".format(score)}" }
            Log.d(TAG, "Song $songId top scores: $top")
        }

        // Takes the single best-scoring Discogs style with no confidence floor: among 400
        // fine-grained styles the model was trained end-to-end to discriminate between (unlike
        // YAMNet's incidental genre classes), "the style that beat all 399 others" is already a
        // meaningful signal even at a modest absolute score. Only skipped if that winning style
        // falls under the "Non-Music" top-level genre (spoken word, field recordings, etc.).
        private fun modelTags(
            songId: Long,
            predictions: FloatArray,
        ): List<SongTag> {
            val topIndex = predictions.indices.maxByOrNull { predictions[it] }
            if (topIndex == null) {
                Log.d(TAG, "Song $songId: no predictions to rank")
                return emptyList()
            }
            val genre = LabelMap.topLevelGenre(labels[topIndex])
            if (genre == null) {
                Log.d(TAG, "Song $songId: top style '${labels[topIndex]}' is Non-Music, skipping")
                return emptyList()
            }
            val tag = SongTag(songId, genre, predictions[topIndex].coerceIn(0f, 1f), TagSource.AUDIO_MODEL)
            Log.d(TAG, "Song $songId classified as: ${tag.label}=${tag.confidence} (style: ${labels[topIndex]})")
            return listOf(tag)
        }

        private fun loadInterpreterPool(modelAsset: String): BlockingQueue<Interpreter>? =
            try {
                val bytes = context.assets.open(modelAsset).use { it.readBytes() }
                val threads = interpreterThreadCount()
                val pool = ArrayBlockingQueue<Interpreter>(INTERPRETER_POOL_SIZE)
                repeat(INTERPRETER_POOL_SIZE) {
                    val buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
                    buffer.put(bytes).rewind()
                    val options = Interpreter.Options().setNumThreads(threads)
                    pool.put(Interpreter(buffer, options))
                }
                Log.d(TAG, "Loaded interpreter pool for $modelAsset: $INTERPRETER_POOL_SIZE interpreters x $threads threads")
                pool
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to load $modelAsset", t)
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

            const val EMBEDDING_MODEL_ASSET = "discogs_effnet.tflite"
            const val CLASSIFIER_MODEL_ASSET = "genre_discogs400.tflite"
            const val LABELS_ASSET = "discogs_genre_labels.txt"
            const val EMBEDDING_SIZE = 1280

            // Matches AnalyzeLibraryWorker.MAX_CONCURRENT_ANALYSES, the number of songs the
            // library-wide analysis worker analyzes at the same time.
            const val INTERPRETER_POOL_SIZE = 3
            const val MAX_INTERPRETER_THREADS = 2
        }
    }
