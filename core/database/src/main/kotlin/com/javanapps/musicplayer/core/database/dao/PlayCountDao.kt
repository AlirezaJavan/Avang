package com.javanapps.musicplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.javanapps.musicplayer.core.database.model.PlayCountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayCountDao {
    @Query("SELECT * FROM play_counts WHERE song_id = :songId")
    fun getPlayCount(songId: Long): Flow<PlayCountEntity?>

    @Query("SELECT * FROM play_counts ORDER BY count DESC")
    fun getMostPlayed(): Flow<List<PlayCountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayCount(playCount: PlayCountEntity)

    @Transaction
    suspend fun incrementPlayCount(songId: Long) {
        val current = getPlayCountSync(songId)
        val newCount = (current?.count ?: 0) + 1
        insertPlayCount(
            PlayCountEntity(
                songId = songId,
                count = newCount,
                lastPlayed = System.currentTimeMillis(),
            ),
        )
    }

    @Query("SELECT * FROM play_counts WHERE song_id = :songId")
    suspend fun getPlayCountSync(songId: Long): PlayCountEntity?
}
