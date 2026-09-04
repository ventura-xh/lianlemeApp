package com.example.helloandroid.ui.plan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.helloandroid.entity.ActionDetailEntity
import com.example.helloandroid.entity.ActionLibEntity
import com.example.helloandroid.entity.PlanActionsEntity
import com.example.helloandroid.entity.PlansEntity
import com.example.helloandroid.navigation.Screen
import com.example.helloandroid.viewmodel.PlanDetailViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

data class PlanDetail(
    val plan: PlansEntity,
    val actions: List<PlanActionWithGroups>
)

data class PlanActionWithGroups(
    val planAction: PlanActionsEntity,
    val action: ActionLibEntity,
    val groups: List<ActionDetailEntity>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailBottomSheet(
    planId: Long,
    onDismiss: () -> Unit,
    navController: NavHostController? = null,
    viewModel: PlanDetailViewModel = viewModel(
        factory = PlanDetailViewModel.factory
    )
) {
    var planDetail by remember { mutableStateOf<PlanDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // ✅ 控制显示/隐藏动画
    var isVisible by remember { mutableStateOf(false) }

    // ✅ 第一步：先弹出 BottomSheet（不等待数据加载）
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // ✅ 第二步：再加载数据
    LaunchedEffect(planId) {
        isLoading = true
        planDetail = viewModel.getPlanDetail(planId)
        isLoading = false
    }

    // ✅ 关闭逻辑
    fun dismissWithAnimation() {
        isVisible = false
    }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            // ✅ 等待 exit 动画完成（250ms）后再通知父组件移除
            delay(300L.milliseconds)
            onDismiss()
        }
    }

    // ✅ 自定义固定高度 BottomSheet
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { dismissWithAnimation() }  // 点击外部关闭
    ) {
        // ✅ 遮罩层（带淡入淡出动画）
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { dismissWithAnimation() }
            )
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(600.dp)  // ✅ 固定高度
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null  // ✅ 去掉点击涟漪效果
                    ) {
                        // 空实现，什么都不做
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 标题行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左侧：关闭按钮 ✕
                        IconButton(
                            onClick = { dismissWithAnimation() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 中间：计划名称（居中）
                        Text(
                            text = planDetail?.plan?.name ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        // 右侧：编辑按钮 ✏️
                        IconButton(
                            onClick = {
                                // TODO: 跳转到编辑计划页面
                                navController?.navigate("edit_plan/${planId}")
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "编辑计划",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ✅ 内容区域：根据加载状态显示
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("加载中...")
                        }
                    } else if (planDetail?.actions.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("该计划暂无动作")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(planDetail?.actions ?: emptyList()) { item ->
                                PlanActionWithGroupsCard(
                                    actionWithGroups = item
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 开始运动按钮
                    Button(
                        onClick = {
                            navController?.navigate(Screen.ExercisePrepare.pass(planId))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !planDetail?.actions.isNullOrEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "开始运动"
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("开始运动")
                    }
                }
            }
        }
    }
}

@Composable
fun PlanActionWithGroupsCard(
    actionWithGroups: PlanActionWithGroups
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            // ✅ 点击卡片切换展开状态
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = actionWithGroups.action.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.height(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${actionWithGroups.groups.size}组",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = if (isExpanded) "▲" else "▼",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ✅ 展开时显示详细组信息
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    actionWithGroups.groups.forEachIndexed { index, group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(
                                text = "第${index + 1}组",
                                modifier = Modifier.weight(1f),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "重量: ${group.weight}kg",
                                modifier = Modifier.weight(1f),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "次数: ${group.reps}次",
                                modifier = Modifier.weight(1f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}