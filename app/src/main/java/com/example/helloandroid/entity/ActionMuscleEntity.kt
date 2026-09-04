package com.example.helloandroid.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "action_muscles",
    foreignKeys = [
        ForeignKey(
            entity = ActionLibEntity::class,
            parentColumns = ["id"],
            childColumns = ["actionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MuscleEntity::class,
            parentColumns = ["id"],
            childColumns = ["muscleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("actionId"), Index("muscleId")]
)
data class ActionMuscleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionId: Long,
    val muscleId: Long
)