package com.javanapps.musicplayer.core.analysis.classify

import android.content.Context
import com.javanapps.musicplayer.core.analysis.decode.PcmAudio
import com.javanapps.musicplayer.core.analysis.dsp.MelSpectrogram
import com.javanapps.musicplayer.core.analysis.metadata.TrackMetadata
import com.javanapps.musicplayer.core.model.AudioFeatures
import com.javanapps.musicplayer.core.model.SongTag
import com.javanapps.musicplayer.core.model.TagSource
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

class TfLiteTagClassifier
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val melSpectrogram: MelSpectrogram,
        private val metadataTagger: MetadataTagger,
    ) : TagClassifier {
        private val interpreter: Interpreter? by lazy { loadInterpreter() }
        private val labels: List<String> by lazy { loadLabels() }

        fun isAvailable(): Boolean = interpreter != null && labels.isNotEmpty()

        override fun classify(
            songId: Long,
            pcm: PcmAudio,
            features: AudioFeatures,
            metadata: TrackMetadata,
        ): List<SongTag> {
            val tags = metadataTagger.tag(songId, metadata).toMutableList()
            val probabilities = interpreter?.let { runModel(it, pcm) } ?: return tags
            tags += modelTags(songId, probabilities)
            return tags
        }

        private fun modelTags(
            songId: Long,
            probabilities: FloatArray,
        ): List<SongTag> =
            probabilities
                .take(labels.size)
                .mapIndexedNotNull { index, probability ->
                    if (probability < MODEL_TAG_THRESHOLD) return@mapIndexedNotNull null
                    val display = LabelMap.displayName(labels[index]) ?: return@mapIndexedNotNull null
                    SongTag(songId, display, probability.coerceIn(0f, 1f), TagSource.AUDIO_MODEL)
                }.distinctBy { it.label }

        private fun runModel(
            interpreter: Interpreter,
            pcm: PcmAudio,
        ): FloatArray? =
            try {
                val mel = melSpectrogram.logMel(pcm.samples, pcm.sampleRate)
                if (mel.isEmpty()) return null
                val inputShape = interpreter.getInputTensor(0).shape()
                val outputShape = interpreter.getOutputTensor(0).shape()
                inferAveraged(interpreter, mel, inputShape, outputShape.last())
            } catch (t: Throwable) {
                null
            }

        private fun inferAveraged(
            interpreter: Interpreter,
            mel: Array<FloatArray>,
            inputShape: IntArray,
            labelCount: Int,
        ): FloatArray? {
            val frames = inputShape[FRAME_AXIS]
            val bands = inputShape[BAND_AXIS]
            if (bands != mel[0].size || frames <= 0) return null
            val accumulator = FloatArray(labelCount)
            var patches = 0
            var start = 0
            val inputBuffer = ByteBuffer.allocateDirect(frames * bands * 4).order(ByteOrder.nativeOrder())
            while (start + frames <= mel.size) {
                val output = Array(1) { FloatArray(labelCount) }
                inputBuffer.clear()
                for (f in 0 until frames) {
                    for (b in 0 until bands) {
                        inputBuffer.putFloat(mel[start + f][b])
                    }
                }
                inputBuffer.rewind()
                interpreter.run(inputBuffer, output)
                for (i in 0 until labelCount) accumulator[i] += output[0][i]
                patches++
                start += frames
            }
            if (patches == 0) return null
            return FloatArray(labelCount) { accumulator[it] / patches }
        }

        private fun loadInterpreter(): Interpreter? =
            try {
                context.assets.open(MODEL_ASSET).use { stream ->
                    val bytes = stream.readBytes()
                    val buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
                    buffer.put(bytes).rewind()
                    Interpreter(buffer)
                }
            } catch (t: Throwable) {
                null
            }

        private fun loadLabels(): List<String> =
            try {
                context.assets.open(LABELS_ASSET).bufferedReader().useLines { lines ->
                    lines.map { it.trim() }.filter { it.isNotEmpty() }.toList()
                }
            } catch (t: Throwable) {
                emptyList()
            }

        private companion object {
            const val MODEL_ASSET = "autotag.tflite"
            const val LABELS_ASSET = "autotag_labels.txt"
            const val MODEL_TAG_THRESHOLD = 0.5f
            const val FRAME_AXIS = 1
            const val BAND_AXIS = 2
        }
    }
