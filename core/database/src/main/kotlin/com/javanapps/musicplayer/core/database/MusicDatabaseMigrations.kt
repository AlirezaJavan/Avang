package com.javanapps.musicplayer.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS song_tags (
                    song_id INTEGER NOT NULL,
                    label TEXT NOT NULL,
                    confidence REAL NOT NULL,
                    source TEXT NOT NULL,
                    analyzed_at INTEGER NOT NULL,
                    PRIMARY KEY(song_id, label)
                )
                """.trimIndent(),
            )
        }
    }

val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS analyzed_songs (
                    song_id INTEGER NOT NULL,
                    analyzed_at INTEGER NOT NULL,
                    PRIMARY KEY(song_id)
                )
                """.trimIndent(),
            )
            // Songs that already have a tag row were already analyzed; carry that status
            // forward so they aren't needlessly re-analyzed after the upgrade.
            db.execSQL(
                """
                INSERT OR IGNORE INTO analyzed_songs (song_id, analyzed_at)
                SELECT song_id, MAX(analyzed_at) FROM song_tags GROUP BY song_id
                """.trimIndent(),
            )
        }
    }
