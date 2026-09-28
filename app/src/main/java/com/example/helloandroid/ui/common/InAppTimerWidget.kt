// ui/common/InAppTimerWidget.kt

package com.example.helloandroid.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

/**
 * App 内悬浮计时器（可拖动）
 * 在训练进行中但不在运动页面时显示
 */
@Composable
fun InAppTimerWidget(
    elapsedTime: Long,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    // ✅ 悬浮窗尺寸（宽 × 高）
    val widgetWidthPx = with(density) { 120.dp.toPx() }
    val widgetHeightPx = with(density) { 40.dp.toPx() }

    // ✅ 屏幕尺寸（用于边界约束）
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // ✅ 初始位置：距右侧 16dp，距底部 100dp
    val initialX = screenWidthPx - widgetWidthPx - with(density) { 16.dp.toPx() }
    val initialY = screenHeightPx - widgetHeightPx - with(density) { 200.dp.toPx() }

    var offsetX by remember { mutableStateOf(initialX) }
    var offsetY by remember { mutableStateOf(initialY) }

    val interactionSource = remember { MutableInteractionSource() }

    // ✅ 外层 Box 仅用于全屏定位和 zIndex，不添加任何手势，事件自然穿透
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(Float.MAX_VALUE)
    ) {
        // ✅ 真正的交互区域仅限于 Surface 自身大小
        Surface(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    clip = false
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { },
                        onDrag = { change, dragAmount ->
                            change.consume()

                            // ✅ 边界约束：防止拖出屏幕
                            val newX = (offsetX + dragAmount.x)
                                .coerceIn(0f, screenWidthPx - widgetWidthPx)
                            val newY = (offsetY + dragAmount.y)
                                .coerceIn(0f, screenHeightPx - widgetHeightPx)
                            offsetX = newX
                            offsetY = newY
                        },
                        onDragEnd = {}
                    )
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null  // 去掉水波纹，避免拖动时闪烁
                ) {
                    onClick()
                },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "计时器",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatTime(elapsedTime),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}