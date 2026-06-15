package com.javanapps.musicplayer.core.analysis.classify

import com.javanapps.musicplayer.core.analysis.decode.PcmAudio
import com.javanapps.musicplayer.core.analysis.metadata.TrackMetadata
import com.javanapps.musicplayer.core.model.AudioFeatures
import com.javanapps.musicplayer.core.model.SongTag
import javax.inject.Inject

class CompositeTagClassifier
    @Inject
    constructor(
        private val model: TfLiteTagClassifier,
        private val rules: RuleTagClassifier,
    ) : TagClassifier {
        override fun classify(
            songId: Long,
            pcm: PcmAudio,
            features: AudioFeatures,
            metadata: TrackMetadata,
        ): List<SongTag> {
            val classifier = if (model.isAvailable()) model else rules
            return classifier.classify(songId, pcm, features, metadata)
        }
    }
