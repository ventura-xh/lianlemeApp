package com.example.helloandroid.entity.model

import java.util.UUID

/**
 * 计划中的动作（包含多组训练参数）
 */
data class PlanAction(
    val actionId: Long,
    val actionName: String = "",
    val groups: MutableList<ActionGroup> = mutableListOf(ActionGroup()) // 默认一组
)

/**
 * 单组训练参数
 */
data class ActionGroup(
    val id: String = UUID.randomUUID().toString(), // 唯一标识，用于列表更新
    var weight: String = "",
    var reps: String = "",
    var isLeftRight: Boolean = false
)