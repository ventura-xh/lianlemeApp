package com.example.helloandroid.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

object BodyFatCalculator {

    enum class Method {
        THREE_POINT,
        SEVEN_POINT
    }

    enum class Gender {
        MALE,
        FEMALE
    }

    data class Result(
        val bodyFatPercentage: Double,
        val bodyFatMass: Double,
        val leanBodyMass: Double,
        val category: String,
        val categoryColor: Color,
        val method: Method,
        val gender: Gender,
        val age: Int
    ) {
        val methodDisplay: String
            get() = when (method) {
                Method.THREE_POINT -> "三点测量"
                Method.SEVEN_POINT -> "七点测量"
            }
    }

    /**
     * 三点测量法 (Jackson-Pollock 公式)
     * 测量部位：胸、腹、大腿
     */
    fun calculateThreePoint(
        gender: Gender,
        age: Int,
        chest: Double,      // 胸 (mm)
        abdominal: Double,  // 腹 (mm)
        thigh: Double       // 大腿 (mm)
    ): Result? {
        val sum = chest + abdominal + thigh

        // 体密度计算 (Jackson-Pollock 公式)
        val density = when (gender) {
            Gender.MALE -> {
                // 男性: 1.10938 - 0.0008267 * sum + 0.0000016 * sum^2 - 0.0002574 * age
                1.10938 - 0.0008267 * sum + 0.0000016 * sum * sum - 0.0002574 * age
            }
            Gender.FEMALE -> {
                // 女性: 1.0994921 - 0.0009929 * sum + 0.0000023 * sum^2 - 0.0001392 * age
                1.0994921 - 0.0009929 * sum + 0.0000023 * sum * sum - 0.0001392 * age
            }
        }

        return calculateFromDensity(density, gender, age, Method.THREE_POINT)
    }

    /**
     * 七点测量法 (Jackson-Pollock 公式)
     * 测量部位：胸、腋窝、三头肌、肩胛下、腹、髂上、大腿
     */
    fun calculateSevenPoint(
        gender: Gender,
        age: Int,
        chest: Double,       // 胸 (mm)
        axillary: Double,    // 腋窝 (mm)
        triceps: Double,     // 三头肌 (mm)
        subscapular: Double, // 肩胛下 (mm)
        abdominal: Double,   // 腹 (mm)
        suprailiac: Double,  // 髂上 (mm)
        thigh: Double        // 大腿 (mm)
    ): Result? {
        val sum = chest + axillary + triceps + subscapular + abdominal + suprailiac + thigh

        // 体密度计算 (Jackson-Pollock 七点公式)
        val density = when (gender) {
            Gender.MALE -> {
                // 男性: 1.112 - 0.00043499 * sum + 0.00000055 * sum^2 - 0.00028826 * age
                1.112 - 0.00043499 * sum + 0.00000055 * sum * sum - 0.00028826 * age
            }
            Gender.FEMALE -> {
                // 女性: 1.097 - 0.00046971 * sum + 0.00000056 * sum^2 - 0.00012828 * age
                1.097 - 0.00046971 * sum + 0.00000056 * sum * sum - 0.00012828 * age
            }
        }

        return calculateFromDensity(density, gender, age, Method.SEVEN_POINT)
    }

    /**
     * 从体密度计算体脂率
     */
    private fun calculateFromDensity(
        density: Double,
        gender: Gender,
        age: Int,
        method: Method
    ): Result? {
        if (density <= 0) return null

        // Siri 公式: 体脂率 = (495 / 密度) - 450
        val bodyFatPercentage = (495 / density) - 450

        // 限制范围
        val clampedPercentage = bodyFatPercentage.coerceIn(2.0, 60.0)

        // 假设体重 70kg 用于计算（实际可以从用户数据获取）
        val weight = 70.0
        val bodyFatMass = weight * clampedPercentage / 100
        val leanBodyMass = weight - bodyFatMass

        // 分类
        val (category, categoryColor) = getCategory(clampedPercentage, gender)

        return Result(
            bodyFatPercentage = clampedPercentage,
            bodyFatMass = bodyFatMass,
            leanBodyMass = leanBodyMass,
            category = category,
            categoryColor = categoryColor,
            method = method,
            gender = gender,
            age = age
        )
    }

    /**
     * 获取体脂率分类
     */
    private fun getCategory(percentage: Double, gender: Gender): Pair<String, Color> {
        return when (gender) {
            Gender.MALE -> when {
                percentage < 6 -> Pair("⚠️ 必需脂肪过低", Color(0xFFE53935))
                percentage < 10 -> Pair("运动员水平", Color(0xFF43A047))
                percentage < 15 -> Pair("优秀", Color(0xFF1E88E5))
                percentage < 18 -> Pair("健康", Color(0xFF43A047))
                percentage < 22 -> Pair("标准", Color(0xFFFB8C00))
                percentage < 28 -> Pair("偏高", Color(0xFFE65100))
                else -> Pair("⚠️ 过高", Color(0xFFE53935))
            }
            Gender.FEMALE -> when {
                percentage < 10 -> Pair("⚠️ 必需脂肪过低", Color(0xFFE53935))
                percentage < 14 -> Pair("运动员水平", Color(0xFF43A047))
                percentage < 18 -> Pair("优秀", Color(0xFF1E88E5))
                percentage < 24 -> Pair("健康", Color(0xFF43A047))
                percentage < 30 -> Pair("标准", Color(0xFFFB8C00))
                percentage < 36 -> Pair("偏高", Color(0xFFE65100))
                else -> Pair("⚠️ 过高", Color(0xFFE53935))
            }
        }
    }
}