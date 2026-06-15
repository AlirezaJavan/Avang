package com.javanapps.musicplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.javanapps.musicplayer.core.database.model.PlaylistEntity
import com.javanapps.musicplayer.core.database.model.PlaylistEntityWithCount
import com.javanapps.musicplayer.core.database.model.PlaylistSongCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query(
        """
        SELECT p.id, p.name, COUNT(ref.song_id) as songCount
        FROM playlists p
        LEFT JOIN playlist_song_cross_ref ref ON p.id = ref.playlist_id
        GROUP BY p.id
    """,
    )
    fun getPlaylists(): Flow<List<PlaylistEntityWithCount>>

    @Query(
        """
        SELECT p.id, p.name, COUNT(ref.song_id) as songCount
        FROM playlists p
        LEFT JOIN playlist_song_cross_ref ref ON p.id = ref.playlist_id
        WHERE p.id = :id
        GROUP BY p.id
    """,
    )
    fun getPlaylist(id: Long): Flow<PlaylistEntityWithCount?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)

    @Delete
    suspend fun deletePlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)

    @Query("SELECT song_id FROM playlist_song_cross_ref WHERE playlist_id = :playlistId")
    fun getSongIdsInPlaylist(playlistId: Long): Flow<List<Long>>

    @Transaction
    @Query("DELETE FROM playlist_song_cross_ref WHERE playlist_id = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)
}
