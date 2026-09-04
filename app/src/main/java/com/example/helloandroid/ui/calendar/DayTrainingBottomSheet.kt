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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.helloandroid.entity.TrainingSessionEntity
import com.example.helloandroid.entity.model.formatDate
import com.example.helloandroid.entity.model.formatDuration
import com.example.helloandroid.navigation.Screen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun DayTrainingBottomSheet(
    sessions: List<TrainingSessionEntity>,
    currentIndex: Int,
    onDismiss: () -> Unit,
    navController: NavHostController  // ✅ 添加 navController
) {
    var index by remember { mutableStateOf(currentIndex) }
    val totalSessions = sessions.size
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetHeight = screenHeight * 2 / 3  // ✅ 三分之二屏幕高度

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        // ✅ 遮罩层
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.1f))
                .clickable { onDismiss() }
        ) {
            // ✅ 底部弹窗
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

                    // ✅ 中间：训练记录内容
                    if (sessions.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            // 当前选中的训练记录
                            val currentSession = sessions[index]

                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // 训练名称和状态
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = currentSession.planName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (currentSession.status == 1) "✅ 已完成" else "❌ 已取消",
                                        fontSize = 14.sp,
                                        color = if (currentSession.status == 1) {
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
                                        text = "⏱️ ${formatDuration(currentSession.totalDuration)}",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "🏋️ 动作: ${getActionCount(currentSession.id)}",
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

                                // 动作列表（预览）
                                Text(
                                    text = "动作列表",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // 这里可以显示该训练的动作列表
                                // 简单显示组数信息
                                TrainingSessionPreview(
                                    sessionId = currentSession.id
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ✅ 底部：左右切换 + 页码
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 左箭头
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

                            // 页码
                            Text(
                                text = "${index + 1} / $totalSessions",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // 右箭头
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
                                // ✅ 跳转到训练详情页面
                                navController.navigate(Screen.TrainingResult.pass(session.id, false))
                                onDismiss()  // 关闭底部弹窗
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("查看详情")
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无训练记录")
                        }
                    }
                }
            }
        }
    }
}

// ✅ 训练会话预览（显示动作数量）
@Composable
fun TrainingSessionPreview(
    sessionId: Long
) {
    // TODO: 根据 sessionId 加载动作列表
    // 这里先显示示例数据
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 1..3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "动作 $i",
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "3组",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ✅ 获取训练的动作数量
private fun getActionCount(sessionId: Long): Int {
    // TODO: 从数据库查询
    return 3
}