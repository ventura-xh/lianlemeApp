package com.example.helloandroid.entity.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.helloandroid.entity.ActionLibEntity
import com.example.helloandroid.entity.ActionMuscleEntity

data class ActionWithMuscles(
    @Embedded val action: ActionLibEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "actionId",
        entity = ActionMuscleEntity::class
    )
    val muscleIds: List<Long>
)