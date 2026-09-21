// ui/execute/SyncPlanDialog.kt

package com.example.helloandroid.ui.execute

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 同步计划确认对话框
 * 询问用户是否将训练中的修改同步到计划模板
 */
@Composable
fun SyncPlanDialog(
    planName: String,
    onSync: () -> Unit,
    onSkip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* 不允许点击外部关闭 */ },
        title = {
            Text(
                text = "📋 同步到计划？",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text(
                    text = "训练已完成并保存。",
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "是否将本次训练中的修改（动作、组数、重量、次数）同步到计划「$planName」？",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ 选择「同步」将覆盖计划的原始模板",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSync,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("同步修改")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onSkip
            ) {
                Text("仅保存记录")
            }
        }
    )
}