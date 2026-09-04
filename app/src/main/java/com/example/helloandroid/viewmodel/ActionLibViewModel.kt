package com.example.helloandroid.ui.actionlib

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.helloandroid.FitApplication
import com.example.helloandroid.dao.ActionLibDAO
import com.example.helloandroid.entity.ActionLibEntity
import com.example.helloandroid.entity.MuscleEntity
import com.example.helloandroid.repository.ActionLibRepository
import com.example.helloandroid.ui.common.InitState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActionLibViewModel(
    private val repository: ActionLibRepository
) : ViewModel() {

    // ============================================================
    // 1. 状态定义
    // ============================================================

    private val _initState = MutableStateFlow<InitState>(InitState.Idle)
    val initState: StateFlow<InitState> = _initState.asStateFlow()

    private val _actions = MutableStateFlow<List<ActionLibEntity>>(emptyList())
    val actions: StateFlow<List<ActionLibEntity>> = _actions.asStateFlow()

    private val _allMuscles = MutableStateFlow<List<MuscleEntity>>(emptyList())
    val allMuscles: StateFlow<List<MuscleEntity>> = _allMuscles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    // ============================================================
    // 2. 分组数据（按分类 → 肌肉分组）
    // ============================================================

    /**
     * 按分类和肌肉分组
     * Map<分类, Map<肌肉名称, List<动作>>>
     */
    private val _groupedActions = MutableStateFlow<Map<String, Map<String, List<ActionLibEntity>>>>(emptyMap())
    val groupedActions: StateFlow<Map<String, Map<String, List<ActionLibEntity>>>> = _groupedActions.asStateFlow()

    /**
     * 所有分类列表（用于侧边栏）
     */
    val categories: StateFlow<List<String>> = _groupedActions
        .map { it.keys.toList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ============================================================
    // 3. 数据加载
    // ============================================================

    fun loadActions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 加载动作列表
                val actionList = repository.getAllActionsOnce()
                _actions.value = actionList

                // 加载肌肉列表
                val muscles = repository.getAllMuscles()
                _allMuscles.value = muscles

                // 加载分组数据
                loadGroupedActions()

                _initState.value = InitState.Success
            } catch (e: Exception) {
                _initState.value = InitState.Error(e.message ?: "加载失败")
            }
            _isLoading.value = false
        }
    }

    /**
     * 加载分组数据
     */
    private suspend fun loadGroupedActions() {
        val grouped = repository.getGroupedActionsByCategoryAndMuscle()
        _groupedActions.value = grouped
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        viewModelScope.launch {
            loadActions()
            _refreshTrigger.value++
        }
    }

    // ============================================================
    // 4. 初始化预设动作
    // ============================================================

    fun initializeData(context: Context) {
        viewModelScope.launch {
            _initState.value = InitState.Loading
            try {
                repository.initializePresetActions(context)
                loadActions()
                _initState.value = InitState.Success
            } catch (e: Exception) {
                _initState.value = InitState.Error(e.message ?: "初始化失败")
            }
        }
    }

    /**
     * 手动重置预设动作（从 JSON 重新加载）
     * 用于设置页面中的"重置动作库"功能
     */
    fun resetPresetActions(context: Context) {
        viewModelScope.launch {
            _initState.value = InitState.Loading
            try {
                // 1. 清空所有预设动作（保留自定义动作）
                repository.clearPresetActions()

                // 2. 重新导入预设数据
                repository.initializePresetActions(context)

                // 3. 刷新数据
                loadActions()

                _initState.value = InitState.Success
            } catch (e: Exception) {
                _initState.value = InitState.Error(e.message ?: "重置失败")
            }
        }
    }

    // ============================================================
    // 5. 添加自定义动作
    // ============================================================

    suspend fun addCustomAction(
        name: String,
        category: String,
        muscleIds: List<Long>,
        description: String = "",
        tips: String = ""
    ) {
        repository.addCustomAction(
            name = name,
            category = category,
            muscleIds = muscleIds,
            description = description,
            tips = tips
        )
        // 重新加载数据
        loadActions()
        loadGroupedActions()
    }

    // ============================================================
    // 6. 查询方法
    // ============================================================

    /**
     * 获取指定分类下的所有子肌肉
     */
    fun getSubCategories(category: String): List<String> {
        return _groupedActions.value[category]?.keys?.toList() ?: emptyList()
    }

    /**
     * 获取指定分类和肌肉下的动作列表
     */
    fun getActionsByCategoryAndMuscle(category: String, muscle: String): List<ActionLibEntity> {
        return _groupedActions.value[category]?.get(muscle) ?: emptyList()
    }

    /**
     * 获取指定分类下的所有动作
     */
    fun getActionsByCategory(category: String): List<ActionLibEntity> {
        return _actions.value.filter { it.category == category }
    }

    /**
     * 获取动作的肌肉名称列表
     */
    suspend fun getMuscleNamesForAction(actionId: Long): List<String> {
        return repository.getMuscleNamesForAction(actionId)
    }

    // ============================================================
    // 7. 工厂
    // ============================================================

    companion object {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = FitApplication.instance.database
                return ActionLibViewModel(
                    ActionLibRepository(
                        actionLibDAO = database.actionDao(),
                        muscleDao = database.muscleDao(),
                        actionMuscleDao = database.actionMuscleDao()
                    )
                ) as T
            }
        }
    }
}