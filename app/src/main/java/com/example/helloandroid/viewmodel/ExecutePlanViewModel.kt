package com.example.helloandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.helloandroid.FitApplication
import com.example.helloandroid.entity.model.TrainingGroup
import com.example.helloandroid.entity.model.TrainingSession
import com.example.helloandroid.repository.PlanRepository
import com.example.helloandroid.repository.TrainingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class ExecutePlanViewModel(
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    private val _session = MutableStateFlow<TrainingSession?>(null)
    val session: StateFlow<TrainingSession?> = _session.asStateFlow()

    private val _savedSessionId = MutableStateFlow<Long?>(null)
    val savedSessionId: StateFlow<Long?> = _savedSessionId.asStateFlow()

    // ✅ 当前展开的动作卡片索引
    private val _currentActionIndex = MutableStateFlow(0)
    val currentActionIndex: StateFlow<Int> = _currentActionIndex.asStateFlow()

    private val _currentGroupIndex = MutableStateFlow(0)
    val currentGroupIndex: StateFlow<Int> = _currentGroupIndex.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(true)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    // ✅ 倒计时状态
    private val _showRestDialog = MutableStateFlow(false)
    val showRestDialog: StateFlow<Boolean> = _showRestDialog.asStateFlow()

    // ✅ 默认休息时间（用户可调整）
    private var defaultRestSeconds = 60

    private val _restSeconds = MutableStateFlow(60)  // ✅ 总休息时间
    val restSeconds: StateFlow<Int> = _restSeconds.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(60)  // ✅ 剩余时间
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

    // 主计时器
    private var timerJob: Job? = null

    // ============================================================
    // 1. 加载训练计划
    // ============================================================

    fun loadPlan(planId: Long, planName: String) {
        viewModelScope.launch {
            val trainingSession = trainingRepository.loadSessionFromPlan(planId, planName)
            _session.value = trainingSession
            // ✅ 默认展开第一个未完成的动作
            findFirstIncompleteAction()
        }
    }

    // ============================================================
    // 2. 计时器
    // ============================================================

    fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var seconds = 0L
            _elapsedTime.value = 0L
            _isTimerRunning.value = true
            while (_isTimerRunning.value) {
                delay(1000L.milliseconds)
                seconds++
                _elapsedTime.value = seconds
            }
        }
    }

    fun stopTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    // ============================================================
    // 3. 查找第一个未完成的动作
    // ============================================================

    // ✅ 查找下一个未完成的组
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
        // 所有动作都完成了
        if (session.actions.isNotEmpty()) {
            _currentActionIndex.value = session.actions.size - 1
        }
    }

    // ============================================================
    // 4. 切换动作完成状态（点击卡片标题的完成按钮）
    // ============================================================

    fun toggleActionCompleted(actionIndex: Int) {
        val session = _session.value ?: return
        val action = session.actions.getOrNull(actionIndex) ?: return

        // ✅ 切换动作完成状态
        action.isCompleted = !action.isCompleted

        // 如果动作被标记为完成，所有组也标记为完成
        if (action.isCompleted) {
            action.groups.forEach { it.isCompleted = true }
        } else {
            // 如果取消完成，所有组也取消完成
            action.groups.forEach { it.isCompleted = false }
        }

        _session.value = session.copy()
        _refreshTrigger.value++

        // ✅ 自动跳转到下一个未完成的动作
        findFirstIncompleteAction()
    }

    // ============================================================
    // 5. 切换组完成状态
    // ============================================================

    fun toggleGroupCompleted(actionIndex: Int, groupIndex: Int) {
        val session = _session.value ?: return
        val action = session.actions.getOrNull(actionIndex) ?: return
        val group = action.groups.getOrNull(groupIndex) ?: return

        // ✅ 如果组已经完成，直接取消完成状态，不触发倒计时
        if (group.isCompleted) {
            group.isCompleted = false
            action.isCompleted = false
            _session.value = session.copy()
            _refreshTrigger.value++
            return
        }

        // ✅ 切换组完成状态
        group.isCompleted = true
        group.completedAt = System.currentTimeMillis()

        // ✅ 更新动作完成状态：所有组完成则动作完成
        val allGroupsCompleted = action.groups.all { it.isCompleted }
        if (allGroupsCompleted) {
            action.isCompleted = true

            findFirstIncompleteAction()
        }

        _session.value = session.copy()
        _refreshTrigger.value++

        // ✅ 如果有下一组未完成，弹出倒计时
        val nextGroup = findNextUncompletedGroup()
        if (nextGroup != null) {
            // ✅ 只有在没有活跃的倒计时时才启动新的
            if (restTimerJob == null || restTimerJob?.isActive != true) {
                startRestTimer(60)
            }
        } else {
            // 所有组都完成了，结束训练
            // 不弹倒计时
        }
    }

    // ============================================================
    // 6. 更新组数据（重量/次数）
    // ============================================================

    fun updateGroupWeight(actionIndex: Int, groupIndex: Int, weight: String) {
        val session = _session.value ?: return
        val action = session.actions.getOrNull(actionIndex) ?: return
        val group = action.groups.getOrNull(groupIndex) ?: return

        group.weight = weight.toDoubleOrNull() ?: 0.0
        _session.value = session.copy()
    }

    fun updateGroupReps(actionIndex: Int, groupIndex: Int, reps: String) {
        val session = _session.value ?: return
        val action = session.actions.getOrNull(actionIndex) ?: return
        val group = action.groups.getOrNull(groupIndex) ?: return

        group.reps = reps.toIntOrNull() ?: 0
        _session.value = session.copy()
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
        // ✅ 如果动作已完成，新增组后取消完成状态
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
    // 组间歇倒计时
    // ============================================================

    fun startRestTimer(seconds: Int) {
        val seconds = defaultRestSeconds
        _restSeconds.value = seconds
        _remainingSeconds.value = seconds
        _showRestDialog.value = true
        _showRestFloating.value = false
        startRestCountdown()
    }

    private fun startRestCountdown() {
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            // ✅ 每次循环都读取最新的 _remainingSeconds.value
            while (_remainingSeconds.value > 0) {
                delay(1000L.milliseconds)
                // ✅ 从当前 _remainingSeconds.value 减1，而不是使用局部变量
                _remainingSeconds.value = (_remainingSeconds.value - 1).coerceAtLeast(0)
            }
            // 倒计时结束自动关闭
            _showRestDialog.value = false
            _showRestFloating.value = false
            restTimerJob = null
        }
    }

    fun adjustRestTime(delta: Int) {
        // ✅ 总时间调整
        val newTotal = (_restSeconds.value + delta).coerceIn(10, 300)
        _restSeconds.value = newTotal

        // ✅ 剩余时间也相应调整（在原有剩余基础上增加/减少）
        val newRemaining = (_remainingSeconds.value + delta).coerceIn(0, newTotal)
        _remainingSeconds.value = newRemaining

        defaultRestSeconds = newTotal
    }

    // ✅ 最小化对话框（显示悬浮窗，倒计时继续）
    fun minimizeRestDialog() {
        _showRestDialog.value = false
        if (_remainingSeconds.value > 0) {
            _showRestFloating.value = true
        }
        // ✅ 倒计时协程继续运行，不取消
    }

    // ✅ 恢复倒计时对话框（点击悬浮窗）
    fun resumeRestDialog() {
        _showRestFloating.value = false
        _showRestDialog.value = true
    }

    // ✅ 完成休息（从悬浮窗或对话框）
    fun skipRest() {
        restTimerJob?.cancel()
        _showRestDialog.value = false
        _showRestFloating.value = false
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

        var saveSessionId: Long? = null
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
            trainingRepository.saveSession(session)
            _session.value = null
        }
    }

    // ✅ 获取保存的 sessionId（供 UI 层使用）
    fun getSavedSessionId(): Long? {
        return _savedSessionId.value
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

    // ✅ 重置位置到右下角（可选）
    fun resetFloatingPosition() {
        _floatingOffsetX.value = 0f
        _floatingOffsetY.value = 0f
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
                    TrainingRepository(
                        sessionDao = database.trainingSessionDao(),
                        actionDao = database.trainingSessionActionDao(),
                        detailDao = database.trainingSessionActionDetailDao(),
                        planRepository = PlanRepository(
                            database.plansDao(),
                            database.planActionsDao(),
                            database.actionDetailsDao(),
                            database.planFullDao()
                        )
                    )
                ) as T
            }
        }
    }
}