package com.example.helloandroid.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.helloandroid.entity.MuscleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MuscleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(muscle: MuscleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(muscles: List<MuscleEntity>)

    @Query("SELECT * FROM muscles ORDER BY category, name")
    fun getAllMuscles(): Flow<List<MuscleEntity>>

    @Query("SELECT * FROM muscles ORDER BY category, name")
    suspend fun getAllMusclesOnce(): List<MuscleEntity>

    @Query("SELECT * FROM muscles WHERE id = :muscleId")
    suspend fun getMuscleById(muscleId: Long): MuscleEntity?

    @Query("SELECT * FROM muscles WHERE category = :category")
    suspend fun getMusclesByCategory(category: String): List<MuscleEntity>

    @Query("SELECT * FROM muscles WHERE id IN (:muscleIds)")
    suspend fun getMusclesByIds(muscleIds: List<Long>): List<MuscleEntity>
}