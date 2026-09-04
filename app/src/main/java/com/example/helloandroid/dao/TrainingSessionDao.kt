package com.example.helloandroid.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.helloandroid.entity.TrainingSessionEntity
import com.example.helloandroid.entity.model.TrainingSessionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingSessionDao {
    // ========== 增 ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: TrainingSessionEntity): Long

    // ========== 删 ==========
    @Query("DELETE FROM training_sessions WHERE id = :sessionId")
    suspend fun deleteById(sessionId: Long)

    @Query("DELETE FROM training_sessions")
    suspend fun deleteAll()

    // ========== 改 ==========
    @Update
    suspend fun update(session: TrainingSessionEntity)

    @Query("UPDATE training_sessions SET endTime = :endTime, totalDuration = :totalDuration, status = :status WHERE id = :sessionId")
    suspend fun finishSession(
        sessionId: Long,
        endTime: Long,
        totalDuration: Long,
        status: Int
    )

    // ========== 查 ==========
    @Transaction
    @Query("SELECT * FROM training_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): TrainingSessionEntity?

    @Query("SELECT * FROM training_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<TrainingSessionEntity>>

    /**
     * 获取所有训练会话（一次性查询）
     */
    @Query("SELECT * FROM training_sessions ORDER BY startTime DESC")
    suspend fun getAllSessionsOnce(): List<TrainingSessionEntity>

    @Query("SELECT * FROM training_sessions WHERE status = :status ORDER BY startTime DESC")
    suspend fun getSessionsByStatus(status: Int): List<TrainingSessionEntity>

    @Query("SELECT * FROM training_sessions WHERE planId = :planId ORDER BY startTime DESC")
    suspend fun getSessionsByPlanId(planId: Long): List<TrainingSessionEntity>

    // ✅ 获取进行中的训练会话（应该只有一个）
    @Query("SELECT * FROM training_sessions WHERE status = 0 ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSession(): TrainingSessionEntity?

    // ✅ 获取最近完成的训练会话
    @Query("SELECT * FROM training_sessions WHERE status = 1 ORDER BY endTime DESC LIMIT 1")
    suspend fun getLastCompletedSession(): TrainingSessionEntity?

    // ✅ 获取训练会话完整数据
    @Transaction
    @Query("SELECT * FROM training_sessions WHERE id = :sessionId")
    suspend fun getSessionWithDetails(sessionId: Long): TrainingSessionWithDetails?
}