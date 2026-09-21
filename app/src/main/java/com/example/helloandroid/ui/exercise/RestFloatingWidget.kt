package com.example.helloandroid.ui.exercise

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
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
    var widgetSize by remember { mutableStateOf(0f) }

    // ✅ 用于取消涟漪效果的 InteractionSource
    val interactionSource = remember { MutableInteractionSource() }
    val density = LocalDensity.current

    // ✅ 固定初始位置（从右下角偏移）
    val fixedOffsetX = 16.dp    // 距右侧 16dp
    val fixedOffsetY = 100.dp   // 距底部 200dp

    // ✅ 固定位置提供者
    val positionProvider = remember {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val offsetXPx = with(density) { fixedOffsetX.toPx() }
                val offsetYPx = with(density) { fixedOffsetY.toPx() }

                val x = (windowSize.width - popupContentSize.width - offsetXPx).roundToInt()
                val y = (windowSize.height - popupContentSize.height - offsetYPx).roundToInt()
                return IntOffset(x, y)
            }
        }
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = {},
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            // ✅ 不拦截触摸事件，让点击穿透到下层
            usePlatformDefaultWidth = false
        )
    ) {
        // ✅ 悬浮窗 - 使用 offset 从右下角偏移
        Surface(
            modifier = Modifier
                .size(72.dp)
                .onGloballyPositioned { coordinates ->
                    widgetSize = coordinates.size.width.toFloat()
                }
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    clip = false
                )
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