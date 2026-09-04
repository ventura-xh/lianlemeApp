package com.example.helloandroid.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "muscles")
data class MuscleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,           // 肌肉群名称，如 "股四头肌"
    val category: String,       // 所属部位，如 "下肢"
    val icon: String = "💪"     // 图标
)