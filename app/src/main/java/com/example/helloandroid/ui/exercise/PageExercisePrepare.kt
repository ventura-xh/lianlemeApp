package com.example.helloandroid.ui.exercise

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.helloandroid.navigation.Screen
import com.example.helloandroid.ui.plan.PlanActionWithGroups
import com.example.helloandroid.ui.plan.PlanDetail
import com.example.helloandroid.viewmodel.PlanDetailViewModel
import com.example.helloandroid.viewmodel.TrainingPlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageExercisePrepare(
    planId: Long,
    navController: NavHostController,
    viewModel: PlanDetailViewModel = viewModel(
        factory = PlanDetailViewModel.factory
    )
) {
    var planDetail by remember { mutableStateOf<PlanDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // ✅ 切换计划弹窗状态
    var showPlanListBottomSheet by remember { mutableStateOf(false) }
    var currentPlanId by remember { mutableStateOf(planId) }

    // ✅ 是否有错误
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // ✅ 加载当前计划数据
    LaunchedEffect(currentPlanId) {
        isLoading = true
        hasError = false
        try {
            planDetail = viewModel.getPlanDetail(currentPlanId)
            if (planDetail == null) {
                hasError = true
                errorMessage = "未找到该计划"
            }
        } catch (e: Exception) {
            hasError = true
            errorMessage = e.message ?: "加载失败"
        }
        isLoading = false
    }

    // ✅ 训练计划列表 ViewModel（用于切换计划）
    val trainingPlanViewModel: TrainingPlanViewModel = viewModel(
        factory = TrainingPlanViewModel.factory
    )
    val allPlans by trainingPlanViewModel.plans.collectAsStateWithLifecycle(initialValue = emptyList()
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // ✅ 计划名称改为按钮，点击弹出 BottomSheet
                    Button(
                        onClick = {
                            showPlanListBottomSheet = true
                        },
                        modifier = Modifier
                            .height(40.dp)
                            .padding(horizontal = 8.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(
                            text = if (isLoading) "加载中..." else (planDetail?.plan?.name ?: "选择计划"),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "切换计划",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                navigationIcon = {
                    // ✅ 返回按钮
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                modifier = Modifier.height(48.dp),  // 默认约 64dp，48dp 更紧凑
                windowInsets = WindowInsets(0,0,0,0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = Color.Unspecified
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 动作列表
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(4f)
                    .padding(16.dp)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("加载中...")
                        }
                    }
                    planDetail?.actions.isNullOrEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "该计划暂无动作",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(planDetail?.actions ?: emptyList()) { item ->
                                ExercisePrepareActionCard(
                                    actionWithGroups = item
                                )
                            }
                        }
                    }
                }
            }

            // ✅ 下方 1/5 区域：Go 按钮 + 装备按钮
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically  // ✅ 对齐到底部
                ) {
                    // Go 按钮
                    Button(
                        onClick = {
                            // ✅ 跳转到训练执行页面
                            val planId = currentPlanId
                            val planName = planDetail?.plan?.name ?: "训练计划"
                            // ✅ 保存 planName 到 SavedStateHandle
                            navController.currentBackStackEntry?.savedStateHandle?.set("planName", planName)
                            navController.navigate(Screen.ExecutePlan.pass(planId))
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .padding(end = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "开始训练",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("开始训练")
                    }
                }

                // ✅ 装备按钮：使用 Box 的 align 单独定位在右侧
                Button(
                    onClick = { /* TODO: 打开装备设置 */ },
                    modifier = Modifier
                        .height(48.dp)
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "装备",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("装备")
                }

            }
        }
    }

    if (showPlanListBottomSheet) {
        PlanListBottomSheet(
            plans = allPlans,
            currentPlanId = currentPlanId,
            onPlanSelected = { selectedPlanId ->
                currentPlanId = selectedPlanId
                showPlanListBottomSheet = false
            },
            onDismiss = { showPlanListBottomSheet = false}
        )
    }
}

// ✅ 训练准备页面的动作卡片（精简版，只显示动作名和组数）
@Composable
fun ExercisePrepareActionCard(
    actionWithGroups: PlanActionWithGroups
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 标题行：动作名称 + 组数
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = actionWithGroups.action.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${actionWithGroups.groups.size}组",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 组数据
            actionWithGroups.groups.forEachIndexed { index, group ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "第${index + 1}组",
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${group.weight}kg",
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${group.reps}次",
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}