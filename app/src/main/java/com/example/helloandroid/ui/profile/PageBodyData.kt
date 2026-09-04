package com.example.helloandroid.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.helloandroid.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageBodyData(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory
    )
) {
    val coroutineScope = rememberCoroutineScope()  // ✅ 添加协程作用域

    val user by viewModel.user.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()


    // 本地状态 - 基本信息
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(0) }
    var bodyFat by remember { mutableStateOf("") }
    var muscleMass by remember { mutableStateOf("") }
    var bmi by remember { mutableStateOf("") }

    // 本地状态 - 围度信息
    var chest by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var hip by remember { mutableStateOf("") }
    var arm by remember { mutableStateOf("") }
    var leg by remember { mutableStateOf("") }

    // 围度折叠状态
    var isMeasurementsExpanded by remember { mutableStateOf(false) }

    // ✅ 提示状态
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // 加载用户数据
    LaunchedEffect(Unit) {
        viewModel.loadUser()
    }

    // 当用户数据加载完成后，填充表单
    LaunchedEffect(user) {
        user?.let {
            weight = if (it.weight > 0) it.weight.toString() else ""
            height = if (it.height > 0) it.height.toString() else ""
            age = if (it.age > 0) it.age.toString() else ""
            gender = it.gender
            bodyFat = if (it.bodyFat > 0) it.bodyFat.toString() else ""
            muscleMass = if (it.muscleMass > 0) it.muscleMass.toString() else ""
            bmi = if (it.bmi > 0) String.format("%.1f", it.bmi) else ""
            chest = if (it.chest > 0) it.chest.toString() else ""
            waist = if (it.waist > 0) it.waist.toString() else ""
            hip = if (it.hip > 0) it.hip.toString() else ""
            arm = if (it.arm > 0) it.arm.toString() else ""
            leg = if (it.leg > 0) it.leg.toString() else ""
        }
    }

    // ✅ 保存成功后显示提示并返回
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            toastMessage = "保存成功"
            isError = false
            showToast = true
            viewModel.resetSaveSuccess()
            // 延迟返回
            kotlinx.coroutines.delay(800.milliseconds)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "身体数据",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            // ✅ 保存身体数据
                            coroutineScope.launch {
                                val userId = user?.id ?: return@launch
                                viewModel.saveBodyData(
                                    userId = userId,
                                    weight = weight,
                                    height = height,
                                    age = age,
                                    gender = gender,
                                    bodyFat = bodyFat,
                                    muscleMass = muscleMass,
                                    chest = chest,
                                    waist = waist,
                                    hip = hip,
                                    arm = arm,
                                    leg = leg
                                )
                            }

                        },
                        modifier = Modifier.size(width = 60.dp, height = 36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "保存",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                modifier = Modifier.height(48.dp),  // 默认约 64dp，48dp 更紧凑
                windowInsets = WindowInsets(0,0,0,0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ✅ 基本信息卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "基本信息",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // 性别选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GenderButton(
                            label = "未知",
                            selected = gender == 0,
                            onClick = { gender = 0 }
                        )
                        GenderButton(
                            label = "男",
                            selected = gender == 1,
                            onClick = { gender = 1 }
                        )
                        GenderButton(
                            label = "女",
                            selected = gender == 2,
                            onClick = { gender = 2 }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("体重 (kg)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)  // ✅ 小数键盘
                        )
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("身高 (cm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)  // ✅ 小数键盘
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("年龄") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)  // ✅ 小数键盘
                        )
                        OutlinedTextField(
                            value = bodyFat,
                            onValueChange = { bodyFat = it },
                            label = { Text("体脂率 (%)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)  // ✅ 小数键盘
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = muscleMass,
                            onValueChange = { muscleMass = it },
                            label = { Text("肌肉量 (kg)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)  // ✅ 小数键盘
                        )
                        OutlinedTextField(
                            value = bmi,
                            onValueChange = { bmi = it },
                            label = { Text("BMI") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = false  // BMI 自动计算
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ 围度卡片（默认折叠）
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 标题（可点击展开/折叠）
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isMeasurementsExpanded = !isMeasurementsExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "围度信息 (cm)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (isMeasurementsExpanded) {
                                Icons.Default.ArrowDropUp
                            } else {
                                Icons.Default.ArrowDropDown
                            },
                            contentDescription = if (isMeasurementsExpanded) "收起" else "展开"
                        )
                    }

                    // 围度内容（可折叠）
                    if (isMeasurementsExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = chest,
                                onValueChange = { chest = it },
                                label = { Text("胸围") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)  // ✅ 小数键盘
                            )
                            OutlinedTextField(
                                value = waist,
                                onValueChange = { waist = it },
                                label = { Text("腰围") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)  // ✅ 小数键盘
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = hip,
                                onValueChange = { hip = it },
                                label = { Text("臀围") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)  // ✅ 小数键盘
                            )
                            OutlinedTextField(
                                value = arm,
                                onValueChange = { arm = it },
                                label = { Text("臂围") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)  // ✅ 小数键盘
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = leg,
                                onValueChange = { leg = it },
                                label = { Text("腿围") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)  // ✅ 小数键盘
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ✅ Toast 提示（自定义简单提示）
    if (showToast) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = toastMessage,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ✅ 性别按钮
@Composable
fun GenderButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(label, fontSize = 14.sp)
    }
}