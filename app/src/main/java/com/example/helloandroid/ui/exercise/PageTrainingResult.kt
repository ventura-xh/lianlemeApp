package com.example.helloandroid.ui.exercise

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.helloandroid.entity.model.TrainingSessionWithDetails
import com.example.helloandroid.entity.model.formatTimestamp
import com.example.helloandroid.navigation.Screen
import com.example.helloandroid.utils.BitmapSaver
import com.example.helloandroid.viewmodel.TrainingResultViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageTrainingResult(
    sessionId: Long,
    navController: NavHostController,
    fromTrainingComplete: Boolean,
    viewModel: TrainingResultViewModel = viewModel(factory = TrainingResultViewModel.factory)
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var sessionDetail by remember { mutableStateOf<TrainingSessionWithDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // ✅ GraphicsLayer 仅作用于需要截图的内容区域
    val graphicsLayer = rememberGraphicsLayer()

    LaunchedEffect(sessionId) {
        isLoading = true
        sessionDetail = viewModel.getSessionDetail(sessionId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (fromTrainingComplete) "训练完成" else "训练详情",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // ✅ 根据来源决定返回方式
                        if (!fromTrainingComplete) {
                            // 从日历进入，直接 popBackStack
                            navController.popBackStack()
                        } else {
                            // 从训练完成进入，回到计划列表
                            navController.popBackStack(Screen.TrainingPlan.route, inclusive = false)
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                modifier = Modifier.height(48.dp),
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,  // ✅ 透明背景
                    titleContentColor = MaterialTheme.colorScheme.onSurface,  // ✅ 文字颜色
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface  // ✅ 图标颜色
                )
            )
        },
        bottomBar = {
            // ✅ 底部按钮放在 Scaffold bottomBar，不会被截入图中
            TrainingResultBottomBar(
                isSaving = isSaving,
                onSaveImage = {
                    coroutineScope.launch {
                        isSaving = true
                        try {
                            val bitmap = captureFromGraphicsLayer(graphicsLayer)
                            if (bitmap != null && sessionDetail != null) {
                                val fileName = BitmapSaver.generateFileName(
                                    sessionDetail!!.session.planName,
                                    ".jpg")
                                val success = BitmapSaver.saveBitmapToGallery(
                                    context,
                                    bitmap,
                                    fileName,
                                    Bitmap.CompressFormat.JPEG)
                                Toast.makeText(
                                    context,
                                    if (success) "图片已保存到相册" else "保存失败，请重试",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(context, "截图失败，内容可能未加载完成", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "保存失败：${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isSaving = false
                        }
                    }
                },
                onBack = {
                    // ✅ 根据来源决定返回方式
                    if (!fromTrainingComplete) {
                        // 从日历进入，直接 popBackStack
                        navController.popBackStack()
                    } else {
                        // 从训练完成进入，回到计划列表
                        navController.popBackStack(Screen.TrainingPlan.route, inclusive = false)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                sessionDetail == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("未找到训练记录")
                    }
                }
                else -> {
                    // ✅ 可滚动的截图内容区域
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .background(MaterialTheme.colorScheme.surface)  // ✅ 添加背景色
                            .drawWithContent {
                                graphicsLayer.record {
                                    this@drawWithContent.drawContent()
                                }
                                drawContent()
                            }
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        TrainingResultOverview(session = sessionDetail!!)
                        Spacer(modifier = Modifier.height(12.dp))

                        sessionDetail!!.actions.forEach { actionWithDetails ->
                            TrainingResultActionCard(
                                actionName = actionWithDetails.action.actionName,
                                groups = actionWithDetails.details
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // 底部留白，避免截图裁切到边缘
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

/**
 * ✅ 纯 Column 展示概览，不使用 LazyColumn
 * LazyColumn 是懒加载的，无法被 GraphicsLayer 完整捕获
 */
@Composable
fun TrainingResultOverview(
    session: TrainingSessionWithDetails,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "📋 ${session.session.planName}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "⏱️ 用时: ${formatDuration(session.session.totalDuration)}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "📅 ${formatTimestamp(session.session.startTime)}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "🏋️ 共 ${session.actions.size} 个动作",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TrainingResultActionCard(
    actionName: String,
    groups: List<com.example.helloandroid.entity.TrainingSessionActionDetailEntity>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = actionName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 表头
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("组数", "重量", "次数", "状态").forEach { header ->
                    Text(
                        text = header,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            groups.forEach { group ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${group.groupIndex + 1}", fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text("${group.weight}kg", fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text("${group.reps}次", fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text(if (group.isCompleted) "✅" else "❌", fontSize = 14.sp, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun TrainingResultBottomBar(
    isSaving: Boolean,
    onSaveImage: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onSaveImage,
            modifier = Modifier.weight(1f).height(48.dp),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("保存图片")
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier.weight(1f).height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text("返回")
        }
    }
}

/**
 * ✅ 简化截图函数：不再需要手动传入 size
 * GraphicsLayer.toImageBitmap() 会自动使用录制时的实际内容尺寸
 */
private suspend fun captureFromGraphicsLayer(graphicsLayer: GraphicsLayer): Bitmap? {
    return withContext(Dispatchers.Main) {
        try {
            // toImageBitmap() 内部会使用 layer 录制的实际像素尺寸
            // 无需外部传入 IntSize，避免了尺寸不匹配的问题
            graphicsLayer.toImageBitmap().asAndroidBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
        hours > 0 -> String.format("%d时%d分%d秒", hours, minutes, secs)
        minutes > 0 -> String.format("%d分%d秒", minutes, secs)
        else -> String.format("%d秒", secs)
    }
}