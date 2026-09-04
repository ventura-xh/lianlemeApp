package com.example.helloandroid

import android.app.Application
import com.example.helloandroid.dao.ActionLibDAO
import com.example.helloandroid.database.AppDatabase
import com.example.helloandroid.repository.ActionLibRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FitApplication : Application() {
    companion object {
        lateinit var instance: FitApplication
            private set
    }

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化动作库数据
        initializePresetData()
    }

    private fun initializePresetData() {
        applicationScope.launch {
            try {
                val database = AppDatabase.getInstance(this@FitApplication)
                val repository = ActionLibRepository(
                    actionLibDAO = database.actionDao(),
                    muscleDao = database.muscleDao(),
                    actionMuscleDao = database.actionMuscleDao()
                )
                // 检查是否需要初始化
                val count = database.actionDao().getCount()
                if (count == 0) {
                    // 首次启动，导入预设数据
                    repository.initializePresetActions(this@FitApplication)
                } else {
                    // 非首次启动，检查是否需要更新
                    repository.updatePresetActionsIfNeeded(this@FitApplication)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}