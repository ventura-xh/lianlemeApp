package com.example.helloandroid.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.helloandroid.entity.ActionLibEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionLibDAO {
    @Insert
    suspend fun insert(action: ActionLibEntity): Long // 返回插入行的 ID

    @Insert
    suspend fun insertAll(actions: List<ActionLibEntity>)

    // 2. 更新
    @Update
    suspend fun update(action: ActionLibEntity)

    // 3. 删除
    @Delete
    suspend fun delete(action: ActionLibEntity)

    // ✅ 获取动作总数
    @Query("SELECT COUNT(*) FROM action_lib")
    suspend fun getCount(): Int

    // 4. 查询所有（按 ID 倒序，最新的在前）
    @Query("SELECT * FROM action_lib ORDER BY id DESC")
    fun getAll(): Flow<List<ActionLibEntity>> // Flow 可以监听数据变化，推荐

    // 或者返回 List（一次性查询，不监听变化）
    @Query("SELECT * FROM action_lib ORDER BY id DESC")
    suspend fun getAllOnce(): List<ActionLibEntity>

    // 5. 根据 ID 查询单个
    @Query("SELECT * FROM action_lib WHERE id = :actionId")
    suspend fun getById(actionId: Long): ActionLibEntity? // 可能不存在，返回可空

    @Query("SELECT * FROM ACTION_LIB WHERE id IN (:actionIds)")
    suspend fun getByIds(actionIds: List<Long>): List<ActionLibEntity>

    // 6. 按名称模糊查询
    @Query("SELECT * FROM action_lib WHERE name LIKE '%' || :keyword || '%'")
    suspend fun searchByName(keyword: String): List<ActionLibEntity>

    // 7. 根据 isPreset 筛选
    @Query("SELECT * FROM action_lib WHERE isPreset = :isPreset")
    suspend fun getByPreset(isPreset: Boolean): List<ActionLibEntity>

    // 8. 按分类查询
    @Query("SELECT * FROM action_lib WHERE category = :category")
    suspend fun getByCategory(category: String): List<ActionLibEntity>

    // ✅ 根据肌肉ID获取关联的动作
    @Query("""
        SELECT action_lib.* FROM action_lib
        INNER JOIN action_muscles ON action_lib.id = action_muscles.actionId
        WHERE action_muscles.muscleId = :muscleId
        ORDER BY action_lib.name ASC
    """)
    suspend fun getActionsByMuscleId(muscleId: Long): List<ActionLibEntity>

    // ✅ 获取动作及其关联的肌肉ID
    @Query("SELECT muscleId FROM action_muscles WHERE actionId = :actionId")
    suspend fun getMuscleIdsForAction(actionId: Long): List<Long>

    // 9. 组合查询：获取某个分类下的预设动作
    @Query("SELECT * FROM action_lib WHERE category = :category AND isPreset = :isPreset")
    suspend fun getByCategoryAndPreset(category: String, isPreset: Boolean): List<ActionLibEntity>

    // 10. 删除所有
    @Query("DELETE FROM action_lib")
    suspend fun deleteAll()

    // ✅ 只删除自定义动作
    @Query("DELETE FROM action_lib WHERE isPreset = 0")
    suspend fun deleteAllCustomActions(): Int

    @Query("DELETE FROM action_lib WHERE id = :actionId AND isPreset = 0")
    suspend fun deleteCustomAction(actionId: Long): Int

    // ✅ 删除所有预设动作（保留自定义动作）
    @Query("DELETE FROM action_lib WHERE isPreset = 1")
    suspend fun deleteAllPresetActions()

    // ✅ 获取预设动作数量
    @Query("SELECT COUNT(*) FROM action_lib WHERE isPreset = 1")
    suspend fun getPresetCount(): Int
}