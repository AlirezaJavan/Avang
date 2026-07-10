package com.javanapps.musicplayer.core.analysis

import android.net.Uri
import android.util.Log
import com.javanapps.musicplayer.core.analysis.classify.MetadataTagger
import com.javanapps.musicplayer.core.analysis.classify.TagClassifier
import com.javanapps.musicplayer.core.analysis.decode.AudioDecoder
import com.javanapps.musicplayer.core.analysis.dsp.FeatureExtractor
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
        private val featureExtractor: FeatureExtractor,
        private val metadataExtractor: MetadataExtractor,
        private val metadataTagger: MetadataTagger,
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
                    return@withContext metadataTagger.tag(songId, metadata)
                }

                val fStart = System.currentTimeMillis()
                val features = featureExtractor.extract(pcm)
                val featuresTime = System.currentTimeMillis() - fStart

                val cStart = System.currentTimeMillis()
                val result = classifier.classify(songId, pcm, features, metadata)
                val classifyTime = System.currentTimeMillis() - cStart

                val total = System.currentTimeMillis() - start
                Log.d(
                    "SongAnalyzer",
                    "Analyzed song $songId: Total ${total}ms (Metadata: ${metadataTime}ms, Decode: ${decodeTime}ms, Features: ${featuresTime}ms, Classify: ${classifyTime}ms)",
                )

                result
            }
    }
