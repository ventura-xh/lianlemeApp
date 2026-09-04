package com.example.helloandroid.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberInputBottomSheet(
    currentValue: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onHeightMeasured: (Dp) -> Unit = {}  // ✅ 回调通知高度
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(false) }

    // ✅ 在 @Composable 作用域内获取 Density
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        isVisible = true
    }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                isVisible = false
                coroutineScope.launch {
                    try {
                        sheetState.hide()
                    } catch (_: Exception) {}
                    onDismiss()
                }
            },
            sheetState = sheetState,
            scrimColor = Color.Transparent,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            dragHandle = null
        ) {
            // ✅ 用 Box 包裹测量高度
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        // 通知外部高度（包含 padding）
                        val heightPx = coordinates.size.height.toFloat()
                        val heightDp = with(density) {
                            heightPx.toDp()
                        }
                        onHeightMeasured(heightDp)
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "输入数字",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentValue.ifEmpty { "0" },
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    isVisible = false
                                    coroutineScope.launch {
                                        try { sheetState.hide() } catch (_: Exception) {}
                                        onDismiss()
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "关闭",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SimpleNumberKeyboard(
                        currentValue = currentValue,
                        onDigitClick = { digit ->
                            if (currentValue.length < 6) onValueChange(currentValue + digit)
                        },
                        onDelete = {
                            if (currentValue.isNotEmpty()) onValueChange(currentValue.dropLast(1))
                        },
                        onDotClick = {
                            // ✅ 小数点：只允许一个
                            if (!currentValue.contains(".") && currentValue.isNotEmpty()) {
                                onValueChange(currentValue + ".")
                            } else if (currentValue.isEmpty()) {
                                onValueChange("0.")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NonModalNumberKeyboard(
    visible: Boolean,
    currentValue: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                // ✅ 关键：添加 imePadding 避免被系统导航栏遮挡
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 当前值显示
                Text(
                    text = currentValue.ifEmpty { "0" },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // 数字键盘网格（简化示意，替换为你的 SimpleNumberKeyboard）
                SimpleNumberKeyboard(
                    currentValue = currentValue,
                    onDigitClick = { digit ->
                        if (currentValue.length < 6) onValueChange(currentValue + digit)
                    },
                    onDelete = {
                        if (currentValue.isNotEmpty()) onValueChange(currentValue.dropLast(1))
                    },
                    onDotClick = {
                        // ✅ 小数点：只允许一个
                        if (!currentValue.contains(".") && currentValue.isNotEmpty()) {
                            onValueChange(currentValue + ".")
                        } else if (currentValue.isEmpty()) {
                            onValueChange("0.")
                        }
                    }
                )
            }
        }
    }
}

// 键盘和按钮函数保持不变...
@Composable
fun SimpleNumberKeyboard(
    currentValue: String,
    onDigitClick: (String) -> Unit,
    onDelete: () -> Unit,
    onDotClick: () -> Unit
) {
    val digits = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        digits.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { label ->
                    SimpleKeyButton(
                        label = label,
                        onClick = {
                            when (label) {
                                "⌫" -> onDelete()
                                "." -> onDotClick()
                                else -> if (currentValue.length < 6) onDigitClick(label)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleKeyButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSpecial = label == "⌫" || label == "清空"
    val isDot = label == "."

    Box(
        modifier = modifier
            .height(48.dp)
            .clickable { onClick() }
            .background(
                color = when {
                    isDot -> MaterialTheme.colorScheme.primaryContainer
                    isSpecial -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSpecial) Color.Transparent
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (isSpecial) 14.sp else 20.sp,
            fontWeight = if (isSpecial) FontWeight.Normal else FontWeight.Medium,
            color = when {
                isDot -> MaterialTheme.colorScheme.onPrimaryContainer
                isSpecial -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}