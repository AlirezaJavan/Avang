package com.javanapps.musicplayer.core.database.model

import com.javanapps.musicplayer.core.model.Playlist
import com.javanapps.musicplayer.core.model.SongNote
import com.javanapps.musicplayer.core.model.SongTag
import com.javanapps.musicplayer.core.model.TagSource

fun PlaylistEntityWithCount.asExternalModel(): Playlist =
    Playlist(
        id = id,
        name = name,
        songCount = songCount,
    )

fun SongNoteEntity.asExternalModel(): SongNote =
    SongNote(
        songId = songId,
        note = note,
        timestamp = timestamp,
    )

fun SongTagEntity.asExternalModel(): SongTag =
    SongTag(
        songId = songId,
        label = label,
        confidence = confidence,
        source = TagSource.valueOf(source),
    )

fun SongTag.asEntity(analyzedAt: Long): SongTagEntity =
    SongTagEntity(
        songId = songId,
        label = label,
        confidence = confidence,
        source = source.name,
        analyzedAt = analyzedAt,
    )
