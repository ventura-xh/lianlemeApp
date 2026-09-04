package com.example.helloandroid.utils

/**
 * RM 计算器工具类
 * 使用 Epley 和 Brzycki 公式估算 1RM
 */
object RmCalculator {

    /**
     * 计算 1RM 结果
     * @param weight 举起重量 (kg)
     * @param reps 力竭次数
     * @return RMResult 包含各公式计算结果
     */
    fun calculate(weight: Double, reps: Int): RMResult? {
        if (weight <= 0 || reps <= 0) return null

        // ✅ Epley 公式: 1RM = 重量 × (1 + 次数 / 30)
        val epley = weight * (1 + reps / 30.0)

        // ✅ Brzycki 公式: 1RM = 重量 × (36 / (37 - 次数))
        val brzycki = if (reps < 37) {
            weight * (36.0 / (37 - reps))
        } else {
            weight * 1.1 // 次数 >= 37 时使用近似值
        }

        // ✅ 平均值
        val average = (epley + brzycki) / 2

        return RMResult(
            epley = epley,
            brzycki = brzycki,
            average = average,
            weight = weight,
            reps = reps
        )
    }

    /**
     * 计算各次数的估算重量
     * @param oneRM 1RM 值
     * @param maxReps 最大次数（默认 20）
     * @return Map<次数, RMWeight>
     */
    fun calculateRMTable(oneRM: Double, maxReps: Int = 20): Map<Int, RMWeight> {
        val result = mutableMapOf<Int, RMWeight>()
        for (reps in 1..maxReps) {
            // Epley 反推: 重量 = 1RM / (1 + reps / 30)
            val epleyWeight = oneRM / (1 + reps / 30.0)
            // Brzycki 反推: 重量 = 1RM * (37 - reps) / 36
            val brzyckiWeight = if (reps < 37) {
                oneRM * (37 - reps) / 36.0
            } else {
                oneRM / 1.1
            }
            val avgWeight = (epleyWeight + brzyckiWeight) / 2
            result[reps] = RMWeight(
                epley = epleyWeight,
                brzycki = brzyckiWeight,
                average = avgWeight
            )
        }
        return result
    }
}

/**
 * RM 计算结果
 */
data class RMResult(
    val epley: Double,      // Epley 公式结果
    val brzycki: Double,    // Brzycki 公式结果
    val average: Double,    // 平均值
    val weight: Double,     // 原始重量
    val reps: Int           // 原始次数
) {
    // 格式化显示
    fun getFormattedEpley(): String = String.format("%.1f kg", epley)
    fun getFormattedBrzycki(): String = String.format("%.1f kg", brzycki)
    fun getFormattedAverage(): String = String.format("%.1f kg", average)
}

/**
 * RM 对照表单项
 */
data class RMWeight(
    val epley: Double,
    val brzycki: Double,
    val average: Double
) {
    fun getFormattedEpley(): String = String.format("%.1f", epley)
    fun getFormattedBrzycki(): String = String.format("%.1f", brzycki)
    fun getFormattedAverage(): String = String.format("%.1f kg", average)
}