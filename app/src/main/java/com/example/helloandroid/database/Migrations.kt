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

    val ALL = arrayOf(
        MIGRATION_8_9
    )
}