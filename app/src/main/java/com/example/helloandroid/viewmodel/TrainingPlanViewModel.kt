package com.example.helloandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.helloandroid.FitApplication
import com.example.helloandroid.entity.PlansEntity
import com.example.helloandroid.repository.PlanRepository
import com.example.helloandroid.ui.common.InitState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrainingPlanViewModel(
    private val planRepository: PlanRepository
) : ViewModel() {

    // ✅ 使用 Flow 实时监听数据库变化
    val plans: StateFlow<List<PlansEntity>> = planRepository.getAllPlans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _plans = MutableStateFlow<List<PlansEntity>>(emptyList())

    private val _initState = MutableStateFlow<InitState>(InitState.Loading)
    val initState: StateFlow<InitState> = _initState.asStateFlow()

    init {
        loadPlans()
    }

    fun loadPlans() {
        viewModelScope.launch {
            _initState.update { InitState.Loading }
            try {
                val result = planRepository.getAllPlansOnce()
                _plans.update { result }
                _initState.update { InitState.Success }
            } catch (e: Exception) {
                _initState.update { InitState.Error(e.message ?: "加载失败") }
            }
        }
    }

    companion object {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TrainingPlanViewModel(
                    PlanRepository(
                        FitApplication.instance.database.plansDao(),
                        FitApplication.instance.database.planActionsDao(),
                        FitApplication.instance.database.actionDetailsDao(),
                        FitApplication.instance.database.planFullDao(),
                        FitApplication.instance.database.actionLibDAO(),
                        FitApplication.instance.database.muscleDao()
                    )
                ) as T
            }
        }
    }
}