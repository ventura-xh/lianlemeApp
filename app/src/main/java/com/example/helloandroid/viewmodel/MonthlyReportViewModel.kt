package com.example.helloandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.helloandroid.FitApplication
import com.example.helloandroid.entity.TrainingSessionEntity
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

// ✅ 月报数据
data class MonthlyReportData(
    val monthLabel: String,
    val totalSessions: Int,
    val totalDuration: Long,
    val totalActions: Int,
    val totalGroups: Int,
    val weeklyData: List<WeeklyData>,
    val muscleStats: List<MuscleStat>,
    val dailySessions: List<DaySessionData>
)

// ✅ 每周数据
data class WeeklyData(
    val weekIndex: Int,
    val count: Int
)

// ✅ 肌肉统计
data class MuscleStat(
    val muscleName: String,
    val count: Int,
    val percentage: Float
)

// ✅ 每日训练数据
data class DaySessionData(
    val date: String,
    val sessions: List<TrainingSessionEntity>
)

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

    private fun generateMonthlyReport(allSessions: List<TrainingSessionEntity>): MonthlyReportData {
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

        // TODO: 统计动作和组数（需要从数据库查询）
        // 这里先使用示例数据

        // 每周数据
        val weeklyData = generateWeeklyData(monthSessions)

        // 肌肉统计（示例数据）
        val muscleStats = listOf(
            MuscleStat("胸大肌", 12, 25f),
            MuscleStat("背阔肌", 10, 21f),
            MuscleStat("股四头肌", 8, 17f),
            MuscleStat("臀大肌", 6, 13f),
            MuscleStat("三角肌", 6, 13f),
            MuscleStat("肱二头肌", 5, 11f)
        )

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
                            database.planFullDao()
                        )
                    )
                ) as T
            }
        }
    }
}