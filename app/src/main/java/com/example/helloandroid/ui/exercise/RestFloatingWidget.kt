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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.helloandroid.viewmodel.ExecutePlanViewModel
import kotlin.math.roundToInt

@Composable
fun RestFloatingWidget(
    remainingSeconds: Int,
    viewModel: ExecutePlanViewModel,
    onResume: () -> Unit
) {
    // ✅ 从 ViewModel 获取保存的位置
    val offsetX by viewModel.floatingOffsetX.collectAsStateWithLifecycle()
    val offsetY by viewModel.floatingOffsetY.collectAsStateWithLifecycle()

    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }
    var widgetSize by remember { mutableStateOf(0f) }

    // ✅ 用于取消涟漪效果的 InteractionSource
    val interactionSource = remember { MutableInteractionSource() }
    val density = LocalDensity.current

    Popup(
        alignment = Alignment.BottomEnd,
        onDismissRequest = {},
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            // ✅ 不拦截触摸事件，让点击穿透到下层
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    // ✅ 使用像素值
                    screenWidth = coordinates.size.width.toFloat()
                    screenHeight = coordinates.size.height.toFloat()
                }
        ) {
            // ✅ 悬浮窗 - 使用 offset 从右下角偏移
            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.BottomEnd)
                    .offset {
                        IntOffset(
                            x = (-offsetX).roundToInt(),
                            y = (-offsetY).roundToInt()
                        )
                    }
                    .onGloballyPositioned { coordinates ->
                        widgetSize = coordinates.size.width.toFloat()
                    }
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        clip = false
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()

                                // ✅ 所有值都是像素单位
                                val paddingPx = with(density) { 16.dp.toPx() }
                                val maxOffsetX = screenWidth - widgetSize - paddingPx
                                val maxOffsetY = screenHeight - widgetSize - paddingPx

                                // ✅ 防止边界值无效
                                val safeMaxX = if (maxOffsetX > 0) maxOffsetX else 0f
                                val safeMaxY = if (maxOffsetY > 0) maxOffsetY else 0f

                                val newX = offsetX - dragAmount.x
                                val newY = offsetY - dragAmount.y
                                val clampedX = newX.coerceIn(0f, safeMaxX)
                                val clampedY = newY.coerceIn(0f, safeMaxY)

                                viewModel.updateFloatingPosition(clampedX, clampedY)
                            }
                        )
                    }
                    // ✅ 使用 interactionSource 并设置 indication = null 取消涟漪效果
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
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
}