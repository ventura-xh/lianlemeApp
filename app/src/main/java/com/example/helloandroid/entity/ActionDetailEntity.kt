package com.example.helloandroid.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "action_detail",
    foreignKeys = [
        ForeignKey(
            entity = PlanActionsEntity::class,
            parentColumns = ["id"],
            childColumns = ["planActionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planActionId")]
)
data class ActionDetailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val planActionId: Long = 0,
    val groupIndex: Long = 0,
    val isLeftRight: Boolean = false,
    val weight: Double = 0.0,
    val reps: Long = 0,
    val weightUnit: String = "kg",
    val restSeconds: Long = 60,
    val sortOrder: Long = 0
)
