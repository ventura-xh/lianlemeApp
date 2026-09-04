package com.example.helloandroid.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plan_actions",
    foreignKeys = [
        ForeignKey(
            entity = PlansEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ActionLibEntity::class,
            parentColumns = ["id"],
            childColumns = ["actionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("planId"), Index("actionId")]
)
data class PlanActionsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionId: Long = 0,
    val sortOrder: Int = 0,
    val planId: Long = 0
)
