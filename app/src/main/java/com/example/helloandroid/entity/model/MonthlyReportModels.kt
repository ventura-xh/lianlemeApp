package com.example.helloandroid.entity.model

import com.example.helloandroid.entity.TrainingSessionEntity

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