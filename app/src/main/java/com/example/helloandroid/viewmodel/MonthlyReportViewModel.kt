package com.example.helloandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.helloandroid.FitApplication
import com.example.helloandroid.entity.TrainingSessionEntity
import com.example.helloandroid.entity.model.DaySessionData
import com.example.helloandroid.entity.model.MonthlyReportData
import com.example.helloandroid.entity.model.MuscleStat
import com.example.helloandroid.entity.model.WeeklyData
import com.example.helloandroid.entity.model.formatDate
import com.example.helloandroid.repository.PlanRepository
import com.example.helloandroid.repository.TrainingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MonthlyReportViewModel(
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    private val _reportData = MutableStateFlow<MonthlyReportData?>(null)
    val reportData: StateFlow<MonthlyReportData?> = _reportData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadMonthlyReport() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allSessions = trainingRepository.getAllSessionsOnce()
                val monthlyData = generateMonthlyReport(allSessions)
                _reportData.value = monthlyData
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isLoading.value = false
        }
    }

    private suspend fun generateMonthlyReport(allSessions: List<TrainingSessionEntity>): MonthlyReportData {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        // 筛选本月数据
        val monthSessions = allSessions.filter { session ->
            val c = Calendar.getInstance().apply { timeInMillis = session.startTime }
            c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month
        }

        // 月份标签
        val monthLabel = SimpleDateFormat("yyyy年 MM月", Locale.getDefault())
            .format(calendar.time)

        // 统计数据
        val totalSessions = monthSessions.size
        val totalDuration = monthSessions.sumOf { it.totalDuration }
        var totalActions = 0
        var totalGroups = 0

        // ✅ 肌肉统计 Map<肌肉名称, 次数>
        val muscleCountMap = mutableMapOf<String, Int>()

        // ✅ 遍历所有训练会话，统计动作和肌肉
        monthSessions.forEach { session ->
            try {
                // 获取训练详情
                val sessionDetail = trainingRepository.getSessionWithDetails(session.id)
                sessionDetail?.let { detail ->
                    detail.actions.forEach { actionWithDetails ->
                        totalActions++
                        totalGroups += actionWithDetails.details.size

                        // ✅ 获取该动作关联的肌肉
                        val muscleNames = trainingRepository.getMuscleNamesForAction(
                            actionWithDetails.action.actionId
                        )
                        muscleNames.forEach { muscleName ->
                            muscleCountMap[muscleName] =
                                (muscleCountMap[muscleName] ?: 0) + 1
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // ✅ 计算肌肉统计百分比
        val totalMuscleCount = muscleCountMap.values.sum().toFloat()
        val muscleStats = muscleCountMap
            .map { (name, count) ->
                MuscleStat(
                    muscleName = name,
                    count = count,
                    percentage = if (totalMuscleCount > 0) {
                        count / totalMuscleCount * 100
                    } else {
                        0f
                    }
                )
            }
            .sortedByDescending { it.count }
            .take(10)  // 只显示前10个

        // 每周数据
        val weeklyData = generateWeeklyData(monthSessions)

        // 每日训练
        val dailySessions = monthSessions
            .groupBy { formatDate(it.startTime) }
            .map { DaySessionData(it.key, it.value) }
            .sortedByDescending { it.date }

        return MonthlyReportData(
            monthLabel = monthLabel,
            totalSessions = totalSessions,
            totalDuration = totalDuration,
            totalActions = totalActions,
            totalGroups = totalGroups,
            weeklyData = weeklyData,
            muscleStats = muscleStats,
            dailySessions = dailySessions
        )
    }

    private fun generateWeeklyData(sessions: List<TrainingSessionEntity>): List<WeeklyData> {
        val weeks = mutableMapOf<Int, Int>()
        sessions.forEach { session ->
            val cal = Calendar.getInstance().apply { timeInMillis = session.startTime }
            val weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH)
            weeks[weekOfMonth] = (weeks[weekOfMonth] ?: 0) + 1
        }
        return (1..4).map { week ->
            WeeklyData(weekIndex = week, count = weeks[week] ?: 0)
        }
    }

    companion object {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = FitApplication.instance.database
                return MonthlyReportViewModel(
                    TrainingRepository(
                        sessionDao = database.trainingSessionDao(),
                        actionDao = database.trainingSessionActionDao(),
                        detailDao = database.trainingSessionActionDetailDao(),
                        planRepository = PlanRepository(
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