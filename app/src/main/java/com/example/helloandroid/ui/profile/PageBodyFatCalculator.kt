package com.example.helloandroid.ui.profile

import android.R
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.helloandroid.utils.BodyFatCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageBodyFatCalculator(
    navController: NavHostController
) {
    // 测量方法选择
    var selectedMethod by remember { mutableStateOf(BodyFatCalculator.Method.THREE_POINT) }

    // 性别
    var gender by remember { mutableStateOf(BodyFatCalculator.Gender.MALE) }

    // 三点测量数据 (mm)
    var chest by remember { mutableStateOf("") }
    var abdominal by remember { mutableStateOf("") }
    var thigh by remember { mutableStateOf("") }

    // 七点测量数据 (mm)
    var chest7 by remember { mutableStateOf("") }
    var axillary by remember { mutableStateOf("") }
    var triceps by remember { mutableStateOf("") }
    var subscapular by remember { mutableStateOf("") }
    var abdominal7 by remember { mutableStateOf("") }
    var suprailiac by remember { mutableStateOf("") }
    var thigh7 by remember { mutableStateOf("") }

    // 年龄
    var age by remember { mutableStateOf("") }

    // 结果
    var result by remember { mutableStateOf<BodyFatCalculator.Result?>(null) }

    fun calculate() {
        val ageInt = age.toIntOrNull() ?: 30

        val calculatedResult: BodyFatCalculator.Result? = when (selectedMethod) {
            BodyFatCalculator.Method.THREE_POINT -> {
                val chestVal = chest.toDoubleOrNull()
                val abdominalVal = abdominal.toDoubleOrNull()
                val thighVal = thigh.toDoubleOrNull()
                if (chestVal != null && abdominalVal != null && thighVal != null) {
                    BodyFatCalculator.calculateThreePoint(
                        gender = gender,
                        age = ageInt,
                        chest = chestVal,
                        abdominal = abdominalVal,
                        thigh = thighVal
                    )
                } else {
                    null
                }
            }
            BodyFatCalculator.Method.SEVEN_POINT -> {
                val chestVal = chest7.toDoubleOrNull()
                val axillaryVal = axillary.toDoubleOrNull()
                val tricepsVal = triceps.toDoubleOrNull()
                val subscapularVal = subscapular.toDoubleOrNull()
                val abdominalVal = abdominal7.toDoubleOrNull()
                val suprailiacVal = suprailiac.toDoubleOrNull()
                val thighVal = thigh7.toDoubleOrNull()
                if (chestVal != null && axillaryVal != null && tricepsVal != null &&
                    subscapularVal != null && abdominalVal != null && suprailiacVal != null &&
                    thighVal != null) {
                    BodyFatCalculator.calculateSevenPoint(
                        gender = gender,
                        age = ageInt,
                        chest = chestVal,
                        axillary = axillaryVal,
                        triceps = tricepsVal,
                        subscapular = subscapularVal,
                        abdominal = abdominalVal,
                        suprailiac = suprailiacVal,
                        thigh = thighVal
                    )
                } else {
                    null
                }
            }
        }

        result = calculatedResult
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "体脂计算器",
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
                modifier = Modifier.height(48.dp),
                windowInsets = WindowInsets(0,0,0,0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,  // ✅ 透明背景
                    titleContentColor = MaterialTheme.colorScheme.onSurface,  // ✅ 文字颜色
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface  // ✅ 图标颜色
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
            // ✅ 说明卡片
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
                    Text(
                        text = "💡 使用说明",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "使用体脂钳测量皮肤褶皱厚度（mm），选择测量方法后计算体脂率",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "📐 三点测量：胸 + 腹 + 大腿",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "📐 七点测量：胸 + 腋窝 + 三头肌 + 肩胛下 + 腹 + 髂上 + 大腿",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ 性别选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GenderSexSelectButton(
                    label = "👨 男",
                    selected = gender == BodyFatCalculator.Gender.MALE,
                    onClick = { gender = BodyFatCalculator.Gender.MALE }
                )
                GenderSexSelectButton(
                    label = "👩 女",
                    selected = gender == BodyFatCalculator.Gender.FEMALE,
                    onClick = { gender = BodyFatCalculator.Gender.FEMALE }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ 年龄输入
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("年龄") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ 测量方法选择
            Text(
                text = "测量方法",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MethodButton(
                    label = "三点测量",
                    selected = selectedMethod == BodyFatCalculator.Method.THREE_POINT,
                    onClick = { selectedMethod = BodyFatCalculator.Method.THREE_POINT }
                )
                MethodButton(
                    label = "七点测量",
                    selected = selectedMethod == BodyFatCalculator.Method.SEVEN_POINT,
                    onClick = { selectedMethod = BodyFatCalculator.Method.SEVEN_POINT }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ 测量数据输入
            if (selectedMethod == BodyFatCalculator.Method.THREE_POINT) {
                // 三点测量
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📏 三点测量 (mm)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = chest,
                            onValueChange = { chest = it },
                            label = { Text("胸 (mm)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            )
                        )
                        OutlinedTextField(
                            value = abdominal,
                            onValueChange = { abdominal = it },
                            label = { Text("腹 (mm)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            )
                        )
                        OutlinedTextField(
                            value = thigh,
                            onValueChange = { thigh = it },
                            label = { Text("大腿 (mm)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            )
                        )
                    }
                }
            } else {
                // 七点测量
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📏 七点测量 (mm)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = chest7,
                            onValueChange = { chest7 = it },
                            label = { Text("胸 (mm)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            )
                        )
                        OutlinedTextField(
                            value = axillary,
                            onValueChange = { axillary = it },
                            label = { Text("腋窝 (mm)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            )
                        )
                        OutlinedTextField(
                            value = triceps,
                            onValueChange = { triceps = it },
                            label = { Text("三头肌 (mm)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                 focusedContainerColor = MaterialTheme.colorScheme.background,
                                 unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            )
                        )
                        OutlinedTextField(
                            value = subscapular,
                            onValueChange = { subscapular = it },
                            label = { Text("肩胛下 (mm)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            )
                        )
                        OutlinedTextField(
                            value = abdominal7,
                            onValueChange = { abdominal7 = it },
                            label = { Text("腹 (mm)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            )
                        )
                        OutlinedTextField(
                            value = suprailiac,
                            onValueChange = { suprailiac = it },
                            label = { Text("髂上 (mm)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            )
                        )
                        OutlinedTextField(
                            value = thigh7,
                            onValueChange = { thigh7 = it },
                            label = { Text("大腿 (mm)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ 计算按钮
            Button(
                onClick = { calculate() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = "计算",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("计算体脂率")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ 结果显示
            if (result != null) {
                ResultCard(result = result!!)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ✅ 性别按钮
@Composable
fun GenderSexSelectButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(40.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    ) {
        Text(label, fontSize = 14.sp)
    }
}

// ✅ 方法按钮
@Composable
fun MethodButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(36.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    ) {
        Text(label, fontSize = 13.sp)
    }
}

// ✅ 结果显示卡片
@Composable
fun ResultCard(result: BodyFatCalculator.Result) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📊 体脂率结果",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = String.format("%.1f%%", result.bodyFatPercentage),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.category,
                fontSize = 16.sp,
                color = result.categoryColor,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📐 方法: ${result.methodDisplay}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = "体脂质量: ${result.bodyFatMass}kg | 去脂体重: ${result.leanBodyMass}kg",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            // 体脂率指示条
            Spacer(modifier = Modifier.height(8.dp))
            BodyFatIndicator(
                percentage = result.bodyFatPercentage,
                gender = result.gender
            )
        }
    }
}

// ✅ 体脂率指示条
@Composable
fun BodyFatIndicator(
    percentage: Double,
    gender: BodyFatCalculator.Gender
) {
    val (color, text) = when {
        percentage < 6 -> Pair(MaterialTheme.colorScheme.errorContainer, "⚠️ 过低")
        percentage < 10 -> Pair(MaterialTheme.colorScheme.error, "偏低")
        percentage < 15 -> Pair(MaterialTheme.colorScheme.primary, "运动员水平")
        percentage < 18 -> Pair(MaterialTheme.colorScheme.primary, "健康")
        percentage < 22 -> Pair(MaterialTheme.colorScheme.primary, "健康")
        percentage < 25 -> Pair(MaterialTheme.colorScheme.secondary, "标准")
        percentage < 30 -> Pair(MaterialTheme.colorScheme.tertiary, "偏高")
        else -> Pair(MaterialTheme.colorScheme.error, "⚠️ 过高")
    }

    // 简化显示
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LinearProgressIndicator(
        progress = { (percentage / 40f).toFloat().coerceIn(0f, 1f) },
        modifier = Modifier
                        .weight(1f)
                        .height(12.dp),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}