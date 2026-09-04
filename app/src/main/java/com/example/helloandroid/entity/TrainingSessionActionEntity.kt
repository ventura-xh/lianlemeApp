package com.example.helloandroid.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "training_session_actions",
    foreignKeys = [
        ForeignKey(
            entity = TrainingSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ActionLibEntity::class,
            parentColumns = ["id"],
            childColumns = ["actionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("sessionId"), Index("actionId")]
)
data class TrainingSessionActionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val actionId: Long,            // 关联的动作库 ID
    val actionName: String = "",   // 冗余存储动作名称（历史快照）
    val sortOrder: Int = 0,        // 执行顺序
    val isCompleted: Boolean = false // 是否已完成
)
