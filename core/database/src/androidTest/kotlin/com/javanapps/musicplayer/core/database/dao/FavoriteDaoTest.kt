package com.javanapps.musicplayer.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.javanapps.musicplayer.core.database.MusicDatabase
import com.javanapps.musicplayer.core.database.model.FavoriteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FavoriteDaoTest {
    private lateinit var db: MusicDatabase
    private lateinit var favoriteDao: FavoriteDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MusicDatabase::class.java).build()
        favoriteDao = db.favoriteDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetFavorite() =
        runTest {
            val favorite = FavoriteEntity(songId = 1L)
            favoriteDao.insertFavorite(favorite)

            val favorites = favoriteDao.getFavoriteSongIds().first()
            assertEquals(1, favorites.size)
            assertEquals(1L, favorites[0])
        }

    @Test
    fun deleteFavorite() =
        runTest {
            val favorite = FavoriteEntity(songId = 1L)
            favoriteDao.insertFavorite(favorite)
            favoriteDao.deleteFavorite(favorite)

            val favorites = favoriteDao.getFavoriteSongIds().first()
            assertTrue(favorites.isEmpty())
        }

    @Test
    fun isFavorite() =
        runTest {
            val favorite = FavoriteEntity(songId = 1L)
            favoriteDao.insertFavorite(favorite)

            assertTrue(favoriteDao.isFavorite(1L).first())
        }
}
