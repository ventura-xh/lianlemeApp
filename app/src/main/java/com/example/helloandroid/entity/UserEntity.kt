package com.example.helloandroid.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nickname: String = "健身爱好者",
    val uid: String = "",  // 用户唯一标识
    val avatar: String? = null,  // 头像路径（可选）

    // ✅ 身体数据
    val weight: Double = 0.0,  // 体重 (kg)
    val height: Double = 0.0,  // 身高 (cm)
    val age: Int = 0,  // 年龄
    val gender: Int = 0,  // 0=未知, 1=男, 2=女

    // ✅ 围度数据 (cm)
    val chest: Double = 0.0,  // 胸围
    val waist: Double = 0.0,  // 腰围
    val hip: Double = 0.0,  // 臀围
    val arm: Double = 0.0,  // 臂围
    val leg: Double = 0.0,  // 腿围

    // ✅ 身体指标
    val bodyFat: Double = 0.0,  // 体脂率 (%)
    val muscleMass: Double = 0.0,  // 肌肉量 (kg)
    val bmi: Double = 0.0,  // BMI

    val updatedAt: Long = System.currentTimeMillis()  // 更新时间
)