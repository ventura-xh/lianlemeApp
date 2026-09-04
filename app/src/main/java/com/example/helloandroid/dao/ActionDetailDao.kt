package com.example.helloandroid.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.helloandroid.entity.ActionDetailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionDetailDao {

    // ========== 增 ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detail: ActionDetailEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(details: List<ActionDetailEntity>)

    // ========== 删 ==========
    @Delete
    suspend fun delete(detail: ActionDetailEntity)

    @Query("DELETE FROM action_detail WHERE id = :detailId")
    suspend fun deleteById(detailId: Long)

    @Query("DELETE FROM action_detail WHERE planActionId = :planActionId")
    suspend fun deleteByPlanActionId(planActionId: Long)  // 删除某个动作的所有组

    @Query("DELETE FROM action_detail")
    suspend fun deleteAll()

    // ========== 改 ==========
    @Update
    suspend fun update(detail: ActionDetailEntity)

    // 批量更新（比如一次修改多组数据）
    @Update
    suspend fun updateAll(details: List<ActionDetailEntity>)

    // ========== 查 ==========
    // 获取某个计划动作的所有组详情（实时监听）
    @Query("""
        SELECT * FROM action_detail 
        WHERE planActionId = :planActionId 
        ORDER BY groupIndex ASC
    """)
    fun getDetailsByPlanActionId(planActionId: Long): Flow<List<ActionDetailEntity>>

    // 获取某个计划动作的所有组详情（一次性）
    @Query("""
        SELECT * FROM action_detail 
        WHERE planActionId = :planActionId 
        ORDER BY groupIndex ASC
    """)
    suspend fun getDetailsByPlanActionIdOnce(planActionId: Long): List<ActionDetailEntity>

    // 根据 id 查询
    @Query("SELECT * FROM action_detail WHERE id = :detailId")
    suspend fun getDetailById(detailId: Long): ActionDetailEntity?

    // 获取某个计划动作的最大组号
    @Query("SELECT MAX(groupIndex) FROM action_detail WHERE planActionId = :planActionId")
    suspend fun getMaxGroupIndex(planActionId: Long): Long?

    // 获取某个计划下所有动作的详情（用于展示完整计划）
    @Query("""
        SELECT action_detail.* 
        FROM action_detail
        INNER JOIN plan_actions ON action_detail.planActionId = plan_actions.id
        WHERE plan_actions.planId = :planId
        ORDER BY plan_actions.sortOrder ASC, action_detail.groupIndex ASC
    """)
    suspend fun getDetailsByPlanId(planId: Long): List<ActionDetailEntity>
}