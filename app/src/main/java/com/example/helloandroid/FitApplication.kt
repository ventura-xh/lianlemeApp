package com.example.helloandroid

import android.app.Application
import com.example.helloandroid.dao.ActionDetailDao
import com.example.helloandroid.dao.ActionLibDAO
import com.example.helloandroid.dao.MuscleDao
import com.example.helloandroid.dao.PlanActionsDao
import com.example.helloandroid.dao.PlanFullDao
import com.example.helloandroid.dao.PlansDao
import com.example.helloandroid.database.AppDatabase
import com.example.helloandroid.repository.ActionLibRepository
import com.example.helloandroid.repository.PlanRepository
import com.example.helloandroid.repository.TrainingRepository
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

        // ✅ 清理超过24小时未完成的训练
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getInstance(this@FitApplication)
                val repository = TrainingRepository(
                    database.trainingSessionDao(),
                    database.trainingSessionActionDao(),
                    database.trainingSessionActionDetailDao(),
                    planRepository = PlanRepository(
                        database.plansDao(),
                        database.planActionsDao(),
                        database.actionDetailsDao(),
                        database.planFullDao(),
                        database.actionLibDAO(),
                        database.muscleDao()
                    )
                )
                repository.cleanupStaleSessions()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initializePresetData() {
        applicationScope.launch {
            try {
                val database = AppDatabase.getInstance(this@FitApplication)
                val repository = ActionLibRepository(
                    actionLibDAO = database.actionLibDAO(),
                    muscleDao = database.muscleDao(),
                    actionMuscleDao = database.actionMuscleDao()
                )
                // 检查是否需要初始化
                val count = database.actionLibDAO().getCount()
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