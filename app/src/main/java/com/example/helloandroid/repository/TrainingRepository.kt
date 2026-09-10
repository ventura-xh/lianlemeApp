package com.example.helloandroid.repository

import com.example.helloandroid.dao.PlanFullDao
import com.example.helloandroid.dao.TrainingSessionActionDao
import com.example.helloandroid.dao.TrainingSessionActionDetailDao
import com.example.helloandroid.dao.TrainingSessionDao
import com.example.helloandroid.entity.TrainingSessionActionDetailEntity
import com.example.helloandroid.entity.TrainingSessionActionEntity
import com.example.helloandroid.entity.TrainingSessionEntity
import com.example.helloandroid.entity.model.TrainingAction
import com.example.helloandroid.entity.model.TrainingGroup
import com.example.helloandroid.entity.model.TrainingSession
import com.example.helloandroid.entity.model.TrainingSessionWithDetails
import kotlinx.coroutines.flow.Flow

class TrainingRepository(
    private val sessionDao: TrainingSessionDao,
    private val actionDao: TrainingSessionActionDao,
    private val detailDao: TrainingSessionActionDetailDao,
    private val planRepository: PlanRepository,
) {

    // ============================================================
    // 1. 加载计划数据到内存
    // ============================================================

    /**
     * 从计划加载数据，构建内存中的 TrainingSession
     */
    suspend fun loadSessionFromPlan(planId: Long, planName: String): TrainingSession {
        // 获取计划详情
        val planDetail = planRepository.getPlanDetail(planId)

        // ✅ 如果 planName 为空，从 planDetail 中获取
        val actualPlanName = if (planName.isNotEmpty()) {
            planName
        } else {
            planDetail?.plan?.name ?: "训练计划"
        }

        val session = TrainingSession(
            planId = planId,
            planName = actualPlanName  // ✅ 使用实际的名称
        )

        planDetail?.actions?.forEach { actionWithDetails ->
            val groups = actionWithDetails.groups.mapIndexed { index, detail ->
                TrainingGroup(
                    groupIndex = index,
                    weight = detail.weight,
                    reps = detail.reps.toInt(),
                    isCompleted = false
                )
            }.toMutableList()

            session.actions.add(
                TrainingAction(
                    actionId = actionWithDetails.planAction.actionId,
                    actionName = actionWithDetails.action.name,
                    groups = groups
                )
            )
        }

        return session
    }

    // ============================================================
    // 2. 保存训练结果（结束时一次性保存）
    // ============================================================

    /**
     * 保存完整的训练会话到数据库（结束时调用）
     */
    suspend fun saveSession(session: TrainingSession): Long {
        // 1. 插入训练会话
        val sessionId = sessionDao.insert(
            TrainingSessionEntity(
                planId = session.planId,
                planName = session.planName,
                startTime = session.startTime,
                endTime = session.endTime,
                totalDuration = if (session.endTime > 0) {
                    (session.endTime - session.startTime) / 1000
                } else 0,
                status = session.status
            )
        )

        // 2. 插入动作
        session.actions.forEachIndexed { sortOrder, action ->
            val actionId = actionDao.insert(
                TrainingSessionActionEntity(
                    sessionId = sessionId,
                    actionId = action.actionId,
                    actionName = action.actionName,
                    sortOrder = sortOrder,
                    isCompleted = action.isCompleted
                )
            )

            // 3. 插入组详情
            action.groups.forEach { group ->
                detailDao.insert(
                    TrainingSessionActionDetailEntity(
                        sessionActionId = actionId,
                        groupIndex = group.groupIndex,
                        weight = group.weight,
                        reps = group.reps,
                        isCompleted = group.isCompleted,
                        completedAt = if (group.isCompleted) System.currentTimeMillis() else 0
                    )
                )
            }
        }

        return sessionId
    }

    // ============================================================
    // 3. 查询历史记录
    // ============================================================

    /**
     * 获取所有训练历史
     */
    fun getAllSessions(): Flow<List<TrainingSessionEntity>> {
        return sessionDao.getAllSessions()
    }

    /**
     * 获取所有训练历史（一次性查询）
     */
    suspend fun getAllSessionsOnce(): List<TrainingSessionEntity> {
        return sessionDao.getAllSessionsOnce()
    }

    /**
     * 获取训练会话详情（用于历史查看）
     */
    suspend fun getSessionWithDetails(sessionId: Long): TrainingSessionWithDetails? {
        return sessionDao.getSessionWithDetails(sessionId)
    }

    /**
     * 获取最近完成的训练会话
     */
    suspend fun getLastCompletedSession(): TrainingSessionEntity? {
        return sessionDao.getLastCompletedSession()
    }

    /**
     * 获取进行中的训练会话
     */
    suspend fun getActiveSession(): TrainingSessionEntity? {
        return sessionDao.getActiveSession()
    }

    /**
     * 获取动作关联的肌肉名称列表
     * 通过 ActionLibDAO 和 MuscleDao 查询
     */
    suspend fun getMuscleNamesForAction(actionId: Long): List<String> {
        return try {
            // 通过 PlanRepository 的 actionLibRepository 获取
            planRepository.getMuscleNamesForAction(actionId)
        } catch (e: Exception) {
            emptyList()
        }
    }
}