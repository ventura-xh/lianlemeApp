package com.example.helloandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.helloandroid.FitApplication
import com.example.helloandroid.entity.ActionDetailEntity
import com.example.helloandroid.entity.PlanActionsEntity
import com.example.helloandroid.repository.ActionLibRepository
import com.example.helloandroid.repository.PlanRepository
import com.example.helloandroid.entity.model.ActionGroup
import com.example.helloandroid.entity.model.PlanAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.plus

class CreatePlanViewModel(
    private val planRepository: PlanRepository,
    private val actionLibRepository: ActionLibRepository
) : ViewModel() {
    // ✅ 已选动作列表（包含组信息）
    private val _selectedActions = MutableStateFlow<List<PlanAction>>(emptyList())
    val selectedActions: StateFlow<List<PlanAction>> = _selectedActions.asStateFlow()


    // ✅ 添加 _refreshTrigger
    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    // 添加动作
    suspend fun addAction(actionId: Long) {
        // 检查是否已存在
        if (_selectedActions.value.any { it.actionId == actionId }) return

        val action = actionLibRepository.getActionById(actionId)
        val actionName = action?.name ?: "未知动作"

        _selectedActions.update { current ->
            current + PlanAction(
                actionId = actionId,
                actionName = actionName,
                groups = mutableListOf(ActionGroup())
            )
        }
    }

    // ✅ 移动动作顺序
    fun moveAction(from: Int, to: Int) {
        if (from == to) return
        if (from !in 0 until _selectedActions.value.size) return
        if (to !in 0 until _selectedActions.value.size) return

        val currentList = _selectedActions.value.toMutableList()
        val item = currentList.removeAt(from)
        currentList.add(to, item)
        _selectedActions.value = currentList
        _refreshTrigger.value++
    }

    // ✅ 移除动作
    fun removeAction(actionId: Long) {
        _selectedActions.update { current ->
            current.filter { it.actionId != actionId }
        }
    }

    // ✅ 为动作新增一组
    fun addGroup(actionId: Long) {
        _selectedActions.update { current ->
            current.map { action ->
                if (action.actionId == actionId) {
                    action.copy(
                        groups = (action.groups + ActionGroup()).toMutableList()
                    )
                } else {
                    action
                }
            }
        }
    }

    // ✅ 移除动作的某一组
    fun removeGroup(actionId: Long, groupId: String) {
        _selectedActions.update { current ->
            current.map { action ->
                if (action.actionId == actionId && action.groups.size > 1) {
                    action.copy(
                        groups = action.groups.filter { it.id != groupId }.toMutableList()
                    )
                } else {
                    action
                }
            }
        }
    }

    // ✅ 更新组的重量
    fun updateGroupWeight(actionId: Long, groupId: String, weight: String) {
        _selectedActions.update { current ->
            current.map { action ->
                if (action.actionId == actionId) {
                    val updatedGroups = action.groups.map { group ->
                        if (group.id == groupId) {
                            group.copy(weight = weight)
                        } else {
                            group
                        }
                    }.toMutableList()
                    action.copy(groups = updatedGroups)
                } else {
                    action
                }
            }
        }
    }

    // ✅ 更新组的次数
    fun updateGroupReps(actionId: Long, groupId: String, reps: String) {
        _selectedActions.update { current ->
            current.map { action ->
                if (action.actionId == actionId) {
                    val updatedGroups = action.groups.map { group ->
                        if (group.id == groupId) {
                            group.copy(reps = reps)
                        } else {
                            group
                        }
                    }.toMutableList()
                    action.copy(groups = updatedGroups)
                } else {
                    action
                }
            }
        }
    }

    // ✅ 清空所有动作
    fun clearActions() {
        _selectedActions.value = emptyList()
    }

    // ✅ 保存计划
    suspend fun savePlan(planName: String): Result<Long> {
        return try {
            val actions = _selectedActions.value

            if (actions.isEmpty()) {
                return Result.failure(IllegalStateException("请至少添加一个动作"))
            }

            val finalName = planName.ifBlank { "未命名计划" }

            // 1.插入计划
            val planId = planRepository.insertPlan(finalName)

            // 2.插入每个动作及其组详情
            actions.forEachIndexed { index, planAction ->
                // 插入 plan_action
                val planActionId = planRepository.insertPlanAction(
                    PlanActionsEntity(
                        planId = planId,
                        actionId = planAction.actionId,
                        sortOrder = index
                    )
                )

                // 3.插入该动作的所有组详情
                planAction.groups.forEachIndexed { groupIndex, group ->
                    val weight = group.weight.toDoubleOrNull() ?: 0.0
                    val reps = group.reps.toLongOrNull() ?: 0L

                    planRepository.insertActionDetail(
                        ActionDetailEntity(
                            planActionId = planActionId,
                            groupIndex = groupIndex.toLong(),
                            weight = weight,
                            reps = reps,
                            weightUnit = "kg",
                            restSeconds = 60,
                            isLeftRight = false
                        )
                    )
                }
            }
            // 保存成功后清空状态
            _selectedActions.value = emptyList()

            Result.success(planId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CreatePlanViewModel(
                    PlanRepository(
                        FitApplication.instance.database.plansDao(),
                        FitApplication.instance.database.planActionsDao(),
                        FitApplication.instance.database.actionDetailsDao(),
                        FitApplication.instance.database.planFullDao(),
                        FitApplication.instance.database.actionLibDAO(),
                        FitApplication.instance.database.muscleDao()
                    ),
                    ActionLibRepository(
                        FitApplication.instance.database.actionLibDAO(),
                        FitApplication.instance.database.muscleDao(),
                        FitApplication.instance.database.actionMuscleDao()
                    )
                ) as T
            }
        }
    }

}