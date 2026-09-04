package com.example.helloandroid.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.helloandroid.entity.PlanActionsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanActionsDao {

    // ========== 增 ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(planAction: PlanActionsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(planActions: List<PlanActionsEntity>)

    // ========== 删 ==========
    @Delete
    suspend fun delete(planAction: PlanActionsEntity)

    @Query("DELETE FROM plan_actions WHERE id = :planActionId")
    suspend fun deleteById(planActionId: Long)

    @Query("DELETE FROM plan_actions WHERE planId = :planId")
    suspend fun deleteByPlanId(planId: Long)  // 删除计划下所有关联

    @Query("DELETE FROM plan_actions")
    suspend fun deleteAll()

    // ========== 改 ==========
    @Update
    suspend fun update(planAction: PlanActionsEntity)

    // 更新排序顺序
    @Query("UPDATE plan_actions SET sortOrder = :sortOrder WHERE id = :planActionId")
    suspend fun updateSortOrder(planActionId: Long, sortOrder: Int)

    // ========== 查 ==========
    // 获取某个计划下的所有关联动作（实时监听）
    @Query("""
        SELECT * FROM plan_actions 
        WHERE planId = :planId 
        ORDER BY sortOrder ASC
    """)
    fun getPlanActionsByPlanId(planId: Long): Flow<List<PlanActionsEntity>>

    // 获取某个计划下的所有关联动作（一次性）
    @Query("""
        SELECT * FROM plan_actions 
        WHERE planId = :planId 
        ORDER BY sortOrder ASC
    """)
    suspend fun getPlanActionsByPlanIdOnce(planId: Long): List<PlanActionsEntity>

    // 根据 id 查询
    @Query("SELECT * FROM plan_actions WHERE id = :planActionId")
    suspend fun getPlanActionById(planActionId: Long): PlanActionsEntity?

    // 检查某个动作是否已被计划引用
    @Query("SELECT COUNT(*) FROM plan_actions WHERE actionId = :actionId")
    suspend fun getActionUsageCount(actionId: Long): Int

    // 检查某个动作是否在指定计划中
    @Query("SELECT COUNT(*) FROM plan_actions WHERE planId = :planId AND actionId = :actionId")
    suspend fun isActionInPlan(planId: Long, actionId: Long): Int

    // 获取计划中动作的最大 sortOrder（用于追加时计算新顺序）
    @Query("SELECT MAX(sortOrder) FROM plan_actions WHERE planId = :planId")
    suspend fun getMaxSortOrder(planId: Long): Int?
}