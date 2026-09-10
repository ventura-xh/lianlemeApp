package com.example.helloandroid.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.helloandroid.dao.ActionDetailDao
import com.example.helloandroid.dao.ActionLibDAO
import com.example.helloandroid.dao.ActionMuscleDao
import com.example.helloandroid.dao.MuscleDao
import com.example.helloandroid.dao.PlanActionsDao
import com.example.helloandroid.dao.PlanFullDao
import com.example.helloandroid.dao.PlansDao
import com.example.helloandroid.dao.TrainingSessionActionDao
import com.example.helloandroid.dao.TrainingSessionActionDetailDao
import com.example.helloandroid.dao.TrainingSessionDao
import com.example.helloandroid.dao.UserDao
import com.example.helloandroid.entity.ActionDetailEntity
import com.example.helloandroid.entity.ActionLibEntity
import com.example.helloandroid.entity.ActionMuscleEntity
import com.example.helloandroid.entity.MuscleEntity
import com.example.helloandroid.entity.PlanActionsEntity
import com.example.helloandroid.entity.PlansEntity
import com.example.helloandroid.entity.TrainingSessionActionDetailEntity
import com.example.helloandroid.entity.TrainingSessionActionEntity
import com.example.helloandroid.entity.TrainingSessionEntity
import com.example.helloandroid.entity.UserEntity
import kotlin.concurrent.Volatile

@Database(
    entities = [
        ActionLibEntity::class,
        PlansEntity::class,
        ActionDetailEntity::class,
        PlanActionsEntity::class,
        TrainingSessionActionDetailEntity::class,
        TrainingSessionActionEntity::class,
        TrainingSessionEntity::class,
        MuscleEntity::class,
        ActionMuscleEntity::class,
        UserEntity::class
    ],
    version = 9,  // 20260910,完善用户数据信息
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun actionLibDAO(): ActionLibDAO
    abstract fun plansDao(): PlansDao
    abstract fun planActionsDao(): PlanActionsDao
    abstract fun actionDetailsDao(): ActionDetailDao
    abstract fun planFullDao(): PlanFullDao
    abstract fun trainingSessionDao(): TrainingSessionDao
    abstract fun trainingSessionActionDao(): TrainingSessionActionDao
    abstract fun trainingSessionActionDetailDao(): TrainingSessionActionDetailDao
    abstract fun muscleDao(): MuscleDao
    abstract fun actionMuscleDao(): ActionMuscleDao
    abstract fun userDao(): UserDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // ✅ 从版本 4 迁移到 5
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加 city 字段，默认值为空字符串
                db.execSQL(
                    "ALTER TABLE users ADD COLUMN city TEXT NOT NULL DEFAULT ''"
                )
            }
        }
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database.db"
                )
                .addMigrations(MIGRATION_8_9)
//                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}