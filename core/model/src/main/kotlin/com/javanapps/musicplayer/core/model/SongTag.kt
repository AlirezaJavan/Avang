package com.javanapps.musicplayer.core.model

enum class TagSource { METADATA, AUDIO_RULES, AUDIO_MODEL }

data class SongTag(
    val songId: Long,
    val label: String,
    val confidence: Float,
    val source: TagSource,
)
