package com.example.helloandroid.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.helloandroid.FitApplication
import com.example.helloandroid.entity.model.TrainingGroup
import com.example.helloandroid.entity.model.TrainingSession
import com.example.helloandroid.repository.PlanRepository
import com.example.helloandroid.repository.TrainingRepository
import com.example.helloandroid.service.TrainingTimerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

// ✅ 改为继承 AndroidViewModel，以便获取 Application Context
class ExecutePlanViewModel(
    private val application: Application,
    private val trainingRepository: TrainingRepository
) : AndroidViewModel(application) {

    private val _session = MutableStateFlow<TrainingSession?>(null)
    val session: StateFlow<TrainingSession?> = _session.asStateFlow()

    private val _savedSessionId = MutableStateFlow<Long?>(null)
    val savedSessionId: StateFlow<Long?> = _savedSessionId.asStateFlow()

    // ✅ 当前展开的动作卡片索引
    private val _currentActionIndex = MutableStateFlow(0)
    val currentActionIndex: StateFlow<Int> = _currentActionIndex.asStateFlow()

    private val _currentGroupIndex = MutableStateFlow(0)
    val currentGroupIndex: StateFlow<Int> = _currentGroupIndex.asStateFlow()

    // ✅ 从 Service 获取时间
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(true)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    // ✅ 倒计时状态
    private val _showRestDialog = MutableStateFlow(false)
    val showRestDialog: StateFlow<Boolean> = _showRestDialog.asStateFlow()

    // ✅ 默认休息时间（用户可调整）
    private var defaultRestSeconds = 60

    private val _restSeconds = MutableStateFlow(60)
    val restSeconds: StateFlow<Int> = _restSeconds.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _showRestFloating = MutableStateFlow(false)
    val showRestFloating: StateFlow<Boolean> = _showRestFloating.asStateFlow()

    private var restTimerJob: Job? = null

    // ✅ 悬浮窗位置
    private val _floatingOffsetX = MutableStateFlow(0f)
    val floatingOffsetX: StateFlow<Float> = _floatingOffsetX.asStateFlow()

    private val _floatingOffsetY = MutableStateFlow(80f)
    val floatingOffsetY: StateFlow<Float> = _floatingOffsetY.asStateFlow()

    // ✅ 用于触发 UI 刷新
    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    // ✅ 计时器服务是否已启动
    private var isServiceStarted = false

    // ============================================================
    // 1. 加载训练计划
    // ============================================================

    fun loadPlan(planId: Long, planName: String) {
        viewModelScope.launch {
            val trainingSession = trainingRepository.loadSessionFromPlan(planId, planName)
            _session.value = trainingSession
            findFirstIncompleteAction()

            _elapsedTime.value = 0L
            val context = getApplication<Application>().applicationContext
            TrainingTimerService.resetTime()
        }
    }

    // ============================================================
    // 2. 计时器（使用 Service）
    // ============================================================

    fun startTimer() {
        if (isServiceStarted) return
        isServiceStarted = true

        val context = getApplication<Application>().applicationContext

        // ✅ 标记训练活跃
        TrainingTimerService.isTrainingActive = true

        // ✅ 先重置再显示
        TrainingTimerService.resetTime()
        _elapsedTime.value = 0L

        // ✅ 启动计时
        TrainingTimerService.startTimer(context)

        // 监听时间更新
        TrainingTimerService.elapsedTime.observeForever { time ->
            time?.let { _elapsedTime.value = it }
        }
    }

    fun stopTimer() {
        isServiceStarted = false

        // ✅ 标记训练活跃
        TrainingTimerService.isTrainingActive = false

        val context = getApplication<Application>().applicationContext
        TrainingTimerService.stopTimer(context)
        TrainingTimerService.stopTrainingCompletely(context)
        TrainingTimerService.hideOverlay(context)
    }

    // ============================================================
    // 3. 查找第一个未完成的动作
    // ============================================================

    private fun findNextUncompletedGroup(): Pair<Int, Int>? {
        val session = _session.value ?: return null
        for (i in session.actions.indices) {
            val action = session.actions[i]
            for (j in action.groups.indices) {
                if (!action.groups[j].isCompleted) {
                    return i to j
                }
            }
        }
        return null
    }

    private fun findFirstIncompleteAction() {
        val session = _session.value ?: return
        for (i in session.actions.indices) {
            val action = session.actions[i]
            if (!action.isCompleted) {
                _currentActionIndex.value = i
                return
            }
        }
        if (session.actions.isNotEmpty()) {
            _currentActionIndex.value = session.actions.size - 1
        }
    }

    // ============================================================
    // 4. 切换动作完成状态
    // ============================================================

    fun toggleActionCompleted(actionIndex: Int) {
        val session = _session.value ?: return
        val action = session.actions.getOrNull(actionIndex) ?: return

        action.isCompleted = !action.isCompleted

        if (action.isCompleted) {
            action.groups.forEach { it.isCompleted = true }
        } else {
            action.groups.forEach { it.isCompleted = false }
        }

        _session.value = session.copy()
        _refreshTrigger.value++
        findFirstIncompleteAction()
    }

    // ============================================================
    // 5. 切换组完成状态
    // ============================================================

    fun toggleGroupCompleted(actionIndex: Int, groupIndex: Int) {
        val session = _session.value ?: return
        val action = session.actions.getOrNull(actionIndex) ?: return
        val group = action.groups.getOrNull(groupIndex) ?: return

        if (group.isCompleted) {
            group.isCompleted = false
            action.isCompleted = false
            _session.value = session.copy()
            _refreshTrigger.value++
            return
        }

        group.isCompleted = true
        group.completedAt = System.currentTimeMillis()

        val allGroupsCompleted = action.groups.all { it.isCompleted }
        if (allGroupsCompleted) {
            action.isCompleted = true
            findFirstIncompleteAction()
        }

        _session.value = session.copy()
        _refreshTrigger.value++

        val nextGroup = findNextUncompletedGroup()
        if (nextGroup != null) {
            if (restTimerJob == null || restTimerJob?.isActive != true) {
                startRestTimer()
            }
        }
    }

    // ============================================================
    // 6. 更新组数据（重量/次数）
    // ============================================================

    fun updateGroupWeight(actionIndex: Int, groupIndex: Int, weight: Double) {
        _session.update { currentSession ->
            currentSession ?: return@update null

            val updatedActions = currentSession.actions.mapIndexed { aIdx, action ->
                if (aIdx != actionIndex) return@mapIndexed action

                val updatedGroups = action.groups.mapIndexed { gIdx, group ->
                    if (gIdx != groupIndex) return@mapIndexed group
                    group.copy(weight = weight)
                }.toMutableList()

                action.copy(groups = updatedGroups)
            }.toMutableList()

            currentSession.copy(actions = updatedActions)
        }
    }

    fun updateGroupReps(actionIndex: Int, groupIndex: Int, reps: Int) {
        _session.update { currentSession ->
            currentSession ?: return@update null

            val updatedActions = currentSession.actions.mapIndexed { aIdx, action ->
                if (aIdx != actionIndex) return@mapIndexed action

                val updatedGroups = action.groups.mapIndexed { gIdx, group ->
                    if (gIdx != groupIndex) return@mapIndexed group
                    group.copy(reps = reps)
                }.toMutableList()
                action.copy(groups = updatedGroups)
            }.toMutableList()

            currentSession.copy(actions = updatedActions)
        }
    }

    // ============================================================
    // 7. 组操作（新增/复制/插入/删除）
    // ============================================================

    fun addGroupToAction(actionIndex: Int) {
        val session = _session.value ?: return
        val action = session.actions.getOrNull(actionIndex) ?: return

        val newGroupIndex = action.groups.size
        val newGroup = TrainingGroup(
            groupIndex = newGroupIndex,
            weight = 0.0,
            reps = 0,
            isCompleted = false
        )
        action.groups.add(newGroup)
        action.isCompleted = false

        _session.value = session.copy()
        _refreshTrigger.value++
    }

    fun copyGroup(actionIndex: Int, groupIndex: Int) {
        val session = _session.value ?: return
        val action = session.actions.getOrNull(actionIndex) ?: return
        val group = action.groups.getOrNull(groupIndex) ?: return

        val newGroup = TrainingGroup(
            groupIndex = groupIndex + 1,
            weight = group.weight,
            reps = group.reps,
            isCompleted = false
        )
        action.groups.add(groupIndex + 1, newGroup)
        action.groups.forEachIndexed { i, g -> g.groupIndex = i }
        action.isCompleted = false

        _session.value = session.copy()
        _refreshTrigger.value++
    }

    fun insertGroup(actionIndex: Int, groupIndex: Int) {
        val session = _session.value ?: return
        val action = session.actions.getOrNull(actionIndex) ?: return

        val newGroup = TrainingGroup(
            groupIndex = groupIndex,
            weight = 0.0,
            reps = 0,
            isCompleted = false
        )
        action.groups.add(groupIndex, newGroup)
        action.groups.forEachIndexed { i, g -> g.groupIndex = i }
        action.isCompleted = false

        _session.value = session.copy()
        _refreshTrigger.value++
    }

    fun deleteGroup(actionIndex: Int, groupIndex: Int) {
        val session = _session.value ?: return
        val action = session.actions.getOrNull(actionIndex) ?: return

        if (action.groups.size <= 1) return

        action.groups.removeAt(groupIndex)
        action.groups.forEachIndexed { i, g -> g.groupIndex = i }
        action.isCompleted = false

        _session.value = session.copy()
        _refreshTrigger.value++
    }

    // ============================================================
    // 组间歇倒计时（使用 Service）
    // ============================================================

    // 开始倒计时
    fun startRestTimer() {
        val seconds = defaultRestSeconds
        _restSeconds.value = seconds
        _remainingSeconds.value = seconds
        _showRestDialog.value = true
        _showRestFloating.value = false

        // 发送到服务
        val context = getApplication<Application>().applicationContext
        TrainingTimerService.updateRestTime(seconds)
        TrainingTimerService.startRest(context, seconds)

        startRestCountdown()
    }

    private fun startRestCountdown() {
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000L.milliseconds)
                _remainingSeconds.value = (_remainingSeconds.value - 1).coerceAtLeast(0)
            }
            _showRestDialog.value = false
            _showRestFloating.value = false
            restTimerJob = null
        }
    }

    fun adjustRestTime(delta: Int) {
        val newTotal = (_restSeconds.value + delta).coerceIn(10, 300)
        _restSeconds.value = newTotal
        val newRemaining = (_remainingSeconds.value + delta).coerceIn(0, newTotal)
        _remainingSeconds.value = newRemaining
        defaultRestSeconds = newTotal

        val context = getApplication<Application>().applicationContext
        TrainingTimerService.updateRestTime(newRemaining)
    }

    fun minimizeRestDialog() {
        _showRestDialog.value = false
        if (_remainingSeconds.value > 0) {
            _showRestFloating.value = true
        }
    }

    fun resumeRestDialog() {
        _showRestFloating.value = false
        _showRestDialog.value = true
    }

    fun skipRest() {
        restTimerJob?.cancel()
        _showRestDialog.value = false
        _showRestFloating.value = false

        val context = getApplication<Application>().applicationContext
        TrainingTimerService.stopRest(context)

        restTimerJob = null
    }

    // ============================================================
    // 8. 结束/取消训练
    // ============================================================

    fun finishSession() {
        stopTimer()

        val session = _session.value ?: return
        session.endTime = System.currentTimeMillis()
        session.status = 1

        viewModelScope.launch {
            val id = trainingRepository.saveSession(session)
            _savedSessionId.value = id
            _session.value = null
        }
    }

    fun cancelSession() {
        stopTimer()

        val session = _session.value ?: return
        session.endTime = System.currentTimeMillis()
        session.status = 2

        viewModelScope.launch {
            // 取消则不保存
//            trainingRepository.saveSession(session)
            _session.value = null
        }
    }

    fun clearSavedSessionId() {
        _savedSessionId.value = null
    }

    // ============================================================
    // 9. 获取进度
    // ============================================================

    fun getProgress(): Float {
        val session = _session.value ?: return 0f
        var total = 0
        var completed = 0
        session.actions.forEach { action ->
            total += action.groups.size
            completed += action.groups.count { it.isCompleted }
        }
        return if (total > 0) completed.toFloat() / total else 0f
    }

    // ============================================================
    // 悬浮窗相关
    // ============================================================

    fun updateFloatingPosition(x: Float, y: Float) {
        _floatingOffsetX.value = x
        _floatingOffsetY.value = y
    }

    fun resetFloatingPosition() {
        _floatingOffsetX.value = 0f
        _floatingOffsetY.value = 0f
    }

    // ✅ 从服务同步时间
    fun syncElapsedTime(time: Long) {
        _elapsedTime.value = time
    }

    // ✅ 从服务同步倒计时
    fun syncRestTime(time: Int) {
        if (_remainingSeconds.value != time) {
            _remainingSeconds.value = time
        }
    }

    // ============================================================
    // 10. 工厂
    // ============================================================

    companion object {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = FitApplication.instance.database
                return ExecutePlanViewModel(
                    application = FitApplication.instance,
                    trainingRepository = TrainingRepository(
                        sessionDao = database.trainingSessionDao(),
                        actionDao = database.trainingSessionActionDao(),
                        detailDao = database.trainingSessionActionDetailDao(),
                        planRepository = PlanRepository(
                            database.plansDao(),
                            database.planActionsDao(),
                            database.actionDetailsDao(),
                            database.planFullDao(),
                            database.actionLibDAO(),
                            database.muscleDao()
                        )
                    )
                ) as T
            }
        }
    }
}