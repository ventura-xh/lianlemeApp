package com.example.helloandroid.ui.exercise

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.example.helloandroid.viewmodel.ExecutePlanViewModel
import kotlin.math.roundToInt

@Composable
fun DraggableRestFloatingWidget(
    remainingSeconds: Int,
    viewModel: ExecutePlanViewModel,
    onResume: () -> Unit
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    // ✅ 屏幕尺寸（用于边界约束）
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val widgetSizePx = with(density) { 72.dp.toPx() }

    // ✅ 初始位置：距右侧 16dp，距底部 100dp
    val initialX = screenWidthPx - widgetSizePx - with(density) { 16.dp.toPx() }
    val initialY = screenHeightPx - widgetSizePx - with(density) { 100.dp.toPx() }

    var offsetX by remember { mutableStateOf(initialX) }
    var offsetY by remember { mutableStateOf(initialY) }

    val interactionSource = remember { MutableInteractionSource() }

    // ✅ 外层 Box 仅用于全屏定位和 zIndex，不添加任何手势，事件自然穿透
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(Float.MAX_VALUE)
    ) {
        // ✅ 真正的交互区域仅限于 Surface 自身大小 (72dp)
        Surface(
            modifier = Modifier
                .size(72.dp)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .shadow(elevation = 8.dp, shape = CircleShape, clip = false)
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    val dragThreshold = 1f.dp.toPx()

                    detectDragGestures(
                        onDragStart = { },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            totalDrag += dragAmount.getDistance()

                            // ✅ 边界约束：防止拖出屏幕
                            val newX = (offsetX + dragAmount.x).coerceIn(0f, screenWidthPx - widgetSizePx)
                            val newY = (offsetY + dragAmount.y).coerceIn(0f, screenHeightPx - widgetSizePx)
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
                    onResume()
                },
            shape = CircleShape,
            color = if (remainingSeconds <= 10) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatRestTime(remainingSeconds),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (remainingSeconds <= 10) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }
    }
}