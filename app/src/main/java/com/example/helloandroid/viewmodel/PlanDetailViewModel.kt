package com.example.helloandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.helloandroid.FitApplication
import com.example.helloandroid.repository.PlanRepository
import com.example.helloandroid.ui.plan.PlanActionWithGroups
import com.example.helloandroid.ui.plan.PlanDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlanDetailViewModel(
    private val planRepository: PlanRepository
) : ViewModel() {

    private val _planDetail = MutableStateFlow<PlanDetail?>(null)
    val planDetail: StateFlow<PlanDetail?> = _planDetail.asStateFlow()

    suspend fun getPlanDetail(planId: Long): PlanDetail? {
        // 1. 获取计划基本信息
        val plan = planRepository.getPlanById(planId) ?: return null

        // 2. 获取计划的所有动作（含组详情）
        val planActions = planRepository.getPlanActionsWithDetails(planId)

        // 3. 构建 PlanDetail
        val actionsWithGroups = planActions.map { pa ->
            PlanActionWithGroups(
                planAction = pa.planAction,
                action = pa.action,  // 从 @Relation 获取
                groups = pa.groups  // 从 @Relation 获取
            )
        }

        return PlanDetail(
            plan = plan,
            actions = actionsWithGroups
        )
    }

    companion object {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = FitApplication.instance.database
                return PlanDetailViewModel(
                    PlanRepository(
                        database.plansDao(),
                        database.planActionsDao(),
                        database.actionDetailsDao(),
                        database.planFullDao(),
                        database.actionLibDAO(),
                        database.muscleDao()
                    )
                ) as T
            }
        }
    }
}