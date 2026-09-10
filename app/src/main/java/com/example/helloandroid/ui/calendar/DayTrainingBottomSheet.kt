package com.example.helloandroid.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.example.helloandroid.FitApplication
import com.example.helloandroid.entity.TrainingSessionActionDetailEntity
import com.example.helloandroid.entity.TrainingSessionActionEntity
import com.example.helloandroid.entity.TrainingSessionEntity
import com.example.helloandroid.entity.model.TrainingSessionWithDetails
import com.example.helloandroid.entity.model.formatDate
import com.example.helloandroid.entity.model.formatDuration
import com.example.helloandroid.navigation.Screen

@Composable
fun DayTrainingBottomSheet(
    sessions: List<TrainingSessionEntity>,
    currentIndex: Int,
    onDismiss: () -> Unit,
    navController: NavHostController
) {
    var index by remember { mutableStateOf(currentIndex) }
    val totalSessions = sessions.size
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetHeight = screenHeight * 2 / 3

    // ✅ 当前选中的训练详情
    var sessionDetail by remember { mutableStateOf<TrainingSessionWithDetails?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // ✅ 获取当前选中的 session
    val currentSession = sessions.getOrNull(index)

    // ✅ 加载训练详情
    LaunchedEffect(currentSession?.id) {
        currentSession?.let {
            isLoading = true
            sessionDetail = getSessionDetail(it.id)
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.1f))
                .clickable { onDismiss() }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(sheetHeight)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .clickable { /* 阻止点击穿透 */ },
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // ✅ 顶部：日期 + 关闭按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 ${formatDate(sessions.first().startTime)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("加载中...")
                        }
                    } else if (sessionDetail == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无数据")
                        }
                    } else {
                        // ✅ 训练信息
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            // 训练名称和状态
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = sessionDetail!!.session.planName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (sessionDetail!!.session.status == 1) "✅ 已完成" else "❌ 已取消",
                                    fontSize = 14.sp,
                                    color = if (sessionDetail!!.session.status == 1) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // 时间和时长
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "⏱️ ${formatDuration(sessionDetail!!.session.totalDuration)}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "🏋️ ${sessionDetail!!.actions.size} 个动作",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 分割线
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // ✅ 动作列表（真实数据）
                            Text(
                                text = "📋 动作详情",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // 动作列表
                            sessionDetail!!.actions.forEach { actionWithDetails ->
                                TrainingActionPreview(
                                    action = actionWithDetails.action,
                                    details = actionWithDetails.details
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ✅ 底部：左右切换 + 页码
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (index > 0) {
                                    index--
                                }
                            },
                            enabled = index > 0
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "上一个",
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "${index + 1} / $totalSessions",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        IconButton(
                            onClick = {
                                if (index < totalSessions - 1) {
                                    index++
                                }
                            },
                            enabled = index < totalSessions - 1
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "下一个",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ✅ 查看详情按钮
                    Button(
                        onClick = {
                            val session = sessions[index]
                            navController.navigate(Screen.TrainingResult.pass(session.id, false))
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("查看详情")
                    }
                }
            }
        }
    }
}

// ✅ 动作预览项
@Composable
fun TrainingActionPreview(
    action: TrainingSessionActionEntity,
    details: List<TrainingSessionActionDetailEntity>
) {
    // ✅ 显示动作名称和完成的组数
    val completedGroups = details.count { it.isCompleted }
    val totalGroups = details.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = action.actionName,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (action.isCompleted) {
                "✅ $completedGroups/$totalGroups"
            } else {
                "⏳ $completedGroups/$totalGroups"
            },
            fontSize = 13.sp,
            color = if (action.isCompleted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

// ✅ 获取训练详情
private suspend fun getSessionDetail(sessionId: Long): TrainingSessionWithDetails? {
    return try {
        val database = FitApplication.instance.database
        database.trainingSessionDao().getSessionWithDetails(sessionId)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}