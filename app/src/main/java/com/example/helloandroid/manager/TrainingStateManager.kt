// manager/TrainingStateManager.kt

package com.example.helloandroid.manager

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * 全局训练状态管理器
 * 用于在不同页面之间共享训练状态
 */
object TrainingStateManager {

    // ✅ 是否有训练进行中
    var isTrainingActive by mutableStateOf(false)
        private set

    // ✅ 当前训练的计划ID
    var currentPlanId by mutableStateOf<Long?>(null)
        private set

    // ✅ 当前训练的计划名称
    var currentPlanName by mutableStateOf("")
        private set

    // ✅ 是否在运动页面（用于控制 App 内悬浮窗显示）
    var isOnExecutePlanPage by mutableStateOf(false)

    /**
     * 开始训练
     */
    fun startTraining(planId: Long, planName: String) {
        isTrainingActive = true
        currentPlanId = planId
        currentPlanName = planName
    }

    /**
     * 结束训练
     */
    fun stopTraining() {
        isTrainingActive = false
        currentPlanId = null
        currentPlanName = ""
        isOnExecutePlanPage = false
    }

    /**
     * 检查训练是否进行中
     */
    fun hasActiveTraining(): Boolean {
        return isTrainingActive && currentPlanId != null
    }
}