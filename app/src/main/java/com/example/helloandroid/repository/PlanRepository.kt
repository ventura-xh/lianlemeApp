package com.example.helloandroid.repository

// repository/PlanRepository.kt

import com.example.helloandroid.dao.ActionDetailDao
import com.example.helloandroid.dao.ActionLibDAO
import com.example.helloandroid.dao.MuscleDao
import com.example.helloandroid.dao.PlanActionsDao
import com.example.helloandroid.dao.PlanFullDao
import com.example.helloandroid.dao.PlansDao
import com.example.helloandroid.entity.ActionDetailEntity
import com.example.helloandroid.entity.PlanActionsEntity
import com.example.helloandroid.entity.PlansEntity
import com.example.helloandroid.entity.model.PlanWithActionsAndDetails
import com.example.helloandroid.ui.plan.PlanActionWithGroups
import com.example.helloandroid.ui.plan.PlanDetail
import kotlinx.coroutines.flow.Flow

class PlanRepository(
    private val plansDao: PlansDao,
    private val planActionsDao: PlanActionsDao,
    private val actionDetailsDao: ActionDetailDao,
    private val planFullDao: PlanFullDao,
    private val actionLibDAO: ActionLibDAO,  // ✅ 新增
    private val muscleDao: MuscleDao          // ✅ 新增
) {

    // ========== 插入 ==========
    suspend fun insertPlan(name: String): Long {
        return plansDao.insert(PlansEntity(name = name))
    }

    suspend fun insertPlanAction(planAction: PlanActionsEntity): Long {
        return planActionsDao.insert(planAction)
    }

    suspend fun insertActionDetail(detail: ActionDetailEntity): Long {
        return actionDetailsDao.insert(detail)
    }

    // ========== 查询 ==========
    fun getAllPlans(): Flow<List<PlansEntity>> {
        return plansDao.getAllPlans()
    }

    suspend fun getPlanWithActions(planId: Long): PlanWithActionsAndDetails? {
        return planFullDao.getPlanWithActions(planId)
    }

    // ✅ 根据 ID 获取单个计划
    suspend fun getPlanById(planId: Long): PlansEntity? {
        return plansDao.getPlanById(planId)
    }

    // ✅ 获取所有计划（一次性）
    suspend fun getAllPlansOnce(): List<PlansEntity> {
        return plansDao.getAllPlansOnce()
    }

    // ✅ 获取计划的所有动作（含动作信息和组详情）
    suspend fun getPlanActionsWithDetails(planId: Long): List<PlanActionWithGroups> {
        // 1. 获取该计划的所有 plan_actions
        val planActions = planActionsDao.getPlanActionsByPlanIdOnce(planId)

        if (planActions.isEmpty()) {
            return emptyList()
        }

        // 2. 遍历每个 plan_action，获取对应的动作信息和组详情
        val result = mutableListOf<PlanActionWithGroups>()

        for (planAction in planActions) {
            // 2.1 获取动作信息
            val action = planFullDao.getActionById(planAction.actionId)
            if (action == null) continue

            // 2.2 获取该动作的所有组详情
            val details = actionDetailsDao.getDetailsByPlanActionIdOnce(planAction.id)

            result.add(
                PlanActionWithGroups(
                    planAction = planAction,
                    action = action,
                    groups = details
                )
            )
        }

        // 按 sortOrder 排序
        return result.sortedBy { it.planAction.sortOrder }
    }

    /**
     * 获取计划详情（含所有动作和组详情）
     */
    suspend fun getPlanDetail(planId: Long): PlanDetail? {
        // 1. 获取计划基本信息
        val plan = getPlanById(planId) ?: return null

        // 2. 获取计划的所有动作（含动作信息和组详情）
        val actions = getPlanActionsWithDetails(planId)

        // 3. 返回组合结果
        return PlanDetail(
            plan = plan,
            actions = actions
        )
    }

    /**
     * 获取动作关联的肌肉名称列表
     */
    suspend fun getMuscleNamesForAction(actionId: Long): List<String> {
        return try {
            val muscleIds = actionLibDAO.getMuscleIdsForAction(actionId)
            if (muscleIds.isEmpty()) return emptyList()
            val muscles = muscleDao.getMusclesByIds(muscleIds)
            muscles.map { it.name }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ========== 更新 ==========
    suspend fun updatePlanName(planId: Long, newName: String) {
        val plan = plansDao.getPlanById(planId)
        plan?.let {
            plansDao.update(it.copy(name = newName))
        }
    }

    suspend fun deletePlanActionsByPlanId(planId: Long) {
        planActionsDao.deleteByPlanId(planId)
    }

    // ========== 删除 ==========
    suspend fun deletePlan(plan: PlansEntity) {
        plansDao.delete(plan)  // CASCADE 自动删除关联数据
    }

    suspend fun deletePlanById(planId: Long) {
        plansDao.deleteById(planId)
    }



}