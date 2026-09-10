package com.example.helloandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch
import kotlin.collections.plus

class EditPlanViewModel(
    private val planId: Long,
    private val planRepository: PlanRepository,
    private val actionLibRepository: ActionLibRepository
) : ViewModel() {

    private val _selectedActions = MutableStateFlow<List<PlanAction>>(emptyList())
    val selectedActions: StateFlow<List<PlanAction>> = _selectedActions.asStateFlow()

    private val _planName = MutableStateFlow("")
    val planName: StateFlow<String> = _planName.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ✅ 添加 _refreshTrigger
    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    init {
        loadPlanData()
    }

    private fun loadPlanData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val planDetail = planRepository.getPlanDetail(planId)
                if (planDetail != null) {
                    _planName.value = planDetail.plan.name
                    val actions = planDetail.actions.map { item ->
                        PlanAction(
                            actionId = item.planAction.actionId,
                            actionName = item.action.name,
                            groups = item.groups.map { detail ->
                                ActionGroup(
                                    id = detail.id.toString(),
                                    weight = detail.weight.toString(),
                                    reps = detail.reps.toString()
                                )
                            }.toMutableList()
                        )
                    }
                    _selectedActions.value = actions
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isLoading.value = false
        }
    }

    // ✅ 添加动作
    suspend fun addAction(actionId: Long) {
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

    // ✅ 保存计划（更新）
    suspend fun savePlan(planName: String): Result<Long> {
        return try {
            val actions = _selectedActions.value

            if (actions.isEmpty()) {
                return Result.failure(IllegalStateException("请至少添加一个动作"))
            }

            // 1. 删除旧的 plan_actions 和 action_details（外键 CASCADE 自动删除）
            // 需要先删除旧的 plan_actions，这里通过 Repository 处理

            // 2. 重新插入所有数据
            val finalName = planName.ifBlank { "未命名计划" }

            // 更新计划名称
            planRepository.updatePlanName(planId, finalName)

            // 删除旧的关联数据
            planRepository.deletePlanActionsByPlanId(planId)

            // 重新插入
            actions.forEachIndexed { index, planAction ->
                val planActionId = planRepository.insertPlanAction(
                    PlanActionsEntity(
                        planId = planId,
                        actionId = planAction.actionId,
                        sortOrder = index
                    )
                )

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

            _selectedActions.value = emptyList()
            _planName.value = ""

            Result.success(planId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        fun factory(planId: Long): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val database = FitApplication.instance.database
                    return EditPlanViewModel(
                        planId = planId,
                        planRepository = PlanRepository(
                            database.plansDao(),
                            database.planActionsDao(),
                            database.actionDetailsDao(),
                            database.planFullDao(),
                            database.actionLibDAO(),
                            database.muscleDao()
                        ),
                        actionLibRepository = ActionLibRepository(
                            database.actionLibDAO(),
                            database.muscleDao(),
                            database.actionMuscleDao()
                        )
                    ) as T
                }
            }
        }
    }
}