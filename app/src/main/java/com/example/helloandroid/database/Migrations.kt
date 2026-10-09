package com.example.helloandroid.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 添加 city 字段，默认值为空字符串
            db.execSQL(
                "ALTER TABLE users ADD COLUMN city TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE training_session_action_details " +
                        "ADD COLUMN restSeconds INTEGER NOT NULL DEFAULT 60"
            )
        }
    }

    val ALL = arrayOf(
        MIGRATION_8_9,
        MIGRATION_9_10
    )
}