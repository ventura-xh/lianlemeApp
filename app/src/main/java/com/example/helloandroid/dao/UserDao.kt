package com.example.helloandroid.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.helloandroid.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // ========== 增 ==========
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long

    // ========== 改 ==========
    @Update
    suspend fun update(user: UserEntity)

    // ========== 查 ==========
    @Query("SELECT * FROM users ORDER BY id LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("SELECT * FROM users ORDER BY id LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

    // ========== 删 ==========
    @Query("DELETE FROM users")
    suspend fun deleteAll()
}