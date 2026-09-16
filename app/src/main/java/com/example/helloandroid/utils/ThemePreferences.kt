// utils/ThemePreferences.kt

package com.example.helloandroid.utils

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ThemePreferences {

    private const val PREF_NAME = "theme_prefs"
    private const val KEY_DARK_MODE = "dark_mode"

    // ✅ 深色模式状态（可选值：0=跟随系统，1=浅色，2=深色）
    var darkModeState by mutableStateOf(0)
        private set

    /**
     * 初始化主题
     */
    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        darkModeState = prefs.getInt(KEY_DARK_MODE, 0)
    }

    /**
     * 设置深色模式
     */
    fun setDarkMode(context: Context, state: Int) {
        darkModeState = state
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_DARK_MODE, state).apply()
    }

    /**
     * 是否使用深色主题
     */
    fun isDarkTheme(context: Context): Boolean {
        return when (darkModeState) {
            1 -> false  // 浅色
            2 -> true   // 深色
            else -> {   // 跟随系统
                val uiMode = context.resources.configuration.uiMode
                (uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                        android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
}