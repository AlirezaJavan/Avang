package com.javanapps.musicplayer.core.analysis

import android.net.Uri
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
                val metadata = metadataExtractor.read(mediaUri)
                val pcm = decoder.decode(mediaUri) ?: return@withContext metadataTagger.tag(songId, metadata)
                val features = featureExtractor.extract(pcm)
                classifier.classify(songId, pcm, features, metadata)
            }
    }
