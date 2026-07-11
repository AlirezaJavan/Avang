package com.javanapps.musicplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.javanapps.musicplayer.core.database.model.AnalyzedSongEntity
import com.javanapps.musicplayer.core.database.model.LabelCount
import com.javanapps.musicplayer.core.database.model.SongTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongTagDao {
    @Query("SELECT * FROM song_tags")
    fun getAllTags(): Flow<List<SongTagEntity>>

    @Query(
        "SELECT label AS label, COUNT(DISTINCT song_id) AS songCount FROM song_tags " +
            "WHERE confidence >= :minConfidence GROUP BY label ORDER BY label ASC",
    )
    fun getLabelCounts(minConfidence: Float): Flow<List<LabelCount>>

    @Query("SELECT * FROM song_tags WHERE label = :label AND confidence >= :minConfidence")
    fun getTagsByLabel(
        label: String,
        minConfidence: Float,
    ): Flow<List<SongTagEntity>>

    @Query("SELECT DISTINCT label FROM song_tags ORDER BY label ASC")
    fun getAllLabels(): Flow<List<String>>

    @Query("SELECT song_id FROM analyzed_songs")
    suspend fun getAnalyzedSongIds(): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tags: List<SongTagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markAnalyzed(analyzed: AnalyzedSongEntity)

    @Query("DELETE FROM song_tags WHERE song_id = :songId")
    suspend fun deleteForSong(songId: Long)

    @Transaction
    suspend fun replaceForSong(
        songId: Long,
        tags: List<SongTagEntity>,
        analyzedAt: Long,
    ) {
        deleteForSong(songId)
        upsert(tags)
        markAnalyzed(AnalyzedSongEntity(songId, analyzedAt))
    }

    @Query("DELETE FROM song_tags")
    suspend fun clearAllTags()

    @Query("DELETE FROM analyzed_songs")
    suspend fun clearAllAnalyzed()

    @Transaction
    suspend fun clearAll() {
        clearAllTags()
        clearAllAnalyzed()
    }
}
