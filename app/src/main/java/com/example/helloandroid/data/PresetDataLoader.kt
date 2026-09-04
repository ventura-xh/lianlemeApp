// data/PresetDataLoader.kt

package com.example.helloandroid.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PresetDataLoader {

    data class PresetData(
        val version: Int = 1,
        val muscles: List<MusclePreset>,
        val actions: List<ActionPreset>
    )

    data class MusclePreset(
        val name: String,
        val category: String,
        val icon: String = "💪"
    )

    data class ActionPreset(
        val name: String,
        val category: String,
        val description: String = "",
        val tips: String = "",
        val muscles: List<String> = emptyList()
    )

    /**
     * 从 assets 加载预设数据
     */
    fun loadPresetData(context: Context): PresetData {
        val inputStream = context.assets.open("preset_actions.json")
        val json = inputStream.bufferedReader().use { it.readText() }
        val type = object : TypeToken<PresetData>() {}.type
        return Gson().fromJson(json, type)
    }

    fun getPresetVersion(context: Context): Int {
        return try {
            loadPresetData(context).version
        } catch (e: Exception) {
            0
        }
    }
}