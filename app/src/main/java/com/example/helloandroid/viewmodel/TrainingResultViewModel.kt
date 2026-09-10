package com.example.helloandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.helloandroid.FitApplication
import com.example.helloandroid.entity.model.TrainingSessionWithDetails
import com.example.helloandroid.repository.TrainingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TrainingResultViewModel(
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    private val _sessionDetail = MutableStateFlow<TrainingSessionWithDetails?>(null)
    val sessionDetail: StateFlow<TrainingSessionWithDetails?> = _sessionDetail.asStateFlow()

    suspend fun getSessionDetail(sessionId: Long): TrainingSessionWithDetails? {
        return trainingRepository.getSessionWithDetails(sessionId)
    }

    companion object {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = FitApplication.instance.database
                return TrainingResultViewModel(
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