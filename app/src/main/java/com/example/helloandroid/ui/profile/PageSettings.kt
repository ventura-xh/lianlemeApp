package com.example.helloandroid.ui.profile

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.helloandroid.FitApplication
import com.example.helloandroid.repository.ActionLibRepository
import com.example.helloandroid.ui.actionlib.ActionLibViewModel
import com.example.helloandroid.ui.common.ConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageSettings(
    navController: NavHostController
) {
    // ✅ 设置项状态
    var notificationEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current  // ✅ 获取 Context

    // ✅ 在 PageSettings 内部获取 ViewModel
    val actionLibViewModel: ActionLibViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "设置",
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // ✅ 通用设置
            item {
                SettingsSectionHeader(title = "通用")
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "通知",
                    subtitle = "开启训练提醒和通知",
                    checked = notificationEnabled,
                    onCheckedChange = { notificationEnabled = it }
                )
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Outlined.DataUsage,
                    title = "深色模式",
                    subtitle = "跟随系统或手动切换",
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            }

            // ✅ 数据管理
            item {
                SettingsSectionHeader(title = "数据管理")
            }

            item {
                SettingsClickItem(
                    icon = Icons.Default.RestartAlt,
                    title = "重置动作库",
                    subtitle = "恢复所有预设动作，清除自定义动作",
                    onClick = {
                        showResetDialog = true
                    }
                )
            }

            item {
                SettingsClickItem(
                    icon = Icons.Default.Delete,
                    title = "清除所有数据",
                    subtitle = "删除所有训练记录和计划",
                    onClick = {
                        showConfirmDialog = true
                    }
                )
            }

            // ✅ 关于
            item {
                SettingsSectionHeader(title = "关于")
            }

            item {
                SettingsClickItem(
                    icon = Icons.Default.Info,
                    title = "版本信息",
                    subtitle = "v1.0.0",
                    onClick = {
                        // TODO: 显示版本信息
                    }
                )
            }

            // 底部留白
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // ✅ 重置动作库确认对话框
    if (showResetDialog) {
        ConfirmDialog(
            showDialog = showResetDialog,
            title = "重置动作库",
            message = "恢复所有预设动作\n清除所有自定义添加的动作\n该操作不可撤销",
            confirmText = "确认重置",
            cancelText = "取消",
            countdownSeconds = 3,
            isDestructive = false,
            onConfirm = {
                actionLibViewModel.resetPresetActions(context)
                showResetDialog = false
            },
            onDismiss = {
                showResetDialog = false
            }
        )
    }

    // ✅ 清除所有数据确认对话框
    if (showConfirmDialog) {
        ConfirmDialog(
            showDialog = showConfirmDialog,
            title = "⚠️ 清除所有数据",
            message = "清除所有训练记录\n所有训练计划\n所有自定义动作\n该操作不可撤销",
            confirmText = "确认重置",
            cancelText = "取消",
            countdownSeconds = 3,
            isDestructive = true,
            onConfirm = {
                actionLibViewModel.resetPresetActions(context)
                showConfirmDialog = false
            },
            onDismiss = {
                showConfirmDialog = false
            }
        )
    }
}

// ✅ 设置分组标题
@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp)
    )
}

// ✅ 开关设置项
@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

// ✅ 点击设置项
@Composable
fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = when (title) {
                        "重置动作库" -> MaterialTheme.colorScheme.primary
                        "清除所有数据" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (title) {
                        "清除所有数据" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "›",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}