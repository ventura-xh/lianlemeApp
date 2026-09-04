package com.example.helloandroid.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "training_session_action_details",
    foreignKeys = [
        ForeignKey(
            entity = TrainingSessionActionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionActionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionActionId")]
)
data class TrainingSessionActionDetailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionActionId: Long,
    val groupIndex: Int = 0,       // 第几组
    val weight: Double = 0.0,      // 实际使用的重量
    val reps: Int = 0,             // 实际完成的次数
    val isCompleted: Boolean = false, // 是否已完成
    val completedAt: Long = 0      // 完成时间戳
)
