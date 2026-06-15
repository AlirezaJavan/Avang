package com.javanapps.musicplayer.core.analysis.metadata

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class TrackMetadata(
    val year: Int?,
    val genre: String?,
)

class MetadataExtractor
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun read(uri: Uri): TrackMetadata {
            val retriever = MediaMetadataRetriever()
            return try {
                retriever.setDataSource(context, uri)
                TrackMetadata(year = retriever.year(), genre = retriever.genre())
            } catch (t: Throwable) {
                TrackMetadata(null, null)
            } finally {
                runCatching { retriever.release() }
            }
        }

        private fun MediaMetadataRetriever.year(): Int? {
            val raw =
                extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                    ?: extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
            return YEAR_PATTERN.find(raw ?: "")?.value?.toIntOrNull()
        }

        private fun MediaMetadataRetriever.genre(): String? =
            extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)?.trim()?.ifEmpty { null }

        private companion object {
            val YEAR_PATTERN = Regex("(19|20)\\d{2}")
        }
    }
