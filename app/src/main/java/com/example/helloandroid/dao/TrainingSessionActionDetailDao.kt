package com.example.helloandroid.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.helloandroid.entity.TrainingSessionActionDetailEntity

@Dao
interface TrainingSessionActionDetailDao {

    // ========== 增 ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detail: TrainingSessionActionDetailEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(details: List<TrainingSessionActionDetailEntity>): List<Long>

    // ========== 删 ==========
    @Query("DELETE FROM training_session_action_details WHERE id = :detailId")
    suspend fun deleteById(detailId: Long)

    @Query("DELETE FROM training_session_action_details WHERE sessionActionId = :sessionActionId")
    suspend fun deleteBySessionActionId(sessionActionId: Long)

    @Query("DELETE FROM training_session_action_details WHERE sessionActionId IN (SELECT id FROM training_session_actions WHERE sessionId = :sessionId)")
    suspend fun deleteBySessionId(sessionId: Long)

    // ========== 改 ==========
    @Update
    suspend fun update(detail: TrainingSessionActionDetailEntity)

    @Query("UPDATE training_session_action_details SET isCompleted = 1, completedAt = :completedAt WHERE id = :detailId")
    suspend fun markDetailCompleted(detailId: Long, completedAt: Long)

    @Query("UPDATE training_session_action_details SET weight = :weight, reps = :reps WHERE id = :detailId")
    suspend fun updateWeightAndReps(detailId: Long, weight: Double, reps: Int)

    // ========== 查 ==========
    @Query("SELECT * FROM training_session_action_details WHERE id = :detailId")
    suspend fun getDetailById(detailId: Long): TrainingSessionActionDetailEntity?

    @Query("SELECT * FROM training_session_action_details WHERE sessionActionId = :sessionActionId ORDER BY groupIndex ASC")
    suspend fun getDetailsByActionId(sessionActionId: Long): List<TrainingSessionActionDetailEntity>

    @Query("SELECT * FROM training_session_action_details WHERE sessionActionId = :sessionActionId AND isCompleted = 0 ORDER BY groupIndex ASC LIMIT 1")
    suspend fun getCurrentDetail(sessionActionId: Long): TrainingSessionActionDetailEntity?

    @Query("SELECT COUNT(*) FROM training_session_action_details WHERE sessionActionId = :sessionActionId AND isCompleted = 1")
    suspend fun getCompletedDetailCount(sessionActionId: Long): Int

    @Query("SELECT COUNT(*) FROM training_session_action_details WHERE sessionActionId = :sessionActionId")
    suspend fun getTotalDetailCount(sessionActionId: Long): Int
}