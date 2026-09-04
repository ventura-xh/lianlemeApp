package com.example.helloandroid.entity.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.helloandroid.entity.TrainingSessionActionDetailEntity
import com.example.helloandroid.entity.TrainingSessionActionEntity
import com.example.helloandroid.entity.TrainingSessionEntity

// ============================================================
// 1. 训练历史查询结果（用于查看历史记录）
// ============================================================

/**
 * 训练会话 + 所有动作 + 所有组详情
 */
data class TrainingSessionWithDetails(
    @Embedded val session: TrainingSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId",
        entity = TrainingSessionActionEntity::class
    )
    val actions: List<TrainingActionWithDetails>
)

/**
 * 训练会话中的单个动作 + 所有组详情
 */
data class TrainingActionWithDetails(
    @Embedded val action: TrainingSessionActionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionActionId",
        entity = TrainingSessionActionDetailEntity::class
    )
    val details: List<TrainingSessionActionDetailEntity>
)

// ============================================================
// 2. 训练执行内存模型（用于训练过程中）
// ============================================================

/**
 * 训练会话（内存模型，用于训练执行过程中）
 */
data class TrainingSession(
    val planId: Long,
    val planName: String,
    val startTime: Long = System.currentTimeMillis(),
    val actions: MutableList<TrainingAction> = mutableListOf(),
    var endTime: Long = 0,
    var status: Int = 0 // 0=进行中，1=已完成，2=已取消
)

/**
 * 训练动作（内存模型）
 */
data class TrainingAction(
    val actionId: Long,
    val actionName: String,
    val groups: MutableList<TrainingGroup> = mutableListOf(),
    var isCompleted: Boolean = false
)

/**
 * 训练组（内存模型）
 */
data class TrainingGroup(
    var groupIndex: Int,
    var weight: Double = 0.0,
    var reps: Int = 0,
    var isCompleted: Boolean = false,
    var completedAt: Long = 0  // ✅ 新增：完成时间戳
)

// ============================================================
// 3. 训练统计信息
// ============================================================

/**
 * 训练统计数据
 */
data class TrainingStatistics(
    val totalSessions: Int,           // 总训练次数
    val totalDuration: Long,          // 总时长（秒）
    val totalActions: Int,            // 总动作数
    val totalCompletedGroups: Int,    // 总完成组数
    val avgDurationPerSession: Double = 0.0  // 平均每次训练时长
) {
    // 格式化时长
    fun getFormattedDuration(): String {
        val hours = totalDuration / 3600
        val minutes = (totalDuration % 3600) / 60
        val seconds = totalDuration % 60
        return when {
            hours > 0 -> String.format("%dh %02dm", hours, minutes)
            minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
            else -> String.format("%ds", seconds)
        }
    }
}

/**
 * 单次训练统计
 */
data class SingleSessionStatistics(
    val sessionId: Long,
    val planName: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    val totalActions: Int,
    val completedActions: Int,
    val totalGroups: Int,
    val completedGroups: Int,
    val completionRate: Float // 0.0 - 1.0
)

// ============================================================
// 4. 动作统计
// ============================================================

/**
 * 动作使用统计（某个动作在所有训练中的统计）
 */
data class ActionUsageStatistics(
    val actionId: Long,
    val actionName: String,
    val totalSessions: Int,           // 出现在多少次训练中
    val totalCompletedGroups: Int,    // 总共完成了多少组
    val avgWeight: Double,            // 平均重量
    val avgReps: Double,              // 平均次数
    val maxWeight: Double,            // 最大重量
    val maxReps: Int,                 // 最大次数
    val bestSet: BestSet? = null      // 最佳一组
)

/**
 * 最佳一组
 */
data class BestSet(
    val weight: Double,
    val reps: Int,
    val sessionId: Long,
    val sessionDate: Long,
    val actionName: String
)

/**
 * 动作在单次训练中的表现
 */
data class ActionPerformance(
    val actionId: Long,
    val actionName: String,
    val groups: List<GroupPerformance>
)

/**
 * 单组表现
 */
data class GroupPerformance(
    val groupIndex: Int,
    val weight: Double,
    val reps: Int,
    val isCompleted: Boolean
)

// ============================================================
// 5. 训练日历数据
// ============================================================

/**
 * 训练日历项（用于日历视图）
 */
data class TrainingCalendarItem(
    val date: String,                 // yyyy-MM-dd
    val sessions: List<TrainingSessionEntity>,
    val totalDuration: Long,
    val totalActions: Int
)

// ============================================================
// 6. 拓展函数
// ============================================================

/**
 * 将训练会话实体转换为内存模型
 */
fun TrainingSessionEntity.toMemoryModel(
    actions: List<TrainingActionWithDetails>
): TrainingSession {
    val session = TrainingSession(
        planId = this.planId,
        planName = this.planName,
        startTime = this.startTime,
        endTime = this.endTime,
        status = this.status
    )

    actions.forEach { actionWithDetails ->
        val groups = actionWithDetails.details.mapIndexed { index, detail ->
            TrainingGroup(
                groupIndex = index,
                weight = detail.weight,
                reps = detail.reps,
                isCompleted = detail.isCompleted
            )
        }.toMutableList()

        session.actions.add(
            TrainingAction(
                actionId = actionWithDetails.action.actionId,
                actionName = actionWithDetails.action.actionName,
                groups = groups,
                isCompleted = actionWithDetails.action.isCompleted
            )
        )
    }

    return session
}

/**
 * 计算训练完成率
 */
fun TrainingSession.getCompletionRate(): Float {
    if (actions.isEmpty()) return 0f
    var total = 0
    var completed = 0
    actions.forEach { action ->
        total += action.groups.size
        completed += action.groups.count { it.isCompleted }
    }
    return if (total > 0) completed.toFloat() / total else 0f
}

/**
 * 获取训练总组数
 */
fun TrainingSession.getTotalGroups(): Int {
    return actions.sumOf { it.groups.size }
}

/**
 * 获取已完成组数
 */
fun TrainingSession.getCompletedGroups(): Int {
    return actions.sumOf { it.groups.count { group -> group.isCompleted } }
}

/**
 * 格式化时长（秒 → 时分秒）
 */
fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
        hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, secs)
        else -> String.format("%02d:%02d", minutes, secs)
    }
}

/**
 * 格式化时间戳
 */
fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    return format.format(date)
}

/**
 * 格式化日期
 */
fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return format.format(date)
}