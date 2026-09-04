package com.example.helloandroid.entity.model

/**
 * 用于创建或更新计划时，传入一个动作及其所有组的训练参数
 * @param actionId 动作库中的动作 ID
 * @param sortOrder 动作在计划中的执行顺序
 * @param details 该动作的所有组详情（重量、次数、组数等）
 */
data class PlanActionWithDetailsInput(
    val actionId: Long,
    val sortOrder: Int = 0,
    val details: List<ActionDetailInput> = emptyList()
)

/**
 * 单组训练参数
 * @param groupIndex 第几组（从 0 开始）
 * @param weight 重量
 * @param reps 次数
 * @param weightUnit 重量单位（kg、lb 等）
 * @param restSeconds 组间休息秒数
 * @param isLeftRight 是否左右分别训练
 */
data class ActionDetailInput(
    val groupIndex: Long = 0,
    val weight: Double = 0.0,
    val reps: Long = 0,
    val weightUnit: String = "kg",
    val restSeconds: Long = 60,
    val isLeftRight: Boolean = false
)