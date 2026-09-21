package com.example.helloandroid.ui.plan

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.helloandroid.FitApplication
import com.example.helloandroid.entity.model.ActionGroup
import com.example.helloandroid.navigation.Screen
import com.example.helloandroid.repository.ActionLibRepository
import com.example.helloandroid.ui.actionlib.ActionLibViewModel
import com.example.helloandroid.ui.common.NumberInputBottomSheet
import com.example.helloandroid.viewmodel.EditPlanViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageEditPlan(
    planId: Long,
    navController: NavHostController,
    viewModel: EditPlanViewModel = viewModel(
        factory = EditPlanViewModel.factory(planId)
    )
) {
    val coroutineScope = rememberCoroutineScope()
    val selectedActions by viewModel.selectedActions.collectAsStateWithLifecycle()
    val planName by viewModel.planName.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    // ✅ 保存状态
    var showSaveDialog by remember { mutableStateOf(false) }
    var planNameInput by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var showSaveError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var draggingIndex by remember { mutableStateOf(-1) }

    val lazyListState = rememberLazyListState()

    // ✅ 数字输入对话框状态
    var showNumberInput by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<ActionGroup?>(null) }
    var editingField by remember { mutableStateOf(0) } // 0=重量, 1=次数
    var editingActionId by remember { mutableStateOf(0L) }
    var editingGroupId by remember { mutableStateOf("") }
    var inputPanelHeight by remember { mutableStateOf(200.dp) }

    // ✅ 获取动作名称（用于显示）
    val actionLibViewModel: ActionLibViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ActionLibViewModel(
                    ActionLibRepository(
                        FitApplication.instance.database.actionLibDAO(),
                        FitApplication.instance.database.muscleDao(),
                        FitApplication.instance.database.actionMuscleDao()
                    )
                ) as T
            }
        }
    )
    val allActions by actionLibViewModel.actions.collectAsStateWithLifecycle(initialValue = emptyList())

    // ✅ 监听从选择动作页面返回的数据
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle
            ?.getLiveData<String>("selected_action_ids")  // ✅ 复数 key，String
            ?.observeForever { idsString ->
                idsString?.let {
                    val actionIds = it.split(",").mapNotNull { id -> id.toLongOrNull() }
                    if (actionIds.isNotEmpty()) {
                        coroutineScope.launch {
                            actionIds.forEach { actionId ->
                                viewModel.addAction(actionId)
                            }
                        }
                    }
                    navController.currentBackStackEntry?.savedStateHandle
                        ?.remove<String>("selected_action_ids")
                }
            }
    }

    LaunchedEffect(showNumberInput, editingGroupId) {
        if (showNumberInput && editingGroupId.isNotEmpty()) {
            // 找到当前编辑的动作在列表中的索引
            val index = selectedActions.indexOfFirst { action ->
                action.groups.any { it.id == editingGroupId }
            }
            if (index >= 0) {
                // 滚动到该项，并留出顶部空间避免被 TopAppBar 遮挡
                lazyListState.animateScrollToItem(
                    index = index,
                    scrollOffset = -200 // 负值表示该项出现在视口上方
                )
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isLoading) "加载中..." else "编辑计划",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedActions.isNotEmpty()) {
                                // TODO: 显示确认放弃对话框
                            }
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (selectedActions.isNotEmpty()) {
                                planNameInput = planName
                                showSaveDialog = true
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        enabled = selectedActions.isNotEmpty() && !isSaving && !isLoading
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "保存", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("保存")
                    }
                },
                modifier = Modifier.height(48.dp),  // 默认约 64dp，48dp 更紧凑
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
                .padding(16.dp)
        ) {
            // 顶部：标题 + 添加按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "已选动作 (${selectedActions.size})",
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium
                )
                Button(
                    onClick = {
                        navController.navigate(Screen.ActionLibSelectForPlan.route)
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加动作")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加动作")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 动作列表
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("加载中...")
                    }
                }
                selectedActions.isEmpty() -> {
                    Text(
                        text = "💡 点击「添加动作」选择动作",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = if (showNumberInput) inputPanelHeight else 24.dp)
                    ) {
                        itemsIndexed(
                            items = selectedActions,
                            key = { _, action -> action.actionId }
                        ) { index, planAction ->
                            DraggablePlanActionCard(
                                planAction = planAction,
                                index = index,
                                totalItems = selectedActions.size,
                                onMove = { from, to ->
                                    viewModel.moveAction(from, to)
                                },
                                onRemove = { viewModel.removeAction(it) },
                                onAddGroup = { viewModel.addGroup(it) },
                                onRemoveGroup = { actionId, groupId ->
                                    viewModel.removeGroup(actionId, groupId)
                                },
                                onWeightChange = { actionId, groupId, weight ->
                                    viewModel.updateGroupWeight(actionId, groupId, weight)
                                },
                                onRepsChange = { actionId, groupId, reps ->
                                    viewModel.updateGroupReps(actionId, groupId, reps)
                                },
                                isDragging = index == draggingIndex,
                                onEditWeight = { group, groupIndex, actionId, groupId ->
                                    editingGroup = group
                                    editingField = 0
                                    editingActionId = actionId
                                    editingGroupId = groupId
                                    showNumberInput = true
                                },
                                onEditReps = { group, groupIndex, actionId, groupId ->
                                    editingGroup = group
                                    editingField = 1
                                    editingActionId = actionId
                                    editingGroupId = groupId
                                    showNumberInput = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // ✅ 数字输入对话框
    if (showNumberInput) {
        val currentValue = remember(selectedActions, editingActionId, editingGroupId, editingField) {
            selectedActions
                .find { it.actionId == editingActionId }
                ?.groups
                ?.find { it.id == editingGroupId }
                ?.let { group ->
                    if (editingField == 0) group.weight else group.reps
                } ?: ""
        }

        NumberInputBottomSheet(
            currentValue = currentValue,
            onValueChange = { newValue ->
                if (editingField == 0) {
                    viewModel.updateGroupWeight(editingActionId, editingGroupId, newValue)
                } else {
                    viewModel.updateGroupReps(editingActionId, editingGroupId, newValue)
                }
            },
            onDismiss = {
                showNumberInput = false
                editingGroup = null
            },
            onHeightMeasured = { height ->
                inputPanelHeight = height
            }
        )
    }

    // ✅ 保存计划对话框
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSaving) showSaveDialog = false
            },
            title = { Text("保存计划") },
            text = {
                Column {
                    Text("请输入计划名称：")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = planNameInput,
                        onValueChange = { planNameInput = it },
                        label = { Text("计划名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isSaving
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (planNameInput.isNotBlank()) {
                            coroutineScope.launch {
                                isSaving = true
                                val result = viewModel.savePlan(planNameInput)
                                isSaving = false
                                result.fold(
                                    onSuccess = {
                                        showSaveDialog = false
                                        navController.popBackStack()
                                    },
                                    onFailure = { error ->
                                        errorMessage = error.message ?: "保存失败"
                                        showSaveError = true
                                        showSaveDialog = false
                                    }
                                )
                            }
                        }
                    },
                    enabled = planNameInput.isNotBlank() && !isSaving
                ) {
                    if (isSaving) {
                        Text("保存中...")
                    } else {
                        Text("保存")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isSaving) showSaveDialog = false
                    },
                    enabled = !isSaving
                ) {
                    Text("取消")
                }
            }
        )
    }

    // ✅ 错误提示对话框
    if (showSaveError) {
        AlertDialog(
            onDismissRequest = { showSaveError = false },
            title = { Text("保存失败") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showSaveError = false }) {
                    Text("确定")
                }
            }
        )
    }
}