package com.example.helloandroid.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.helloandroid.entity.ActionLibEntity
import com.example.helloandroid.entity.model.PlanActionWithDetails
import com.example.helloandroid.entity.model.PlanWithActionsAndDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanFullDao {
    // ✅ 只需要查询 plan_actions，Room 自动关联 actions 和 action_details
    @Transaction
    @Query("SELECT * FROM plan_actions WHERE planId = :planId ORDER BY sortOrder ASC")
    suspend fun getPlanActionsWithDetails(planId: Long): List<PlanActionWithDetails>

    @Transaction
    @Query("SELECT * FROM plans WHERE id = :planId")
    suspend fun getPlanWithActions(planId: Long): PlanWithActionsAndDetails?

    @Transaction
    @Query("SELECT * FROM plans")
    fun getAllPlansWithActions(): Flow<List<PlanWithActionsAndDetails>>

    // ✅ 根据 ID 获取动作信息
    @Query("SELECT * FROM action_lib WHERE id = :actionId")
    suspend fun getActionById(actionId: Long): ActionLibEntity?
}