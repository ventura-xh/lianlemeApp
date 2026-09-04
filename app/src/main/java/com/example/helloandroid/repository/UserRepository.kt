package com.example.helloandroid.repository

import com.example.helloandroid.dao.UserDao
import com.example.helloandroid.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class UserRepository(
    private val userDao: UserDao
) {

    /**
     * 获取当前用户（如果不存在则创建默认用户）
     */
    suspend fun getOrCreateCurrentUser(): UserEntity {
        val existing = userDao.getCurrentUser()
        if (existing != null) {
            return existing
        }
        // 创建默认用户
        val defaultUser = UserEntity(
            nickname = "健身爱好者",
            uid = generateUid(),
            weight = 70.0,
            height = 175.0,
            age = 25,
            gender = 0
        )
        val id = userDao.insert(defaultUser)
        return defaultUser.copy(id = id)
    }

    /**
     * 获取当前用户（实时监听）
     */
    fun getCurrentUserFlow(): Flow<UserEntity?> {
        return userDao.getCurrentUserFlow()
    }

    /**
     * 更新用户信息
     */
    suspend fun updateUser(user: UserEntity) {
        userDao.update(user)
    }

    /**
     * 更新昵称
     */
    suspend fun updateNickname(userId: Long, nickname: String) {
        val user = userDao.getUserById(userId) ?: return
        userDao.update(user.copy(nickname = nickname, updatedAt = System.currentTimeMillis()))
    }

    /**
     * 更新身体数据
     */
    suspend fun updateBodyData(
        userId: Long,
        weight: Double? = null,
        height: Double? = null,
        age: Int? = null,
        gender: Int? = null,
        bodyFat: Double? = null,
        muscleMass: Double? = null
    ) {
        val user = userDao.getUserById(userId) ?: return
        val updated = user.copy(
            weight = weight ?: user.weight,
            height = height ?: user.height,
            age = age ?: user.age,
            gender = gender ?: user.gender,
            bodyFat = bodyFat ?: user.bodyFat,
            muscleMass = muscleMass ?: user.muscleMass,
            updatedAt = System.currentTimeMillis()
        )
        // 自动计算 BMI
        val bmi = if (updated.height > 0) {
            val heightM = updated.height / 100
            updated.weight / (heightM * heightM)
        } else {
            0.0
        }
        userDao.update(updated.copy(bmi = bmi))
    }

    /**
     * 更新围度数据
     */
    suspend fun updateMeasurements(
        userId: Long,
        chest: Double? = null,
        waist: Double? = null,
        hip: Double? = null,
        arm: Double? = null,
        leg: Double? = null
    ) {
        val user = userDao.getUserById(userId) ?: return
        userDao.update(
            user.copy(
                chest = chest ?: user.chest,
                waist = waist ?: user.waist,
                hip = hip ?: user.hip,
                arm = arm ?: user.arm,
                leg = leg ?: user.leg,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * 生成唯一 UID
     */
    private fun generateUid(): String {
        return "UID_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4).uppercase()}"
    }
}