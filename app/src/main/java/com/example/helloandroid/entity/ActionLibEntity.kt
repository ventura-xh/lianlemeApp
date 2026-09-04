package com.example.helloandroid.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "action_lib")
data class ActionLibEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val category: String = "",
    val isPreset: Boolean = true,
    val description: String = "",       // ✅ 动作描述/要领
    val tips: String = ""               // ✅ 注意事项
)