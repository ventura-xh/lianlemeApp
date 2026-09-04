package com.example.helloandroid.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.helloandroid.entity.TrainingSessionActionEntity

@Dao
interface TrainingSessionActionDao {

    // ========== 增 ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: TrainingSessionActionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(actions: List<TrainingSessionActionEntity>): List<Long>

    // ========== 删 ==========
    @Query("DELETE FROM training_session_actions WHERE id = :actionId")
    suspend fun deleteById(actionId: Long)

    @Query("DELETE FROM training_session_actions WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: Long)

    // ========== 改 ==========
    @Update
    suspend fun update(action: TrainingSessionActionEntity)

    @Query("UPDATE training_session_actions SET isCompleted = 1 WHERE id = :actionId")
    suspend fun markActionCompleted(actionId: Long)

    // ========== 查 ==========
    @Query("SELECT * FROM training_session_actions WHERE id = :actionId")
    suspend fun getActionById(actionId: Long): TrainingSessionActionEntity?

    @Query("SELECT * FROM training_session_actions WHERE sessionId = :sessionId ORDER BY sortOrder ASC")
    suspend fun getActionsBySessionId(sessionId: Long): List<TrainingSessionActionEntity>

    @Query("SELECT * FROM training_session_actions WHERE sessionId = :sessionId AND isCompleted = 0 ORDER BY sortOrder ASC LIMIT 1")
    suspend fun getCurrentAction(sessionId: Long): TrainingSessionActionEntity?

    @Query("SELECT COUNT(*) FROM training_session_actions WHERE sessionId = :sessionId AND isCompleted = 1")
    suspend fun getCompletedActionCount(sessionId: Long): Int

    @Query("SELECT COUNT(*) FROM training_session_actions WHERE sessionId = :sessionId")
    suspend fun getTotalActionCount(sessionId: Long): Int
}