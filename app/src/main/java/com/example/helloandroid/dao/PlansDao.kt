package com.example.helloandroid.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.helloandroid.entity.PlansEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlansDao {
    // ========== 增 ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: PlansEntity): Long  // 返回插入的 id

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plans: List<PlansEntity>)

    // ========== 删 ==========
    @Delete
    suspend fun delete(plan: PlansEntity)

    @Query("DELETE FROM plans WHERE id = :planId")
    suspend fun deleteById(planId: Long)

    @Query("DELETE FROM plans")
    suspend fun deleteAll()

    // ========== 改 ==========
    @Update
    suspend fun update(plan: PlansEntity)

    // ========== 查 ==========
    // 获取所有计划（实时监听）
    @Query("SELECT * FROM plans")
    fun getAllPlans(): Flow<List<PlansEntity>>

    // 获取所有计划（一次性）
    @Query("SELECT * FROM plans")
    suspend fun getAllPlansOnce(): List<PlansEntity>

    // 根据 id 查询
    @Query("SELECT * FROM plans WHERE id = :planId")
    suspend fun getPlanById(planId: Long): PlansEntity?

    // 按名称模糊搜索
    @Query("SELECT * FROM plans WHERE name LIKE '%' || :keyword || '%'")
    suspend fun searchPlans(keyword: String): List<PlansEntity>
}