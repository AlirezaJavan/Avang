package com.javanapps.musicplayer.core.analysis

import android.net.Uri
import android.util.Log
import com.javanapps.musicplayer.core.analysis.classify.TagClassifier
import com.javanapps.musicplayer.core.analysis.decode.AudioDecoder
import com.javanapps.musicplayer.core.analysis.metadata.MetadataExtractor
import com.javanapps.musicplayer.core.common.dispatcher.Dispatcher
import com.javanapps.musicplayer.core.common.dispatcher.MusicPlayerDispatchers.IO
import com.javanapps.musicplayer.core.model.SongTag
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SongAnalyzer
    @Inject
    constructor(
        private val decoder: AudioDecoder,
        private val metadataExtractor: MetadataExtractor,
        private val classifier: TagClassifier,
        @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    ) {
        suspend fun analyze(
            songId: Long,
            mediaUri: Uri,
        ): List<SongTag> =
            withContext(ioDispatcher) {
                val start = System.currentTimeMillis()

                val mStart = System.currentTimeMillis()
                val metadata = metadataExtractor.read(mediaUri)
                val metadataTime = System.currentTimeMillis() - mStart

                val dStart = System.currentTimeMillis()
                val pcm = decoder.decode(mediaUri)
                val decodeTime = System.currentTimeMillis() - dStart

                if (pcm == null) {
                    Log.w("SongAnalyzer", "Song $songId: decode failed, skipping classification for $mediaUri")
                    return@withContext emptyList()
                }

                val cStart = System.currentTimeMillis()
                val result = classifier.classify(songId, pcm, metadata)
                val classifyTime = System.currentTimeMillis() - cStart

                val total = System.currentTimeMillis() - start
                Log.d(
                    "SongAnalyzer",
                    "Analyzed song $songId: Total ${total}ms (Metadata: ${metadataTime}ms, Decode: ${decodeTime}ms, " +
                        "Classify: ${classifyTime}ms), tags: ${result.joinToString { it.label }.ifEmpty { "(none)" }}",
                )

                result
            }
    }
