package com.example.helloandroid.repository

import androidx.room.Transaction
import com.example.helloandroid.dao.PlanFullDao
import com.example.helloandroid.dao.TrainingSessionActionDao
import com.example.helloandroid.dao.TrainingSessionActionDetailDao
import com.example.helloandroid.dao.TrainingSessionDao
import com.example.helloandroid.entity.ActionLibEntity
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
    // 2. 创建训练会话（开始时调用，status = 0）
    // ============================================================

    /**
     * 创建训练会话（训练开始时调用）
     * @return 数据库生成的 sessionId
     */
    suspend fun createSession(session: TrainingSession): Long {
        // ✅ 1. 插入训练会话（status = 0）
        val sessionId = sessionDao.insert(
            TrainingSessionEntity(
                planId = session.planId,
                planName = session.planName,
                startTime = session.startTime,
                endTime = 0,
                totalDuration = 0,
                status = 0  // ✅ 进行中
            )
        )

        // ✅ 2. 插入动作和组详情
        saveSessionActions(sessionId, session.actions)

        return sessionId
    }

    /**
     * 保存训练动作和组详情（内部方法）
     */
    private suspend fun saveSessionActions(
        sessionId: Long,
        actions: List<TrainingAction>
    ) {
        actions.forEachIndexed { sortOrder, action ->
            val actionId = actionDao.insert(
                TrainingSessionActionEntity(
                    sessionId = sessionId,
                    actionId = action.actionId,
                    actionName = action.actionName,
                    sortOrder = sortOrder,
                    isCompleted = action.isCompleted
                )
            )

            action.groups.forEach { group ->
                detailDao.insert(
                    TrainingSessionActionDetailEntity(
                        sessionActionId = actionId,
                        groupIndex = group.groupIndex,
                        weight = group.weight,
                        reps = group.reps,
                        isCompleted = group.isCompleted,
                        completedAt = group.completedAt
                    )
                )
            }
        }
    }

    // ============================================================
    // 3. 更新训练状态（完成/取消时调用）
    // ============================================================
    /**
     * 保存训练会话的完整数据（完成时调用）
     * 更新动作和组的完成状态、重量、次数
     */
    suspend fun saveSessionDetails(
        sessionId: Long,
        actions: List<TrainingAction>
    ) {
        // ✅ 1. 获取数据库中的动作列表
        val existingActions = actionDao.getActionsBySessionId(sessionId)
        val actionMap = existingActions.associateBy { it.actionId }

        actions.forEachIndexed { sortOrder, action ->
            val existingAction = actionMap[action.actionId]

            if (existingAction != null) {
                // ✅ 2. 更新动作完成状态和排序
                actionDao.update(
                    existingAction.copy(
                        sortOrder = sortOrder,
                        isCompleted = action.isCompleted
                    )
                )

                // ✅ 3. 获取该动作的所有组详情
                val existingDetails = detailDao.getDetailsByActionId(existingAction.id)
                val detailMap = existingDetails.associateBy { it.groupIndex.toInt() }

                action.groups.forEachIndexed { groupIndex, group ->
                    val existingDetail = detailMap[groupIndex]

                    if (existingDetail != null) {
                        // ✅ 4. 更新组的完成状态、重量、次数
                        detailDao.update(
                            existingDetail.copy(
                                weight = group.weight,
                                reps = group.reps,
                                isCompleted = group.isCompleted,
                                completedAt = group.completedAt
                            )
                        )
                    } else {
                        // ✅ 5. 新增的组，插入数据库
                        detailDao.insert(
                            TrainingSessionActionDetailEntity(
                                sessionActionId = existingAction.id,
                                groupIndex = groupIndex,
                                weight = group.weight,
                                reps = group.reps,
                                isCompleted = group.isCompleted,
                                completedAt = group.completedAt
                            )
                        )
                    }
                }

                // ✅ 6. 删除多余的组（如果用户删除了组）
                val validGroupIndices = action.groups.indices.map { it }
                existingDetails.forEach { detail ->
                    if (detail.groupIndex !in validGroupIndices) {
                        detailDao.deleteById(detail.id)
                    }
                }
            } else {
                // ✅ 7. 新增的动作，插入数据库
                val sessionActionId = actionDao.insert(
                    TrainingSessionActionEntity(
                        sessionId = sessionId,
                        actionId = action.actionId,
                        actionName = action.actionName,
                        sortOrder = sortOrder,
                        isCompleted = action.isCompleted
                    )
                )

                action.groups.forEach { group ->
                    detailDao.insert(
                        TrainingSessionActionDetailEntity(
                            sessionActionId = sessionActionId,
                            groupIndex = group.groupIndex,
                            weight = group.weight,
                            reps = group.reps,
                            isCompleted = group.isCompleted,
                            completedAt = group.completedAt
                        )
                    )
                }
            }
        }

        // ✅ 8. 删除不再存在的动作（用户移除的动作）
        val validActionIds = actions.map { it.actionId }
        existingActions.forEach { existing ->
            if (existing.actionId !in validActionIds) {
                actionDao.deleteById(existing.id)
            }
        }
    }

    /**
     * 完成训练（status = 1）
     */
    suspend fun finishSession(sessionId: Long) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        val endTime = System.currentTimeMillis()
        val duration = (endTime - session.startTime) / 1000

        sessionDao.update(
            session.copy(
                endTime = endTime,
                totalDuration = duration,
                status = 1  // ✅ 已完成
            )
        )
    }

    /**
     * 取消训练（status = 2）
     */
    suspend fun cancelSession(sessionId: Long) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        val endTime = System.currentTimeMillis()
        val duration = (endTime - session.startTime) / 1000

        sessionDao.update(
            session.copy(
                endTime = endTime,
                totalDuration = duration,
                status = 2  // ✅ 已取消
            )
        )
    }

    @Transaction
    suspend fun finishSessionWithDetails(
        sessionId: Long,
        actions: List<TrainingAction>
    ) {
        // ✅ 1. 保存最新数据
        saveSessionDetails(sessionId, actions)

        // ✅ 2. 更新状态
        finishSession(sessionId)
    }

    @Transaction
    suspend fun CancelSessionWithDetails(
        sessionId: Long,
        actions: List<TrainingAction>
    ) {
        // ✅ 1. 保存最新数据
        saveSessionDetails(sessionId, actions)

        // ✅ 2. 更新状态
        cancelSession(sessionId)
    }

    // ============================================================
    // 4. 更新组完成状态（实时保存进度，可选）
    // ============================================================

    /**
     * 更新组完成状态
     */
    suspend fun updateGroupCompleted(
        sessionActionId: Long,
        groupIndex: Int,
        isCompleted: Boolean,
        weight: Double,
        reps: Int
    ) {
        detailDao.updateGroupByIndex(
            sessionActionId = sessionActionId,
            groupIndex = groupIndex,
            isCompleted = isCompleted,
            weight = weight,
            reps = reps
        )
    }

    /**
     * 用训练会话的数据更新计划
     * 将训练中的动作和组数据同步到计划模板
     */
    @Transaction
    suspend fun syncPlanFromSession(
        planId: Long,
        actions: List<TrainingAction>
    ) {
        // ✅ 直接使用已有的 planRepository
        planRepository.updatePlanFromSession(planId, actions)
    }

    // ============================================================
    // 5. 查询
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

    /**
     * 根据 ID 获取动作信息
     */
    suspend fun getActionById(actionId: Long): ActionLibEntity? {
        return try {
            planRepository.getActionById(actionId)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 清理超过24小时未完成的训练（避免脏数据）
     */
    suspend fun cleanupStaleSessions() {
        val threshold = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        sessionDao.deleteStaleActiveSessions(threshold)
    }

}