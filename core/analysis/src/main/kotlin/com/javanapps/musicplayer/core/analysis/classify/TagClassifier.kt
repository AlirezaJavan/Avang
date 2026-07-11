package com.javanapps.musicplayer.core.analysis.classify

import com.javanapps.musicplayer.core.analysis.decode.PcmAudio
import com.javanapps.musicplayer.core.analysis.metadata.TrackMetadata
import com.javanapps.musicplayer.core.model.SongTag

interface TagClassifier {
    fun classify(
        songId: Long,
        pcm: PcmAudio,
        metadata: TrackMetadata,
    ): List<SongTag>
}
