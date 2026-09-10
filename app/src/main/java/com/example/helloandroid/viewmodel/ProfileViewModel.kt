package com.example.helloandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.helloandroid.FitApplication
import com.example.helloandroid.entity.UserEntity
import com.example.helloandroid.repository.PlanRepository
import com.example.helloandroid.repository.TrainingRepository
import com.example.helloandroid.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    // ✅ 训练总次数
    private val _totalSessions = MutableStateFlow(0)
    val totalSessions: StateFlow<Int> = _totalSessions.asStateFlow()

    // ✅ 训练天数
    private val _totalDays = MutableStateFlow(0)
    val totalDays: StateFlow<Int> = _totalDays.asStateFlow()

    fun loadUser() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // ✅ 获取或创建用户
                val currentUser = userRepository.getOrCreateCurrentUser()
                _user.value = currentUser

                // ✅ 获取训练总次数
                val sessions = trainingRepository.getAllSessionsOnce()
                _totalSessions.value = sessions.size
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isLoading.value = false
        }
    }

    /**
     * 保存身体数据
     */
    suspend fun saveBodyData(
        userId: Long,
        weight: String,
        height: String,
        age: String,
        gender: Int,
        bodyFat: String,
        muscleMass: String,
        chest: String,
        waist: String,
        hip: String,
        arm: String,
        leg: String
    ): Boolean {
        return try {
            _isSaving.value = true

            // 转换数据
            val weightDouble = weight.toDoubleOrNull() ?: 0.0
            val heightDouble = height.toDoubleOrNull() ?: 0.0
            val ageInt = age.toIntOrNull() ?: 0
            val bodyFatDouble = bodyFat.toDoubleOrNull() ?: 0.0
            val muscleMassDouble = muscleMass.toDoubleOrNull() ?: 0.0

            val chestDouble = chest.toDoubleOrNull() ?: 0.0
            val waistDouble = waist.toDoubleOrNull() ?: 0.0
            val hipDouble = hip.toDoubleOrNull() ?: 0.0
            val armDouble = arm.toDoubleOrNull() ?: 0.0
            val legDouble = leg.toDoubleOrNull() ?: 0.0

            // 计算 BMI
            val bmi = if (heightDouble > 0) {
                val heightM = heightDouble / 100
                weightDouble / (heightM * heightM)
            } else {
                0.0
            }

            val user = UserEntity(
                id = userId,
                nickname = _user.value?.nickname ?: "健身爱好者",
                uid = _user.value?.uid ?: "",
                weight = weightDouble,
                height = heightDouble,
                age = ageInt,
                gender = gender,
                chest = chestDouble,
                waist = waistDouble,
                hip = hipDouble,
                arm = armDouble,
                leg = legDouble,
                bodyFat = bodyFatDouble,
                muscleMass = muscleMassDouble,
                bmi = bmi,
                updatedAt = System.currentTimeMillis()
            )

            userRepository.updateUser(user)
            _user.value = user
            _saveSuccess.value = true
            _isSaving.value = false
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _isSaving.value = false
            false
        }
    }

    fun updateUser(
        nickname: String,
        gender: Int,
        city: String,
        avatar: String?
    ) {
        val currentUser = _user.value ?: return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val updatedUser = currentUser.copy(
                    nickname = nickname,
                    gender = gender,
                    city = city,
                    avatar = avatar,
                    updatedAt = System.currentTimeMillis()
                )
                userRepository.updateUser(updatedUser)

                _user.value = updatedUser
                _saveSuccess.value = true

                android.util.Log.d("ProfileViewModel", "保存成功")
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "保存失败", e)
                e.printStackTrace()
            }
            _isSaving.value = false
        }
    }

    /**
     * 加载训练统计
     */
    fun loadTrainingStats() {
        viewModelScope.launch {
            try {
                val sessions = trainingRepository.getAllSessionsOnce()
                _totalSessions.value = sessions.size

                // ✅ 统计不同的训练天数
                val uniqueDays = sessions.map { session ->
                    // 格式化为 yyyy-MM-dd
                    val calendar = java.util.Calendar.getInstance().apply {
                        timeInMillis = session.startTime
                    }
                    String.format(
                        "%04d-%02d-%02d",
                        calendar.get(java.util.Calendar.YEAR),
                        calendar.get(java.util.Calendar.MONTH) + 1,
                        calendar.get(java.util.Calendar.DAY_OF_MONTH)
                    )
                }.distinct()

                _totalDays.value = uniqueDays.size
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }

    companion object {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = FitApplication.instance.database
                return ProfileViewModel(
                    UserRepository(database.userDao()),
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