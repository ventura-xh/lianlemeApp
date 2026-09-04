package com.example.helloandroid.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.helloandroid.entity.TrainingSessionEntity
import com.example.helloandroid.entity.model.formatDate
import com.example.helloandroid.navigation.Screen
import com.example.helloandroid.viewmodel.CalendarViewModel
import java.util.Calendar
import java.util.Date

@Composable
fun PageCalendar(
    navController: NavHostController,
    viewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModel.factory
    )
) {
    var currentYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }

    var selectedDateSessions by remember { mutableStateOf<List<TrainingSessionEntity>?>(null) }

    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()

    // 加载所有训练记录
    LaunchedEffect(Unit) {
        viewModel.loadAllSessions()
    }

    Scaffold(
        topBar = {
            CalendarTopBar(
                year = currentYear,
                month = currentMonth,
                onPrevMonth = {
                    if (currentMonth == 0) {
                        currentMonth = 11
                        currentYear--
                    } else {
                        currentMonth--
                    }
                },
                onNextMonth = {
                    if (currentMonth == 11) {
                        currentMonth = 0
                        currentYear++
                    } else {
                        currentMonth++
                    }
                },
                onMonthReport = {
                    // TODO: 月报功能
                },
                onStatistics = {
                    // TODO: 统计功能
                    navController.navigate(Screen.Statistics.route)  // ✅ 跳转到统计页面
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
        ) {
            // 星期标题
            val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        fontSize = 14.sp,
                        fontWeight = if (day == "日" || day == "六") FontWeight.Bold else FontWeight.Normal,
                        color = when (day) {
                            "日" -> MaterialTheme.colorScheme.error
                            "六" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            CalendarGrid(
                year = currentYear,
                month = currentMonth,
                sessions = allSessions,
                onDateClick = { sessions ->
                    // ✅ 点击日期，显示底部弹窗
                    if (sessions.isNotEmpty()) {
                        selectedDateSessions = sessions
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp)
            )
        }
    }

    // 底部弹窗
    if (selectedDateSessions != null) {
        DayTrainingBottomSheet(
            sessions = selectedDateSessions!!,
            currentIndex = 0,
            onDismiss = {
                selectedDateSessions = null
            },
            navController = navController
        )
    }
}

// ✅ CalendarTopBar 函数定义
@Composable
fun CalendarTopBar(
    year: Int,
    month: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthReport: () -> Unit,
    onStatistics: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：年月 + 切换按钮
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            IconButton(
                onClick = onPrevMonth,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "上个月",
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "${year}年 ${month + 1}月",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(130.dp),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = onNextMonth,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "下个月",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 右侧：月报和统计按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onMonthReport,
                modifier = Modifier.height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.InsertChart,
                    contentDescription = "月报",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("月报", fontSize = 12.sp)
            }

            Button(
                onClick = onStatistics,
                modifier = Modifier.height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "统计",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("统计", fontSize = 12.sp)
            }
        }
    }
}

// ✅ 日历网格
@Composable
fun CalendarGrid(
    year: Int,
    month: Int,
    sessions: List<TrainingSessionEntity>,
    onDateClick: (List<TrainingSessionEntity>) -> Unit,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance().apply {
        set(year, month, 1)
    }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    val totalCells = daysInMonth + firstDayOfWeek - 1
    val totalRows = (totalCells + 6) / 7
    val totalGridCells = totalRows * 7

    // ✅ 按日期分组训练记录
    val sessionsByDate = sessions.groupBy {
        formatDate(it.startTime)  // yyyy-MM-dd
    }

    val days = mutableListOf<CalendarDay>()

    // 上月补全
    val prevMonthCalendar = Calendar.getInstance().apply {
        set(year, month, 1)
        add(Calendar.DAY_OF_MONTH, -(firstDayOfWeek - 1))
    }

    for (i in 0 until firstDayOfWeek - 1) {
        val date = prevMonthCalendar.time
        val dateKey = formatDate(date.time)
        days.add(
            CalendarDay(
                day = prevMonthCalendar.get(Calendar.DAY_OF_MONTH),
                isCurrentMonth = false,
                date = date,
                sessions = sessionsByDate[dateKey] ?: emptyList()
            )
        )
        prevMonthCalendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    // 当月日期
    for (day in 1..daysInMonth) {
        val date = Calendar.getInstance().apply {
            set(year, month, day)
        }.time
        val dateKey = formatDate(date.time)
        days.add(
            CalendarDay(
                day = day,
                isCurrentMonth = true,
                date = date,
                sessions = sessionsByDate[dateKey] ?: emptyList()
            )
        )
    }

    // 补全下月日期
    val remaining = totalGridCells - days.size
    val nextMonthCalendar = Calendar.getInstance().apply {
        set(year, month, daysInMonth)
        add(Calendar.DAY_OF_MONTH, 1)
    }
    for (i in 0 until remaining) {
        val date = nextMonthCalendar.time
        val dateKey = formatDate(date.time)
        days.add(
            CalendarDay(
                day = nextMonthCalendar.get(Calendar.DAY_OF_MONTH),
                isCurrentMonth = false,
                date = date,
                sessions = sessionsByDate[dateKey] ?: emptyList()
            )
        )
        nextMonthCalendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        items(days) { day ->
            CalendarDayItem(
                day = day,
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth(),
                onClick = {
                    onDateClick(day.sessions)
                }
            )
        }
    }
}

// ✅ 日期数据类
data class CalendarDay(
    val day: Int,
    val isCurrentMonth: Boolean,
    val date: Date,
    val sessions: List<TrainingSessionEntity> = emptyList()  // ✅ 该日期的训练记录
)

// ✅ 日期格子
@Composable
fun CalendarDayItem(
    day: CalendarDay,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val hasSessions = day.sessions.isNotEmpty()
    // ✅ 显示第一个计划名称（最多3个汉字）
    val displayName = if (hasSessions) {
        day.sessions.first().planName.take(3)
    } else ""

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (hasSessions) {
                    MaterialTheme.colorScheme.primaryContainer
                } else if (!day.isCurrentMonth) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                } else {
                    Color.Transparent
                }
            )
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            // 日期数字
            Text(
                text = day.day.toString(),
                fontSize = if (day.isCurrentMonth) 15.sp else 12.sp,
                color = if (day.isCurrentMonth) {
                    if (hasSessions) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                },
                fontWeight = if (hasSessions) FontWeight.Bold else FontWeight.Normal
            )

            // ✅ 显示计划名称（最多3个汉字）
            if (hasSessions) {
                Text(
                    text = displayName,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPageCalendar() {
    MaterialTheme {
        // ✅ 使用 rememberNavController() 创建模拟 NavController
        PageCalendar(
            navController = rememberNavController()
        )
    }
}