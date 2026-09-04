package com.example.helloandroid.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * 通用确认对话框（带倒计时）
 * @param showDialog 是否显示对话框
 * @param title 标题
 * @param message 消息内容（支持多行文本列表）
 * @param confirmText 确认按钮文字
 * @param cancelText 取消按钮文字
 * @param countdownSeconds 倒计时秒数（默认 3 秒）
 * @param isDestructive 是否为破坏性操作（确认按钮显示为红色）
 * @param onConfirm 确认回调
 * @param onDismiss 取消/关闭回调
 */
@Composable
fun ConfirmDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    confirmText: String = "确认",
    cancelText: String = "取消",
    countdownSeconds: Int = 3,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var countdown by remember { mutableStateOf(countdownSeconds) }

    // ✅ 启动倒计时
    LaunchedEffect(showDialog) {
        if (showDialog) {
            countdown = countdownSeconds
            while (countdown > 0) {
                delay(1000L)
                countdown--
            }
        } else {
            countdown = countdownSeconds
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                if (countdown == 0) onDismiss()
            },
            title = { Text(title) },
            text = {
                Column {
                    // ✅ 支持多行文本（用 \n 分隔）
                    message.split("\n").forEach { line ->
                        if (line.isNotBlank()) {
                            Text(
                                text = if (line.startsWith("•")) line else "• $line",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (countdown > 0) "⏱️ ${countdown}秒后可确认" else "",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        countdown = countdownSeconds
                    },
                    enabled = countdown == 0,
                    colors = if (isDestructive) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(if (countdown > 0) "$confirmText (${countdown}s)" else confirmText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                        countdown = countdownSeconds
                    }
                    // ✅ 取消按钮始终可点击
                ) {
                    Text(cancelText)
                }
            }
        )
    }
}

/**
 * 带列表项的确认对话框
 * @param showDialog 是否显示对话框
 * @param title 标题
 * @param items 列表项
 * @param confirmText 确认按钮文字
 * @param cancelText 取消按钮文字
 * @param countdownSeconds 倒计时秒数（默认 3 秒）
 * @param isDestructive 是否为破坏性操作
 * @param onConfirm 确认回调
 * @param onDismiss 取消/关闭回调
 */
@Composable
fun ConfirmDialogWithItems(
    showDialog: Boolean,
    title: String,
    items: List<String>,
    confirmText: String = "确认",
    cancelText: String = "取消",
    countdownSeconds: Int = 3,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var countdown by remember { mutableStateOf(countdownSeconds) }

    LaunchedEffect(showDialog) {
        if (showDialog) {
            countdown = countdownSeconds
            while (countdown > 0) {
                delay(1000L.milliseconds)
                countdown--
            }
        } else {
            countdown = countdownSeconds
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                if (countdown == 0) onDismiss()
            },
            title = { Text(title) },
            text = {
                Column {
                    items.forEach { item ->
                        Text(
                            text = "• $item",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (countdown > 0) "⏱️ ${countdown}秒后可确认" else "",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        countdown = countdownSeconds
                    },
                    enabled = countdown == 0,
                    colors = if (isDestructive) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(if (countdown > 0) "$confirmText (${countdown}s)" else confirmText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                        countdown = countdownSeconds
                    }
                ) {
                    Text(cancelText)
                }
            }
        )
    }
}