package com.javanapps.musicplayer.core.analysis.decode

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class PcmAudio(
    val samples: FloatArray,
    val sampleRate: Int,
    val durationMs: Long,
) {
    override fun equals(other: Any?): Boolean =
        this === other ||
            (other is PcmAudio && sampleRate == other.sampleRate && durationMs == other.durationMs && samples.contentEquals(other.samples))

    override fun hashCode(): Int = samples.contentHashCode() * 31 + sampleRate
}

class AudioDecoder
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun decode(
            uri: Uri,
            maxSeconds: Int = DEFAULT_MAX_SECONDS,
        ): PcmAudio? {
            val extractor = MediaExtractor()
            var codec: MediaCodec? = null
            return try {
                extractor.setDataSource(context, uri, null)
                val trackIndex = extractor.firstAudioTrack()
                if (trackIndex == null) {
                    Log.w(TAG, "No audio track found for $uri")
                    return null
                }
                val format = extractor.getTrackFormat(trackIndex)
                extractor.selectTrack(trackIndex)
                codec =
                    MediaCodec.createDecoderByType(format.mime()).apply {
                        configure(format, null, null, 0)
                        start()
                    }
                drainToPcm(codec, extractor, format, maxSeconds).also {
                    Log.d(TAG, "Decoded $uri: ${it.samples.size} samples @ ${it.sampleRate}Hz, ${it.durationMs}ms")
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to decode $uri", t)
                null
            } finally {
                runCatching { codec?.stop() }
                runCatching { codec?.release() }
                runCatching { extractor.release() }
            }
        }

        private fun drainToPcm(
            codec: MediaCodec,
            extractor: MediaExtractor,
            format: MediaFormat,
            maxSeconds: Int,
        ): PcmAudio {
            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val maxSamples = sampleRate.toLong() * maxSeconds
            val out = GrowableFloatBuffer(minOf(maxSamples, INITIAL_CAPACITY.toLong()).toInt())
            val info = MediaCodec.BufferInfo()
            var inputDone = false

            while (out.size < maxSamples) {
                if (!inputDone) inputDone = codec.feedInput(extractor)
                val outIndex = codec.dequeueOutputBuffer(info, TIMEOUT_US)
                if (outIndex >= 0) {
                    codec.appendMono(outIndex, info, channels, out)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
                }
            }
            return PcmAudio(out.toFloatArray(), sampleRate, format.durationMs())
        }

        private fun MediaCodec.feedInput(extractor: MediaExtractor): Boolean {
            val index = dequeueInputBuffer(TIMEOUT_US)
            if (index < 0) return false
            val buffer = getInputBuffer(index) ?: return false
            val size = extractor.readSampleData(buffer, 0)
            if (size < 0) {
                queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                return true
            }
            queueInputBuffer(index, 0, size, extractor.sampleTime, 0)
            extractor.advance()
            return false
        }

        private fun MediaCodec.appendMono(
            outIndex: Int,
            info: MediaCodec.BufferInfo,
            channels: Int,
            out: GrowableFloatBuffer,
        ) {
            val buffer = getOutputBuffer(outIndex)
            if (buffer != null && info.size > 0) {
                val shorts = ShortArray(info.size / 2)
                buffer.asShortBuffer().get(shorts)
                var i = 0
                while (i + channels <= shorts.size) {
                    var acc = 0
                    for (c in 0 until channels) acc += shorts[i + c].toInt()
                    out.add(acc / channels / PCM_SCALE)
                    i += channels
                }
            }
            releaseOutputBuffer(outIndex, false)
        }

        private fun MediaExtractor.firstAudioTrack(): Int? =
            (0 until trackCount).firstOrNull { index ->
                getTrackFormat(index).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            }

        private fun MediaFormat.mime(): String = getString(MediaFormat.KEY_MIME)!!

        private fun MediaFormat.durationMs(): Long =
            if (containsKey(MediaFormat.KEY_DURATION)) getLong(MediaFormat.KEY_DURATION) / MICROS_PER_MILLI else 0L

        private companion object {
            private const val TAG = "AudioDecoder"

            // 30s is plenty of signal for genre classification and halves per-song decode +
            // inference cost versus analyzing the full track.
            const val DEFAULT_MAX_SECONDS = 30
            const val TIMEOUT_US = 10_000L
            const val MICROS_PER_MILLI = 1000L
            const val PCM_SCALE = 32768f
            const val INITIAL_CAPACITY = 1_400_000
        }
    }

// Avoids ArrayList<Float> autoboxing, which allocates millions of Float objects per track decode.
private class GrowableFloatBuffer(
    initialCapacity: Int,
) {
    private var data = FloatArray(initialCapacity.coerceAtLeast(1))
    var size: Int = 0
        private set

    fun add(value: Float) {
        if (size == data.size) data = data.copyOf(data.size * 2)
        data[size] = value
        size++
    }

    fun toFloatArray(): FloatArray = data.copyOf(size)
}
