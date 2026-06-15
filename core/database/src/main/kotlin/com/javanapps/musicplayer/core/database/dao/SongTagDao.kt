package com.javanapps.musicplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    @Query("SELECT DISTINCT song_id FROM song_tags")
    suspend fun getAnalyzedSongIds(): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tags: List<SongTagEntity>)

    @Query("DELETE FROM song_tags WHERE song_id = :songId")
    suspend fun deleteForSong(songId: Long)

    @Transaction
    suspend fun replaceForSong(
        songId: Long,
        tags: List<SongTagEntity>,
    ) {
        deleteForSong(songId)
        upsert(tags)
    }

    @Query("DELETE FROM song_tags")
    suspend fun clearAll()
}
