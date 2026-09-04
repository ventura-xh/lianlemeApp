package com.example.helloandroid.ui.plan

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helloandroid.entity.model.ActionGroup
import com.example.helloandroid.entity.model.PlanAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DraggablePlanActionCard(
    planAction: PlanAction,
    index: Int,
    totalItems: Int,
    onMove: (Int, Int) -> Unit,
    onRemove: (Long) -> Unit,
    onAddGroup: (Long) -> Unit,
    onRemoveGroup: (Long, String) -> Unit,
    onWeightChange: (Long, String, String) -> Unit,
    onRepsChange: (Long, String, String) -> Unit,
    isDragging: Boolean = false,
    modifier: Modifier = Modifier,
    onEditWeight: (ActionGroup, Int, Long, String) -> Unit = { _, _, _, _ -> },
    onEditReps: (ActionGroup, Int, Long, String) -> Unit = { _, _, _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()
    var isExpanded by remember { mutableStateOf(false) }
    var isBeingDragged by remember { mutableStateOf(false) }

    val dragOffset = remember { Animatable(0f) }
    var moveCooldown by remember { mutableStateOf(false) }
    val gestureKey = "${planAction.actionId}_${index}"

    LaunchedEffect(index) {
        if (isBeingDragged) {
            isBeingDragged = false
            moveCooldown = false
            dragOffset.snapTo(0f)
        }
    }

    val cardElevation by animateDpAsState(
        targetValue = if (isBeingDragged || isDragging) 12.dp else 2.dp,
        animationSpec = tween(150),
        label = "cardElevation"
    )

    val cardScale by animateFloatAsState(
        targetValue = if (isBeingDragged || isDragging) 1.03f else 1f,
        animationSpec = tween(150),
        label = "cardScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .offset {
                IntOffset(
                    x = 0,
                    y = if (isBeingDragged || isDragging) {
                        dragOffset.value.roundToInt()
                    } else {
                        0
                    }
                )
            }
            .scale(cardScale),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        colors = CardDefaults.cardColors(
            containerColor = if (isBeingDragged || isDragging) {
                MaterialTheme.colorScheme.primaryContainer
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
            // 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DragHandle(
                        key = gestureKey,
                        isBeingDragged = isBeingDragged,
                        onDragStart = {
                            isBeingDragged = true
                            moveCooldown = false
                            coroutineScope.launch {
                                dragOffset.snapTo(0f)
                            }
                        },
                        onDrag = { dragAmount ->
                            coroutineScope.launch {
                                dragOffset.snapTo(dragOffset.value + dragAmount)
                            }
                            if (moveCooldown) return@DragHandle

                            val moveThreshold = 60f
                            val currentOffset = dragOffset.value

                            if (currentOffset > moveThreshold && index < totalItems - 1) {
                                moveCooldown = true
                                isBeingDragged = false
                                onMove(index, index + 1)
                                coroutineScope.launch {
                                    dragOffset.snapTo(0f)
                                    delay(50L.milliseconds)
                                    moveCooldown = false
                                }
                            } else if (dragOffset.value < -moveThreshold && index > 0) {
                                moveCooldown = true
                                isBeingDragged = false
                                coroutineScope.launch {
                                    dragOffset.snapTo(0f)
                                }
                                onMove(index, index - 1)
                                coroutineScope.launch {
                                    dragOffset.snapTo(0f)
                                    delay(50L.milliseconds)
                                    moveCooldown = false
                                }
                            }
                        },
                        onDragEnd = {
                            isBeingDragged = false
                            moveCooldown = false
                            coroutineScope.launch {
                                dragOffset.animateTo(0f, animationSpec = tween(200))
                            }
                        }
                    )

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = if (isExpanded) "收起" else "展开",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${index + 1}. ${planAction.actionName}",
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "(${planAction.groups.size}组)",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { onRemove(planAction.actionId) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "移除动作",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 展开内容：组列表
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                // 表头
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "组",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(28.dp)
                    )
                    Text(
                        text = "重量(kg)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "次数",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(40.dp)
                    )
                }

                // 组列表
                planAction.groups.forEachIndexed { groupIndex, group ->
                    NumberInputGroupItem(
                        group = group,
                        groupIndex = groupIndex,
                        totalGroups = planAction.groups.size,
                        onRemoveGroup = {
                            if (planAction.groups.size > 1) {
                                onRemoveGroup(planAction.actionId, group.id)
                            }
                        },
                        onEditWeight = {
                            onEditWeight(group, groupIndex, planAction.actionId, group.id)
                        },
                        onEditReps = {
                            onEditReps(group, groupIndex, planAction.actionId, group.id)
                        }
                    )
                }

                // 新增一组按钮
                Button(
                    onClick = { onAddGroup(planAction.actionId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(36.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "新增一组", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("新增一组", fontSize = 13.sp)
                }
            }
        }
    }
}

// ✅ 数字输入组项
@Composable
fun NumberInputGroupItem(
    group: ActionGroup,
    groupIndex: Int,
    totalGroups: Int,
    onRemoveGroup: () -> Unit,
    onEditWeight: () -> Unit,
    onEditReps: () -> Unit
) {
    val weightNum = group.weight.toDoubleOrNull()
    val repsNum = group.reps.toIntOrNull()
    val hasWeight = weightNum != null && weightNum > 0
    val hasReps = repsNum != null && repsNum > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${groupIndex + 1}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(28.dp)
        )

        // 重量
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clickable { onEditWeight() }
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = if (hasWeight) "${String.format("%.1f", weightNum)} kg" else "点击输入",
                fontSize = 14.sp,
                color = if (hasWeight) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        // 次数
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clickable { onEditReps() }
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = if (hasReps) "$repsNum 次" else "点击输入",
                fontSize = 14.sp,
                color = if (hasReps) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        // 删除组按钮
        IconButton(
            onClick = onRemoveGroup,
            enabled = totalGroups > 1,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "删除组",
                tint = if (totalGroups > 1) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                },
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ✅ 拖动手柄
@Composable
fun DragHandle(
    key: String,
    isBeingDragged: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .pointerInput(key) {
                detectDragGestures(
                    onDragStart = { _ ->
                        onDragStart()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.y)
                    },
                    onDragEnd = {
                        onDragEnd()
                    },
                    onDragCancel = {
                        onDragEnd()
                    }
                )
            }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(2.dp)
                    .background(
                        color = if (isBeingDragged) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        shape = RoundedCornerShape(1.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(2.dp)
                    .background(
                        color = if (isBeingDragged) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        shape = RoundedCornerShape(1.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(2.dp)
                    .background(
                        color = if (isBeingDragged) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}