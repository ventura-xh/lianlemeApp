package com.example.helloandroid.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.helloandroid.entity.model.TrainingAction
import com.example.helloandroid.entity.model.TrainingGroup
import com.example.helloandroid.navigation.Screen
import com.example.helloandroid.ui.common.NumberInputBottomSheet
import com.example.helloandroid.viewmodel.ExecutePlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageExecutePlan(
    planId: Long,
    planName: String = "",
    navController: NavHostController,
    viewModel: ExecutePlanViewModel = viewModel(
        factory = ExecutePlanViewModel.factory
    )
) {
    // ✅ 如果 planName 为空，从 SavedStateHandle 获取
    var finalPlanName by remember { mutableStateOf(planName) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // 1. 优先使用传入的 planName
        var name = planName

        // 2. 如果为空，从 SavedStateHandle 获取
        if (name.isEmpty()) {
            val savedName = navController.currentBackStackEntry?.savedStateHandle?.get<String>("planName")
            if (!savedName.isNullOrEmpty()) {
                name = savedName
            }
        }

        // 3. 如果还是为空，从 session 获取（在 loadPlan 后）
        finalPlanName = name
        viewModel.loadPlan(planId, name)
        viewModel.startTimer()
        isLoading = false
    }

    val session by viewModel.session.collectAsStateWithLifecycle()
    val currentActionIndex by viewModel.currentActionIndex.collectAsStateWithLifecycle()
    val currentGroupIndex by viewModel.currentGroupIndex.collectAsStateWithLifecycle()  // ✅ 添加这行
    val elapsedTime by viewModel.elapsedTime.collectAsStateWithLifecycle()
    val refreshTrigger by viewModel.refreshTrigger.collectAsStateWithLifecycle()  // ✅ 订阅刷新触发器

    // ✅ 获取当前动作信息
    val currentAction = session?.actions?.getOrNull(currentActionIndex)
    val currentActionName = currentAction?.actionName ?: ""
    val currentGroup = currentAction?.groups?.getOrNull(currentGroupIndex)
    val currentGroupIndexDisplay = currentGroup?.groupIndex ?: 0
    val totalGroupsInAction = currentAction?.groups?.size ?: 0

    // ✅ 倒计时状态
    val showRestDialog by viewModel.showRestDialog.collectAsStateWithLifecycle()
    val showRestFloating by viewModel.showRestFloating.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
    val restSeconds by viewModel.restSeconds.collectAsStateWithLifecycle()

    // ✅ 数字输入对话框状态
    var showNumberInput by remember { mutableStateOf(false) }
    var editingActionIndex by remember { mutableStateOf(0) }
    var editingGroupIndex by remember { mutableStateOf(0) }
    var editingGroup by remember { mutableStateOf<TrainingGroup?>(null) }
    var editingField by remember { mutableStateOf(0) } // 0=重量, 1=次数
    var inputPanelHeight by remember { mutableStateOf(300.dp) }

    // ✅ 当 refreshTrigger 变化时，什么都不做，但会触发重组
    val refreshKey = refreshTrigger

    // 训练结果id
    val savedSessionId by viewModel.savedSessionId.collectAsStateWithLifecycle()

    // ✅ 如果 session 中有计划名称，更新显示
    LaunchedEffect(session) {
        session?.let {
            if (it.planName.isNotEmpty()) {
                finalPlanName = it.planName
            }
        }
    }

    // ✅ 监听保存结果，跳转到结果页面
    LaunchedEffect(savedSessionId) {
        savedSessionId?.let { sessionId ->
            navController.navigate(Screen.TrainingResult.pass(sessionId, true)) {
                // 清除当前页面（ExecutePlan）和训练准备页面
                popUpTo(Screen.ExercisePrepare.route) {
                    inclusive = true  // 包含训练准备页面
                }
                launchSingleTop = true
            }
            // 清除状态，避免重复跳转
            viewModel.clearSavedSessionId()

        }
    }

    Scaffold(
        topBar = {
            ExecuteTopAppBar(
                elapsedTime = elapsedTime,
                planName = finalPlanName,
                onFinish = {
                    viewModel.finishSession()
                },
                onBack = {
                    // TODO: 显示确认退出对话框
                    navController.popBackStack()
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // ✅ 第二行：计划名称
            Text(
                text = "📋${finalPlanName}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // ✅ 进度条（可选）
            val progress = viewModel.getProgress()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "进度: ${(progress * 100).toInt()}%",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${session?.actions?.sumOf { it.groups.size } ?: 0} 组",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            // ✅ 进度条
            LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ 动作卡片列表
            if (session == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("加载中...")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(
                        items = session?.actions ?: emptyList(),
                        key = { _, action -> action.actionId }  // ✅ 使用稳定的 key
                    ) { index, action ->
                        // ✅ 使用 refreshKey 触发重组
                        ExecuteActionCard(
                            action = action,
                            actionIndex = index,
                            isExpanded = index == currentActionIndex,
                            onToggleGroupCompleted = { actionIndex, groupIndex ->
                                viewModel.toggleGroupCompleted(actionIndex, groupIndex)
                            },
                            onAddGroup = { actionIndex ->
                                viewModel.addGroupToAction(actionIndex)
                            },
                            onCopyGroup = { actionIndex, groupIndex ->
                                viewModel.copyGroup(actionIndex, groupIndex)
                            },
                            onInsertGroup = { actionIndex, groupIndex ->
                                viewModel.insertGroup(actionIndex, groupIndex)
                            },
                            onDeleteGroup = { actionIndex, groupIndex ->
                                viewModel.deleteGroup(actionIndex, groupIndex)
                            },
                            refreshKey = refreshKey,  // ✅ 传入刷新key
                            onEditWeight = { groupIndex, group ->
                                editingActionIndex = index
                                editingGroupIndex = groupIndex
                                editingGroup = group
                                editingField = 0
                                showNumberInput = true
                            },
                            onEditReps = { groupIndex, group ->
                                editingActionIndex = index
                                editingGroupIndex = groupIndex
                                editingGroup = group
                                editingField = 1
                                showNumberInput = true
                            }
                        )
                    }
                }
            }
        }
    }

    // ✅ 数字输入对话框
    if (showNumberInput && editingGroup != null) {
        // ✅ 使用本地状态管理输入过程中的临时值
        var tempInputValue by remember(editingGroup, editingField) {
            mutableStateOf(
                if (editingField == 0) editingGroup!!.weight.toString()
                else editingGroup!!.reps.toString()
            )
        }


        NumberInputBottomSheet(
            currentValue = tempInputValue,
            // ✅ 仅更新本地状态，不触发 ViewModel
            onValueChange = { newValue ->
                tempInputValue = newValue
            },

            // ✅ 只在关闭/确认时，才将最终值写入 ViewModel
            onDismiss = {
                // 解析并保存最终值
                if (editingField == 0) {
                    val weight = tempInputValue.toDoubleOrNull() ?: editingGroup!!.weight
                    viewModel.updateGroupWeight(editingActionIndex, editingGroupIndex, weight)
                } else {
                    val reps = tempInputValue.toIntOrNull() ?: editingGroup!!.reps
                    viewModel.updateGroupReps(editingActionIndex, editingGroupIndex, reps)
                }

                showNumberInput = false
                editingGroup = null
                inputPanelHeight = 0.dp
            },
            onHeightMeasured = { height ->
                inputPanelHeight = height
            }
        )
    }

    // ✅ 倒计时对话框
// ✅ 倒计时对话框
    if (showRestDialog) {
        RestDialog(
            remainingSeconds = remainingSeconds,
            totalSeconds = restSeconds,
            currentActionName = currentActionName,
            currentGroupIndex = currentGroupIndexDisplay,
            totalGroups = totalGroupsInAction,
            onAdjust = { delta ->
                viewModel.adjustRestTime(delta)
            },
            onSkip = {
                viewModel.skipRest()
            },
            onMinimize = {
                viewModel.minimizeRestDialog()
            }
        )
    }

    // ✅ 倒计时悬浮窗
    if (showRestFloating) {
        RestFloatingWidget(
            remainingSeconds = remainingSeconds,
            viewModel = viewModel,
            onResume = {
                // ✅ 点击悬浮窗，恢复对话框
                viewModel.resumeRestDialog()
            }
        )
    }
}

// ✅ 顶部标题栏
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecuteTopAppBar(
    elapsedTime: Long,
    planName: String,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "计时器",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = formatTime(elapsedTime),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "返回"
                )
            }
        },
        actions = {
            Button(
                onClick = onFinish,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "完成",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("完成")
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

// ✅ 动作卡片
@Composable
fun ExecuteActionCard(
    action: TrainingAction,
    actionIndex: Int,
    isExpanded: Boolean,
    refreshKey: Int = 0,
    onToggleGroupCompleted: (Int, Int) -> Unit,
    onAddGroup: (Int) -> Unit,
    onCopyGroup: (Int, Int) -> Unit,
    onInsertGroup: (Int, Int) -> Unit,
    onDeleteGroup: (Int, Int) -> Unit,
    onEditWeight: (Int, TrainingGroup) -> Unit,   // ✅ 新增
    onEditReps: (Int, TrainingGroup) -> Unit      // ✅ 新增
) {
    var expanded by remember { mutableStateOf(isExpanded) }

    // ✅ 当 refreshKey 变化时，更新展开状态（触发重组）
    LaunchedEffect(refreshKey) {
        // 什么都不做，只是让这个 Composable 重组
    }

    LaunchedEffect(isExpanded) {
        expanded = isExpanded
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (action.isCompleted) 1.dp else 3.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (action.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 标题行 - 点击展开/收起
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (action.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "已完成",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = action.actionName,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (action.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = "(${action.groups.size}组)",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (expanded) "▲" else "▼",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ✅ 展开内容
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))

                // ✅ 表头 - 只显示一次
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 序号列 - 对应组项序号 Text (width 24dp)
                    Text(
                        text = "序号",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.Center
                    )
                    // 重量列 - 对应组项重量输入框 (width 80dp)
                    Text(
                        text = "重量",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(80.dp),
                        textAlign = TextAlign.Center
                    )
                    // 次数列 - 对应组项次数输入框 (width 80dp)
                    Text(
                        text = "次数",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(80.dp),
                        textAlign = TextAlign.Center
                    )
                    // 完成列 - 对应组项完成按钮 (width 40dp)
                    Text(
                        text = "完成",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.Center
                    )
                    // 更多列 - 对应 Spacer(8dp) + 更多按钮 (width 28dp) + 左侧内边距
                    Text(
                        text = "",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp)  // 8dp 间隔 + 28dp 按钮
                    )
                }

                // ✅ 组列表 - 使用 key 强制每个组独立刷新
                for (index in action.groups.indices) {
                    val group = action.groups[index]
                    key(group) {  // ✅ 使用 key 让 Compose 跟踪每个组的变化
                        ExecuteGroupItem(
                            group = group,
                            groupIndex = index,
                            totalGroups = action.groups.size,
                            isCompleted = group.isCompleted,
                            onToggleCompleted = {
                                onToggleGroupCompleted(actionIndex, index)
                            },
                            onCopy = { onCopyGroup(actionIndex, index) },
                            onInsert = { onInsertGroup(actionIndex, index) },
                            onDelete = { onDeleteGroup(actionIndex, index) },
                            onEditWeight = { onEditWeight(index, group) },  // ✅ 传递
                            onEditReps = { onEditReps(index, group) }       // ✅ 传递
                        )
                    }
                }

                // 新增一组按钮
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onAddGroup(actionIndex) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "新增一组", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("新增一组", fontSize = 13.sp)
                }
            }
        }
    }
}

// ✅ 单组项
// ExecuteGroupItem.kt

@Composable
fun ExecuteGroupItem(
    group: TrainingGroup,
    groupIndex: Int,
    totalGroups: Int,
    isCompleted: Boolean,
    onToggleCompleted: () -> Unit,
    onCopy: () -> Unit,
    onInsert: () -> Unit,
    onDelete: () -> Unit,
    onEditWeight: () -> Unit,   // ✅ 新增
    onEditReps: () -> Unit      // ✅ 新增
) {
    var showMenu by remember { mutableStateOf(false) }
    val weightNum = group.weight
    val repsNum = group.reps

    val rowBackgroundColor = if (isCompleted) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(
                color = rowBackgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 组序号
        Text(
            text = "${groupIndex + 1}",
            fontSize = 13.sp,
            fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Bold,
            modifier = Modifier.width(24.dp),
            color = if (isCompleted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        // ✅ 重量显示 - 点击弹出数字输入
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(44.dp)
                .clickable { onEditWeight() }
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (weightNum > 0) "${weightNum}kg" else "0",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (weightNum > 0) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        // ✅ 次数显示 - 点击弹出数字输入
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(44.dp)
                .clickable { onEditReps() }
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (repsNum > 0) "${repsNum}次" else "0",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (repsNum > 0) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        // 完成按钮
        Button(
            onClick = onToggleCompleted,
            modifier = Modifier
                .width(40.dp)
                .height(32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "√",
                fontSize = if (isCompleted) 14.sp else 9.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.width(80.dp))

        // 更多按钮
        Box(
            modifier = Modifier
            .width(32.dp)  // ✅ 固定宽度
            .height(44.dp)  // ✅ 固定高度，与输入框对齐
            .wrapContentSize(Alignment.Center)  // ✅ 内容居中
        ) {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "更多",
                    tint = if (isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.width(180.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("复制组", fontSize = 14.sp) },
                    onClick = {
                        showMenu = false
                        onCopy()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                )
                DropdownMenuItem(
                    text = { Text("插入组", fontSize = 14.sp) },
                    onClick = {
                        showMenu = false
                        onInsert()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.InsertDriveFile,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("删除组", fontSize = 14.sp) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    enabled = totalGroups > 1
                )
            }
        }
    }
}

// ✅ 格式化时间
fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}