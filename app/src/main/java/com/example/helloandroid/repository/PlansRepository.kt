package com.example.helloandroid.repository

import androidx.room.Transaction
import com.example.helloandroid.dao.ActionDetailDao
import com.example.helloandroid.dao.PlanActionsDao
import com.example.helloandroid.dao.PlanFullDao
import com.example.helloandroid.dao.PlansDao
import com.example.helloandroid.entity.ActionDetailEntity
import com.example.helloandroid.entity.PlanActionsEntity
import com.example.helloandroid.entity.PlansEntity
import com.example.helloandroid.entity.model.ActionDetailInput
import com.example.helloandroid.entity.model.PlanActionWithDetails
import com.example.helloandroid.entity.model.PlanActionWithDetailsInput
import com.example.helloandroid.entity.model.PlanWithActionsAndDetails
import kotlinx.coroutines.flow.Flow

class PlansRepository (
    private val plansDao: PlansDao,
    private val planActionsDao: PlanActionsDao,
    private val actionDetailDao: ActionDetailDao,
    private val planFullDao: PlanFullDao
) {
    // 获取所有计划
    fun getAllPlans(): Flow<List<PlansEntity>> = plansDao.getAllPlans()

    // 获取计划详情
    suspend fun getPlanWithActions(planId: Long): PlanWithActionsAndDetails? {
        return planFullDao.getPlanWithActions(planId)
    }

    /**
     * 创建一个新计划
     * @param planName 计划名称
     * @param actions 包含的动作及训练参数列表
     * @return 新计划的 ID
     */
    suspend fun createPlan(
        planName: String,
        actions: List<PlanActionWithDetailsInput>
    ): Long {
        // 1. 插入计划
        val planId = plansDao.insert(PlansEntity(name = planName))

        // 2. 插入每个动作及其详情
        actions.forEach { input ->
            // 插入 plan_action
            val planActionId = planActionsDao.insert(
                PlanActionsEntity(
                    planId = planId,
                    actionId = input.actionId,
                    sortOrder = input.sortOrder
                )
            )

            // 插入该动作的所有组详情
            input.details.forEach { detailInput ->
                actionDetailDao.insert(
                    ActionDetailEntity(
                        planActionId = planActionId,
                        groupIndex = detailInput.groupIndex,
                        weight = detailInput.weight,
                        reps = detailInput.reps,
                        weightUnit = detailInput.weightUnit,
                        restSeconds = detailInput.restSeconds,
                        isLeftRight = detailInput.isLeftRight
                    )
                )
            }
        }

        return planId
    }

    @Transaction
    suspend fun copyPlan(sourcePlanId: Long, newPlanName: String = ""): Long {
        // 1. 获取源计划数据
        val source = planFullDao.getPlanWithActions(sourcePlanId) ?: return -1

        // 2. 确定新计划名称
        val finalName = if (newPlanName.isBlank()) {
            "${source.plan.name} (副本)"
        } else {
            newPlanName
        }

        // 3. 创建新计划
        val newPlanId = plansDao.insert(PlansEntity(name = finalName))

        // 4. 复制计划中的每个动作
        source.planActions.forEach { item ->
            val newPaId = planActionsDao.insert(
                PlanActionsEntity(
                    planId = newPlanId,
                    actionId = item.planAction.actionId,
                    sortOrder = item.planAction.sortOrder
                )
            )

            // 5. 复制该动作的所有组详情
            item.details.forEach { detail ->
                actionDetailDao.insert(
                    detail.copy(
                        id = 0,
                        planActionId = newPaId
                    )
                )
            }
        }

        return newPlanId
    }

    // 删除计划
    suspend fun deletePlan(plan: PlansEntity) {
        plansDao.delete(plan)  // CASCADE 自动删除关联数据
    }

    // 更新计划名称
    suspend fun updatePlanName(planId: Long, newName: String) {
        val plan = plansDao.getPlanById(planId) ?: return
        plansDao.update(plan.copy(name = newName))
    }

    // 将 PlanActionWithDetails 转为 PlanActionWithDetailsInput
    fun PlanActionWithDetails.toInput(): PlanActionWithDetailsInput {
        return PlanActionWithDetailsInput(
            actionId = this.planAction.actionId,
            sortOrder = this.planAction.sortOrder,
            details = this.details.map { detail ->
                ActionDetailInput(
                    groupIndex = detail.groupIndex,
                    weight = detail.weight,
                    reps = detail.reps,
                    weightUnit = detail.weightUnit,
                    restSeconds = detail.restSeconds,
                    isLeftRight = detail.isLeftRight
                )
            }
        )
    }
}