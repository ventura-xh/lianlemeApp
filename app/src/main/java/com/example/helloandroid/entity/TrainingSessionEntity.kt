package com.example.helloandroid.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_sessions")
data class TrainingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val planId: Long,              // 关联的计划 ID
    val planName: String = "",     // 冗余存储计划名称（历史快照）
    val startTime: Long,           // 开始时间戳
    val endTime: Long = 0,         // 结束时间戳（0 表示未结束）
    val totalDuration: Long = 0,   // 总时长（秒）
    val status: Int = 0            // 0=进行中，1=已完成，2=已取消
)
