package com.example.helloandroid.ui.actionlib

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.helloandroid.entity.ActionLibEntity
import com.example.helloandroid.entity.MuscleEntity
import com.example.helloandroid.ui.common.InitState
import kotlinx.coroutines.launch

// --- UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageActionLib(
    viewModel: ActionLibViewModel = viewModel(
        factory = ActionLibViewModel.factory
    ),
    selectMode: Boolean = false,
    navController: NavHostController? = null,
    onActionsSelected: ((List<Long>) -> Unit)? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val actions by viewModel.actions.collectAsStateWithLifecycle(
        initialValue = emptyList(),
        lifecycle = lifecycleOwner.lifecycle
    )
    val groupedActions by viewModel.groupedActions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val allMuscles by viewModel.allMuscles.collectAsStateWithLifecycle()
    val initState by viewModel.initState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    // 侧边栏状态
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedMuscle by remember { mutableStateOf<String?>(null) }

    // 多选状态
    var selectedActionIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    // 添加自定义动作对话框
    var showAddDialog by remember { mutableStateOf(false) }
    var newActionName by remember { mutableStateOf("") }
    var newActionCategory by remember { mutableStateOf("") }
    var newActionDescription by remember { mutableStateOf("") }
    var newActionTips by remember { mutableStateOf("") }
    var selectedMuscleIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    // 动作详情弹窗
    var selectedActionForDetail by remember { mutableStateOf<ActionLibEntity?>(null) }

    // 初始化状态
    var isInitialized by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(0) }

    // 加载数据
    LaunchedEffect(Unit) {
        viewModel.loadActions()
    }

    LaunchedEffect(initState) {
        when (initState) {
            is InitState.Success -> isInitialized = true
            else -> {}
        }
    }

    // 当前显示的动作列表
    val displayedActions = remember(selectedCategory, selectedMuscle, actions, groupedActions) {
        when {
            selectedCategory != null && selectedMuscle != null -> {
                groupedActions[selectedCategory]?.get(selectedMuscle) ?: emptyList()
            }
            selectedCategory != null -> {
                actions.filter { it.category == selectedCategory }
            }
            else -> actions
        }
    }

    Scaffold(
        topBar = {
            if (selectMode) {
                SelectModeTopAppBar(
                    navController = navController,
                    selectedCount = selectedActionIds.size,
                    onConfirm = {
                        if (selectedActionIds.isNotEmpty()) {
                            onActionsSelected?.invoke(selectedActionIds.toList())
                            navController?.popBackStack()
                        }
                    },
                    onClearAll = {
                        selectedActionIds = emptySet()
                    }
                )
            } else {
                ActionLibTopAppBar(
                    isInitialized = isInitialized,
                    onAddAction = { showAddDialog = true },
                    onReset = { showResetDialog = true }
                )
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 侧边栏
            PermanentCategoryDrawer(
                categories = categories,
                groupedActions = groupedActions,
                selectedCategory = selectedCategory,
                selectedMuscle = selectedMuscle,
                onCategorySelected = { category ->
                    selectedCategory = category
                    if (category != null) {
                        selectedMuscle = null
                    }
                },
                onMuscleSelected = { muscle ->
                    selectedMuscle = muscle
                }
            )

            // 主内容
            ActionLibContent(
                actions = displayedActions,
                selectedCategory = selectedCategory,
                selectedMuscle = selectedMuscle,
                selectMode = selectMode,
                selectedActionIds = selectedActionIds,
                onActionClick = { actionId ->
                    if (selectMode) {
                        selectedActionIds = if (selectedActionIds.contains(actionId)) {
                            selectedActionIds.minus(actionId)
                        } else {
                            selectedActionIds.plus(actionId)
                        }
                    } else {
                        val action = actions.find { it.id == actionId }
                        selectedActionForDetail = action
                    }
                },
                isLoading = isLoading
            )
        }
    }

    // 添加自定义动作对话框
    if (showAddDialog) {
        AddActionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, category, muscleIds, description, tips ->
                coroutineScope.launch {
                    viewModel.addCustomAction(
                        name = name,
                        category = category,
                        muscleIds = muscleIds,
                        description = description,
                        tips = tips
                    )
                    newActionName = ""
                    newActionCategory = ""
                    newActionDescription = ""
                    newActionTips = ""
                    selectedMuscleIds = emptySet()
                    showAddDialog = false
                    viewModel.refresh()
                }
            },
            availableMuscles = allMuscles,
            selectedMuscleIds = selectedMuscleIds,
            onMuscleSelectionChange = { selectedMuscleIds = it }
        )
    }

    // 动作详情弹窗
    if (selectedActionForDetail != null) {
        ActionDetailDialog(
            action = selectedActionForDetail,
            onDismiss = { selectedActionForDetail = null },
            onDelete = { actionId ->
                // ✅ 删除动作
                coroutineScope.launch {
                    viewModel.deleteCustomAction(actionId)
                    selectedActionForDetail = null
                }
            },
            viewModel = viewModel
        )
    }
}

// ============================================================
// 侧边栏
// ============================================================

@Composable
fun PermanentCategoryDrawer(
    categories: List<String>,
    groupedActions: Map<String, Map<String, List<ActionLibEntity>>>,
    selectedCategory: String?,
    selectedMuscle: String?,
    onCategorySelected: (String?) -> Unit,
    onMuscleSelected: (String?) -> Unit
) {
    PermanentDrawerSheet(
        modifier = Modifier
            .width(120.dp)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // "全部" 选项
            val isAllSelected = selectedCategory == null && selectedMuscle == null
            Text(
                text = "💪部位",
                fontSize = 14.sp,
                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isAllSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onCategorySelected(null)
                        onMuscleSelected(null)
                    }
                    .padding(8.dp)
            )
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            categories.forEach { category ->
                val muscles = groupedActions[category]?.keys?.toList() ?: emptyList()
                val isExpanded = selectedCategory == category

                // 分类标题
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (selectedCategory == category) {
                                onCategorySelected(null)
                            } else {
                                onCategorySelected(category)
                                onMuscleSelected(null)
                            }
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "▼" else "▶",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = category,
                        fontSize = 14.sp,
                        fontWeight = if (selectedCategory == category && selectedMuscle == null) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedCategory == category && selectedMuscle == null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // 肌肉列表
                if (isExpanded) {
                    muscles.forEach { muscleName ->
                        val actionCount = groupedActions[category]?.get(muscleName)?.size ?: 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onMuscleSelected(muscleName)
                                }
                                .padding(start = 20.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "└ $muscleName",
                                fontSize = 13.sp,
                                fontWeight = if (selectedMuscle == muscleName) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedMuscle == muscleName) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// 主内容
// ============================================================

@Composable
fun ActionLibContent(
    actions: List<ActionLibEntity>,
    selectedCategory: String?,
    selectedMuscle: String?,
    selectMode: Boolean = false,
    selectedActionIds: Set<Long> = emptySet(),
    onActionClick: ((Long) -> Unit)? = null,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        when {
            selectedCategory != null && selectedMuscle != null -> {
                Text(
                    text = "$selectedCategory / $selectedMuscle",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "共 ${actions.size} 个动作",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            selectedCategory != null -> {
                Text(
                    text = "$selectedCategory (全部)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "共 ${actions.size} 个动作",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                Text(
                    text = "全部动作",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "共 ${actions.size} 个动作",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 动作列表
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("加载中...")
            }
        } else if (actions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "💡 该分类下暂无动作",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(actions) { action ->
                    ActionListItem(
                        action = action,
                        selectMode = selectMode,
                        isSelected = selectedActionIds.contains(action.id),
                        onClick = {
                            onActionClick?.invoke(action.id)
                        }
                    )
                }
            }
        }
    }
}

// ============================================================
// TopAppBar
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionLibTopAppBar(
    isInitialized: Boolean,
    onAddAction: () -> Unit,
    onReset: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "🏋️ 动作库",
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        navigationIcon = { Spacer(modifier = Modifier.size(0.dp)) },
        actions = {
            OutlinedButton(
                onClick = onAddAction,
                modifier = Modifier
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "动作",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("动作", fontSize = 12.sp)
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

// ============================================================
// 选择模式 TopAppBar
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectModeTopAppBar(
    navController: NavHostController?,
    selectedCount: Int,
    onConfirm: () -> Unit,
    onClearAll: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = if (selectedCount > 0) "已选 $selectedCount 个动作" else "选择动作",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer  // ✅ 设置颜色
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController?.popBackStack() }) {
                Icon(Icons.Default.Close,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer // ✅ 设置颜色
                )
            }
        },
        actions = {
            if (selectedCount > 0) {
                TextButton(onClick = onClearAll) {
                    Text("清空", fontSize = 13.sp)
                }
            }
            IconButton(
                onClick = onConfirm,
                enabled = selectedCount > 0
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "确认选择",
                    tint = if (selectedCount > 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
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

// ============================================================
// 动作列表项
// ============================================================

@Composable
fun ActionListItem(
    action: ActionLibEntity,
    selectMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .then(
                if (selectMode) {
                    Modifier.clickable { onClick?.invoke() }
                } else {
                    Modifier.clickable { onClick?.invoke() }
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = action.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = action.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selectMode) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            },
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            } else {
                Text(
                    text = "›",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================
// 添加自定义动作对话框
// ============================================================

@Composable
fun AddActionDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        category: String,
        muscleIds: List<Long>,
        description: String,
        tips: String
    ) -> Unit,
    availableMuscles: List<MuscleEntity>,
    selectedMuscleIds: Set<Long>,
    onMuscleSelectionChange: (Set<Long>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tips by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("➕ 添加自定义动作") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("动作名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("主分类（如：下肢、上肢）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "目标肌肉（可多选）",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                availableMuscles.forEach { muscle ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onMuscleSelectionChange(
                                    if (selectedMuscleIds.contains(muscle.id)) {
                                        selectedMuscleIds.minus(muscle.id)
                                    } else {
                                        selectedMuscleIds.plus(muscle.id)
                                    }
                                )
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedMuscleIds.contains(muscle.id),
                            onCheckedChange = null
                        )
                        Text(
                            text = "${muscle.icon} ${muscle.name}",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("动作要领") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = tips,
                    onValueChange = { tips = it },
                    label = { Text("注意事项") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && category.isNotBlank() && selectedMuscleIds.isNotEmpty()) {
                        onConfirm(name, category, selectedMuscleIds.toList(), description, tips)
                    }
                },
                enabled = name.isNotBlank() && category.isNotBlank() && selectedMuscleIds.isNotEmpty()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}