package com.example.helloandroid.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.helloandroid.entity.TrainingSessionEntity
import com.example.helloandroid.entity.model.formatDuration
import com.example.helloandroid.viewmodel.MonthlyReportViewModel
import com.example.helloandroid.viewmodel.MuscleStat
import com.example.helloandroid.viewmodel.WeeklyData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageMonthlyReport(
    navController: NavHostController,
    viewModel: MonthlyReportViewModel = viewModel(
        factory = MonthlyReportViewModel.factory
    )
) {
    val reportData by viewModel.reportData.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadMonthlyReport()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "📊 月报",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                modifier = Modifier.height(48.dp),
                windowInsets = WindowInsets(0,0,0,0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("加载中...")
            }
        } else if (reportData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("暂无训练数据", fontSize = 18.sp)
                    Text("本月还没有训练记录", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ✅ 月份标题
                item {
                    Text(
                        text = reportData!!.monthLabel,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // ✅ 统计概览卡片
                item {
                    StatisticsOverviewCard(
                        totalSessions = reportData!!.totalSessions,
                        totalDuration = reportData!!.totalDuration,
                        totalActions = reportData!!.totalActions,
                        totalGroups = reportData!!.totalGroups
                    )
                }

                // ✅ 每周训练次数趋势图
                item {
                    WeeklyTrendCard(
                        weeklyData = reportData!!.weeklyData
                    )
                }

                // ✅ 锻炼部位统计（肌肉）
                item {
                    MuscleDistributionCard(
                        muscleStats = reportData!!.muscleStats
                    )
                }

                // ✅ 每日训练记录
                item {
                    Text(
                        text = "📅 每日训练",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(reportData!!.dailySessions) { dayData ->
                    DailySessionItem(
                        date = dayData.date,
                        sessions = dayData.sessions
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// ✅ 统计概览卡片
@Composable
fun StatisticsOverviewCard(
    totalSessions: Int,
    totalDuration: Long,
    totalActions: Int,
    totalGroups: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.FitnessCenter,
                value = "$totalSessions",
                label = "训练次数"
            )
            StatItem(
                icon = Icons.Default.Timer,
                value = formatDuration(totalDuration),
                label = "总时长"
            )
            StatItem(
                icon = Icons.Default.FitnessCenter,
                value = "$totalActions",
                label = "总动作"
            )
            StatItem(
                icon = Icons.Default.FitnessCenter,
                value = "$totalGroups",
                label = "总组数"
            )
        }
    }
}

// ✅ 统计项
@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

// ✅ 每周趋势卡片
@Composable
fun WeeklyTrendCard(
    weeklyData: List<WeeklyData>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📈 每周训练趋势",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val maxSessions = weeklyData.maxOfOrNull { it.count }?.takeIf { it > 0 } ?: 1
            val chartHeight = 100.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { week ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // ✅ 数值标签
                        if (week.count > 0) {
                            Text(
                                text = "${week.count}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        // ✅ 柱子
                        val barHeight = if (week.count > 0) {
                            (week.count.toFloat() / maxSessions * chartHeight.value).dp
                                .coerceAtLeast(6.dp)
                        } else {
                            4.dp
                        }

                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(barHeight)
                                .background(
                                    color = if (week.count > 0) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                    },
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )

                        // ✅ 底部文字
                        Text(
                            text = "第${week.weekIndex}周",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ✅ 肌肉分布卡片
@Composable
fun MuscleDistributionCard(
    muscleStats: List<MuscleStat>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "🎯 锻炼部位分布",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (muscleStats.isEmpty()) {
                Text(
                    text = "暂无肌肉数据",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                muscleStats.forEach { stat ->
                    MuscleStatRow(
                        muscleName = stat.muscleName,
                        count = stat.count,
                        percentage = stat.percentage
                    )
                }
            }
        }
    }
}

// ✅ 肌肉统计行
@Composable
fun MuscleStatRow(
    muscleName: String,
    count: Int,
    percentage: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = muscleName,
            fontSize = 13.sp,
            modifier = Modifier.width(70.dp),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage / 100f)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
        Text(
            text = "$count 次",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

// ✅ 每日训练记录
@Composable
fun DailySessionItem(
    date: String,
    sessions: List<TrainingSessionEntity>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "📅 $date",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            sessions.forEach { session ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = session.planName,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatDuration(session.totalDuration),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}