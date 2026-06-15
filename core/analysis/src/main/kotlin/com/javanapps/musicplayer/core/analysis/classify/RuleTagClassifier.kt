package com.javanapps.musicplayer.core.analysis.classify

import com.javanapps.musicplayer.core.analysis.decode.PcmAudio
import com.javanapps.musicplayer.core.analysis.metadata.TrackMetadata
import com.javanapps.musicplayer.core.model.AudioFeatures
import com.javanapps.musicplayer.core.model.SongTag
import com.javanapps.musicplayer.core.model.TagSource
import javax.inject.Inject

class RuleTagClassifier
    @Inject
    constructor(
        private val metadataTagger: MetadataTagger,
    ) : TagClassifier {
        override fun classify(
            songId: Long,
            pcm: PcmAudio,
            features: AudioFeatures,
            metadata: TrackMetadata,
        ): List<SongTag> = metadataTagger.tag(songId, metadata) + moodTags(songId, features)

        private fun moodTags(
            songId: Long,
            features: AudioFeatures,
        ): List<SongTag> =
            listOfNotNull(
                calm(songId, features),
                energetic(songId, features),
                dance(songId, features),
                acoustic(songId, features),
                intense(songId, features),
            )

        private fun calm(
            songId: Long,
            f: AudioFeatures,
        ): SongTag? =
            if (f.rmsEnergy <= CALM_ENERGY_MAX && f.spectralFlux <= CALM_FLUX_MAX && f.estimatedTempoBpm in 1f..CALM_TEMPO_MAX) {
                tag(songId, "Calm", (1f - f.rmsEnergy) * (1f - f.spectralFlux))
            } else {
                null
            }

        private fun energetic(
            songId: Long,
            f: AudioFeatures,
        ): SongTag? =
            if (f.rmsEnergy >= ENERGY_HIGH && f.spectralFlux >= FLUX_HIGH) {
                tag(songId, "Energetic", (f.rmsEnergy + f.spectralFlux) / 2f)
            } else {
                null
            }

        private fun dance(
            songId: Long,
            f: AudioFeatures,
        ): SongTag? =
            if (f.estimatedTempoBpm in DANCE_TEMPO_MIN..DANCE_TEMPO_MAX && f.beatStrength >= DANCE_BEAT_MIN) {
                tag(songId, "Dance", f.beatStrength)
            } else {
                null
            }

        private fun acoustic(
            songId: Long,
            f: AudioFeatures,
        ): SongTag? =
            if (f.highFreqRatio <= ACOUSTIC_HF_MAX && f.zeroCrossingRate <= ACOUSTIC_ZCR_MAX) {
                tag(songId, "Acoustic", 1f - f.highFreqRatio)
            } else {
                null
            }

        private fun intense(
            songId: Long,
            f: AudioFeatures,
        ): SongTag? =
            if (f.estimatedTempoBpm >= INTENSE_TEMPO_MIN && f.rmsEnergy >= ENERGY_HIGH) {
                tag(songId, "Intense", (f.rmsEnergy + (f.estimatedTempoBpm / TEMPO_NORM)) / 2f)
            } else {
                null
            }

        private fun tag(
            songId: Long,
            label: String,
            confidence: Float,
        ) = SongTag(songId, label, confidence.coerceIn(0f, 1f), TagSource.AUDIO_RULES)

        private companion object {
            const val CALM_ENERGY_MAX = 0.35f
            const val CALM_FLUX_MAX = 0.30f
            const val CALM_TEMPO_MAX = 100f
            const val ENERGY_HIGH = 0.60f
            const val FLUX_HIGH = 0.50f
            const val DANCE_TEMPO_MIN = 95f
            const val DANCE_TEMPO_MAX = 135f
            const val DANCE_BEAT_MIN = 0.45f
            const val ACOUSTIC_HF_MAX = 0.12f
            const val ACOUSTIC_ZCR_MAX = 0.08f
            const val INTENSE_TEMPO_MIN = 130f
            const val TEMPO_NORM = 200f
        }
    }
