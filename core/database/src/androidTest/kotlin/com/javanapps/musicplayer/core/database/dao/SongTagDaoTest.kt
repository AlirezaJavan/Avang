package com.javanapps.musicplayer.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.javanapps.musicplayer.core.database.MusicDatabase
import com.javanapps.musicplayer.core.database.model.SongTagEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SongTagDaoTest {
    private lateinit var db: MusicDatabase
    private lateinit var songTagDao: SongTagDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MusicDatabase::class.java).build()
        songTagDao = db.songTagDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun replaceForSong_persistsTags() =
        runTest {
            songTagDao.replaceForSong(1L, listOf(tag(1L, "Calm"), tag(1L, "Acoustic")), analyzedAt = 0L)

            val labels = songTagDao.getAllLabels().first()
            assertEquals(listOf("Acoustic", "Calm"), labels)
        }

    @Test
    fun replaceForSong_overwritesPreviousTags() =
        runTest {
            songTagDao.replaceForSong(1L, listOf(tag(1L, "Calm")), analyzedAt = 0L)
            songTagDao.replaceForSong(1L, listOf(tag(1L, "Dance")), analyzedAt = 0L)

            val labels = songTagDao.getAllLabels().first()
            assertEquals(listOf("Dance"), labels)
        }

    @Test
    fun getLabelCounts_countsDistinctSongsAboveConfidence() =
        runTest {
            songTagDao.upsert(
                listOf(
                    tag(1L, "Calm", confidence = 0.9f),
                    tag(2L, "Calm", confidence = 0.9f),
                    tag(3L, "Calm", confidence = 0.2f),
                ),
            )

            val counts = songTagDao.getLabelCounts(minConfidence = 0.5f).first()
            assertEquals(1, counts.size)
            assertEquals("Calm", counts[0].label)
            assertEquals(2, counts[0].songCount)
        }

    @Test
    fun getTagsByLabel_filtersByConfidence() =
        runTest {
            songTagDao.upsert(
                listOf(
                    tag(1L, "Dance", confidence = 0.8f),
                    tag(2L, "Dance", confidence = 0.3f),
                ),
            )

            val rows = songTagDao.getTagsByLabel("Dance", minConfidence = 0.5f).first()
            assertEquals(listOf(1L), rows.map { it.songId })
        }

    @Test
    fun clearAll_removesEverything() =
        runTest {
            songTagDao.replaceForSong(1L, listOf(tag(1L, "Calm")), analyzedAt = 0L)
            songTagDao.clearAll()

            assertTrue(songTagDao.getAllLabels().first().isEmpty())
        }

    @Test
    fun replaceForSong_withNoTags_stillMarksSongAnalyzed() =
        runTest {
            // A song can be analyzed and simply have no tag clear the confidence threshold.
            // It must not look "unanalyzed" or the worker will keep retrying it forever.
            songTagDao.replaceForSong(1L, emptyList(), analyzedAt = 123L)

            assertEquals(listOf(1L), songTagDao.getAnalyzedSongIds())
        }

    private fun tag(
        songId: Long,
        label: String,
        confidence: Float = 1f,
    ) = SongTagEntity(
        songId = songId,
        label = label,
        confidence = confidence,
        source = "AUDIO_RULES",
        analyzedAt = 0L,
    )
}
