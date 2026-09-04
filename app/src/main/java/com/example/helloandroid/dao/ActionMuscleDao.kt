package com.example.helloandroid.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.helloandroid.entity.ActionMuscleEntity

@Dao
interface ActionMuscleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(actionMuscle: ActionMuscleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(actionMuscles: List<ActionMuscleEntity>)

    @Query("DELETE FROM action_muscles WHERE actionId = :actionId")
    suspend fun deleteByActionId(actionId: Long)

    @Query("SELECT muscleId FROM action_muscles WHERE actionId = :actionId")
    suspend fun getMuscleIdsByActionId(actionId: Long): List<Long>

    @Query("SELECT actionId FROM action_muscles WHERE muscleId = :muscleId")
    suspend fun getActionIdsByMuscleId(muscleId: Long): List<Long>

    @Query("DELETE FROM action_muscles")
    suspend fun deleteAll()

    // ✅ 根据动作ID和肌肉ID列表删除关联
    @Query("DELETE FROM action_muscles WHERE actionId = :actionId AND muscleId IN (:muscleIds)")
    suspend fun deleteByActionAndMuscleIds(actionId: Long, muscleIds: List<Long>)

    // ✅ 删除预设动作的关联（用于首次导入清理）
    @Query("""
        DELETE FROM action_muscles 
        WHERE actionId IN (SELECT id FROM action_lib WHERE isPreset = 1)
    """)
    suspend fun deleteByPresetActions()
}