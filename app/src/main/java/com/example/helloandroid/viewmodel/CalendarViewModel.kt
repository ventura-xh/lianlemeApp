package com.example.helloandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.helloandroid.FitApplication
import com.example.helloandroid.entity.TrainingSessionEntity
import com.example.helloandroid.repository.TrainingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    private val _allSessions = MutableStateFlow<List<TrainingSessionEntity>>(emptyList())
    val allSessions: StateFlow<List<TrainingSessionEntity>> = _allSessions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadAllSessions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val sessions = trainingRepository.getAllSessionsOnce()
                // 按开始时间倒序排列（最新的在前）
                _allSessions.value = sessions.sortedByDescending { it.startTime }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isLoading.value = false
        }
    }

    companion object {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = FitApplication.instance.database
                return CalendarViewModel(
                    TrainingRepository(
                        sessionDao = database.trainingSessionDao(),
                        actionDao = database.trainingSessionActionDao(),
                        detailDao = database.trainingSessionActionDetailDao(),
                        planRepository = com.example.helloandroid.repository.PlanRepository(
                            database.plansDao(),
                            database.planActionsDao(),
                            database.actionDetailsDao(),
                            database.planFullDao(),
                            database.actionLibDAO(),
                            database.muscleDao()
                        )
                    )
                ) as T
            }
        }
    }
}