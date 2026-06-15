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
