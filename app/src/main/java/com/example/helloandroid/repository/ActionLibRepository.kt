package com.example.helloandroid.repository

import android.content.Context
import com.example.helloandroid.dao.ActionLibDAO
import com.example.helloandroid.dao.ActionMuscleDao
import com.example.helloandroid.dao.MuscleDao
import com.example.helloandroid.data.PresetDataLoader
import com.example.helloandroid.entity.ActionLibEntity
import com.example.helloandroid.entity.ActionMuscleEntity
import com.example.helloandroid.entity.MuscleEntity
import com.example.helloandroid.entity.model.ActionWithMuscles
import kotlinx.coroutines.flow.Flow
import androidx.core.content.edit

class ActionLibRepository(
    private val actionLibDAO: ActionLibDAO,
    private val muscleDao: MuscleDao,
    private val actionMuscleDao: ActionMuscleDao
) {
    private var isInitialized = false

    // ✅ 版本号存储 Key
    companion object {
        private const val PREF_NAME = "app_prefs"
        private const val KEY_PRESET_VERSION = "preset_version"
    }


    private fun getPresetVersion(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_PRESET_VERSION, 0)
    }

    private fun savePresetVersion(context: Context, version: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(KEY_PRESET_VERSION, version) }
    }

    // ============================================================
    // 1. 初始化 / 更新预设数据
    // ============================================================

    /**
     * 启动时检查并更新预设数据
     */
    suspend fun updatePresetActionsIfNeeded(context: Context) {
        val currentVersion = getPresetVersion(context)
        val latestVersion = PresetDataLoader.getPresetVersion(context)

        if (latestVersion > currentVersion) {
            // 有更新，执行增量更新
            val presetData = PresetDataLoader.loadPresetData(context)
            val existingActions = actionLibDAO.getAllOnce()
            updatePresetData(presetData, existingActions)
            savePresetVersion(context, latestVersion)
        }
    }

    /**
     * 初始化或更新预设数据
     * - 首次安装：导入所有预设数据
     * - 后续启动：只添加新动作，更新已有动作的描述，不删除任何数据
     */
    suspend fun initializePresetActions(context: Context) {
        if (isInitialized) return

        val savedVersion = getPresetVersion(context)
        val latestVersion = PresetDataLoader.getPresetVersion(context)

        // ✅ 版本相同且数据库有数据 → 跳过
        if (latestVersion <= savedVersion && actionLibDAO.getCount() > 0) {
            isInitialized = true
            return
        }

        val presetData = PresetDataLoader.loadPresetData(context)
        val existingActions = actionLibDAO.getAllOnce()

        // 首次安装
        if (existingActions.isEmpty()) {
            importAllPresetData(presetData)
            savePresetVersion(context, latestVersion)
            isInitialized = true
            return
        }

        // 版本更新
        if (latestVersion > savedVersion) {
            // 增量更新：不删除任何数据
            updatePresetData(presetData, existingActions)
            savePresetVersion(context, latestVersion)
        }

        isInitialized = true
    }

    // ============================================================
    // 2. 首次导入所有预设数据
    // ============================================================

    private suspend fun importAllPresetData(presetData: PresetDataLoader.PresetData) {
        // 1. 插入肌肉
        val muscleIds = mutableMapOf<String, Long>()
        presetData.muscles.forEach { muscle ->
            val id = muscleDao.insert(
                MuscleEntity(
                    name = muscle.name,
                    category = muscle.category,
                    icon = muscle.icon
                )
            )
            muscleIds[muscle.name] = id
        }

        // 2. 插入动作
        val actionIds = mutableMapOf<String, Long>()
        presetData.actions.forEach { action ->
            val id = actionLibDAO.insert(
                ActionLibEntity(
                    name = action.name,
                    isPreset = true,
                    category = action.category,
                    description = action.description,
                    tips = action.tips
                )
            )
            actionIds[action.name] = id
        }

        // 3. 插入关联
        val actionMuscles = mutableListOf<ActionMuscleEntity>()
        presetData.actions.forEach { action ->
            val actionId = actionIds[action.name] ?: return@forEach
            action.muscles.forEach { muscleName ->
                val muscleId = muscleIds[muscleName] ?: return@forEach
                actionMuscles.add(
                    ActionMuscleEntity(
                        actionId = actionId,
                        muscleId = muscleId
                    )
                )
            }
        }
        if (actionMuscles.isNotEmpty()) {
            actionMuscleDao.insertAll(actionMuscles)
        }
    }

    // ============================================================
    // 3. 增量更新预设数据（安全更新）
    // ============================================================

    private suspend fun updatePresetData(
        presetData: PresetDataLoader.PresetData,
        existingActions: List<ActionLibEntity>
    ) {
        // 构建现有动作名称索引
        val existingMap = existingActions.associateBy { it.name }
        val existingPresetNames = existingActions.filter { it.isPreset }.map { it.name }.toSet()

        // 获取所有肌肉
        val allMuscles = muscleDao.getAllMusclesOnce()
        val muscleMap = allMuscles.associateBy { it.name }

        // 处理每个预设动作
        presetData.actions.forEach { preset ->
            val existing = existingMap[preset.name]

            if (existing != null) {
                // ✅ 动作已存在：更新描述和提示（保持 ID 不变）
                if (existing.description != preset.description ||
                    existing.tips != preset.tips ||
                    existing.category != preset.category) {
                    actionLibDAO.update(
                        existing.copy(
                            category = preset.category,
                            description = preset.description,
                            tips = preset.tips
                        )
                    )
                }
                // 更新肌肉关联
                updateActionMuscles(existing.id, preset.muscles, muscleMap)
            } else {
                // ✅ 动作不存在：插入新动作
                insertNewPresetAction(preset, muscleMap)
            }
        }
    }

    // ============================================================
    // 4. 插入新的预设动作
    // ============================================================

    private suspend fun insertNewPresetAction(
        preset: PresetDataLoader.ActionPreset,
        muscleMap: Map<String, MuscleEntity>
    ) {
        // 插入动作
        val actionId = actionLibDAO.insert(
            ActionLibEntity(
                name = preset.name,
                isPreset = true,
                category = preset.category,
                description = preset.description,
                tips = preset.tips
            )
        )

        // 插入肌肉关联
        val actionMuscles = preset.muscles.mapNotNull { muscleName ->
            val muscle = muscleMap[muscleName]
            if (muscle != null) {
                ActionMuscleEntity(
                    actionId = actionId,
                    muscleId = muscle.id
                )
            } else {
                null
            }
        }
        if (actionMuscles.isNotEmpty()) {
            actionMuscleDao.insertAll(actionMuscles)
        }
    }

    // ============================================================
    // 5. 更新动作的肌肉关联
    // ============================================================

    private suspend fun updateActionMuscles(
        actionId: Long,
        muscleNames: List<String>,
        muscleMap: Map<String, MuscleEntity>
    ) {
        // 获取当前关联的肌肉
        val currentMuscleIds = actionLibDAO.getMuscleIdsForAction(actionId)
        val targetMuscleIds = muscleNames.mapNotNull { muscleMap[it]?.id }

        // 计算需要添加和删除的关联
        val toAdd = targetMuscleIds.filter { it !in currentMuscleIds }
        val toRemove = currentMuscleIds.filter { it !in targetMuscleIds }

        // 删除不再关联的肌肉
        if (toRemove.isNotEmpty()) {
            actionMuscleDao.deleteByActionAndMuscleIds(actionId, toRemove)
        }

        // 添加新的关联
        if (toAdd.isNotEmpty()) {
            val newAssociations = toAdd.map { muscleId ->
                ActionMuscleEntity(
                    actionId = actionId,
                    muscleId = muscleId
                )
            }
            actionMuscleDao.insertAll(newAssociations)
        }
    }

    // ============================================================
    // 6. 查询方法
    // ============================================================

    fun getAllActions(): Flow<List<ActionLibEntity>> = actionLibDAO.getAll()

    suspend fun getAllActionsOnce(): List<ActionLibEntity> = actionLibDAO.getAllOnce()

    suspend fun getActionById(actionId: Long): ActionLibEntity? {
        return actionLibDAO.getById(actionId)
    }

    suspend fun getByCategory(category: String): List<ActionLibEntity> {
        return actionLibDAO.getByCategory(category)
    }

    suspend fun getAllMuscles(): List<MuscleEntity> {
        return muscleDao.getAllMusclesOnce()
    }

    suspend fun getMuscleIdsForAction(actionId: Long): List<Long> {
        return actionLibDAO.getMuscleIdsForAction(actionId)
    }

    suspend fun getMuscleNamesForAction(actionId: Long): List<String> {
        val muscleIds = actionLibDAO.getMuscleIdsForAction(actionId)
        if (muscleIds.isEmpty()) return emptyList()
        val muscles = muscleDao.getMusclesByIds(muscleIds)
        return muscles.map { it.name }
    }

    suspend fun getActionsByMuscle(muscleId: Long): List<ActionLibEntity> {
        return actionLibDAO.getActionsByMuscleId(muscleId)
    }

    suspend fun getActionWithMuscles(actionId: Long): ActionWithMuscles? {
        val action = actionLibDAO.getById(actionId) ?: return null
        val muscleIds = actionLibDAO.getMuscleIdsForAction(actionId)
        return ActionWithMuscles(action = action, muscleIds = muscleIds)
    }

    suspend fun getAllActionsWithMuscles(): List<ActionWithMuscles> {
        val actions = actionLibDAO.getAllOnce()
        return actions.map { action ->
            val muscleIds = actionLibDAO.getMuscleIdsForAction(action.id)
            ActionWithMuscles(action = action, muscleIds = muscleIds)
        }
    }

    suspend fun getGroupedActionsByCategoryAndMuscle(): Map<String, Map<String, List<ActionLibEntity>>> {
        val allActions = actionLibDAO.getAllOnce()
        val result = mutableMapOf<String, MutableMap<String, MutableList<ActionLibEntity>>>()

        allActions.forEach { action ->
            val muscleNames = getMuscleNamesForAction(action.id)
            val categoryMap = result.getOrPut(action.category) { mutableMapOf() }

            if (muscleNames.isEmpty()) {
                val muscleList = categoryMap.getOrPut("未分类") { mutableListOf() }
                muscleList.add(action)
            } else {
                muscleNames.forEach { muscleName ->
                    val muscleList = categoryMap.getOrPut(muscleName) { mutableListOf() }
                    muscleList.add(action)
                }
            }
        }
        return result
    }

    // ============================================================
    // 7. 添加自定义动作
    // ============================================================

    suspend fun addCustomAction(
        name: String,
        category: String,
        description: String = "",
        tips: String = "",
        muscleIds: List<Long> = emptyList()
    ) {
        val action = ActionLibEntity(
            name = name,
            isPreset = false,
            category = category,
            description = description,
            tips = tips
        )
        val actionId = actionLibDAO.insert(action)

        if (muscleIds.isNotEmpty()) {
            val actionMuscles = muscleIds.map { muscleId ->
                ActionMuscleEntity(
                    actionId = actionId,
                    muscleId = muscleId
                )
            }
            actionMuscleDao.insertAll(actionMuscles)
        }
    }

    // ============================================================
    // 8. 删除（仅供自定义动作使用）
    // ============================================================

    suspend fun deleteCustomAction(actionId: Long): Int {
        // ✅ 只允许删除自定义动作
        return actionLibDAO.deleteCustomAction(actionId)
    }

    suspend fun deleteAllCustomActions(): Int {
        // ✅ 只删除自定义动作，保留预设动作
        return actionLibDAO.deleteAllCustomActions()
    }

    /**
     * 清空所有预设动作（保留自定义动作）
     * 用于手动重置
     */
    suspend fun clearPresetActions() {
        // 1. 删除预设动作的肌肉关联
        actionMuscleDao.deleteByPresetActions()

        // 2. 删除所有预设动作
        actionLibDAO.deleteAllPresetActions()

        // 注意：自定义动作（isPreset = false）不会被删除
    }

    /**
     * 重置预设动作（清空 + 重新导入）
     */
    suspend fun resetPresetActions(context: Context) {
        clearPresetActions()
        // 重置初始化标记，允许重新导入
        isInitialized = false
        initializePresetActions(context)
    }

    suspend fun clearAll() = actionLibDAO.deleteAll()
}