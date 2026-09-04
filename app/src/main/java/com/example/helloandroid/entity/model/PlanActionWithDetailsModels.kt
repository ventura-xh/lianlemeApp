package com.example.helloandroid.entity.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.helloandroid.entity.ActionDetailEntity
import com.example.helloandroid.entity.ActionLibEntity
import com.example.helloandroid.entity.PlanActionsEntity
import com.example.helloandroid.entity.PlansEntity

// 查询结果，用于展示
data class PlanActionWithDetails(
    @Embedded val planAction: PlanActionsEntity,
    // ✅ 使用 @Relation 自动关联 actions 表
    @Relation(
        parentColumn = "actionId",      // PlanActionsEntity 的 actionId
        entityColumn = "id",            // ActionLibEntity 的 id
        entity = ActionLibEntity::class
    )
    val action: ActionLibEntity,        // 完整的动作对象

    @Relation(
        parentColumn = "id",            // PlanActionsEntity 的 id
        entityColumn = "planActionId",  // ActionDetailEntity 的 planActionId
        entity = ActionDetailEntity::class
    )
    val details: List<ActionDetailEntity>
)

data class PlanWithActionsAndDetails(
    @Embedded val plan: PlansEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "planId",
        entity = PlanActionsEntity::class
    )
    val planActions: List<PlanActionWithDetails>
)