package com.example.helloandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroid.FitApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel(){
    private val _firstPlanId = MutableStateFlow<Long?>(null)
    val firstPlanId: StateFlow<Long?> = _firstPlanId.asStateFlow()

    private val _hasPlans = MutableStateFlow(false)
    val hasPlans: StateFlow<Boolean> = _hasPlans.asStateFlow()

    init {
        loadFirstPlan()
    }

    fun loadFirstPlan() {
        viewModelScope.launch {
            try {
                val database = FitApplication.instance.database
                val plans = database.plansDao().getAllPlansOnce()
                val first = plans.firstOrNull()
                _firstPlanId.value = first?.id
                _hasPlans.value = !plans.isEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
                _firstPlanId.value = null
                _hasPlans.value = false
            }
        }
    }
}