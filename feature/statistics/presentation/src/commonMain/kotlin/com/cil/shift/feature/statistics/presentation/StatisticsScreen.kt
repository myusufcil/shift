package com.cil.shift.feature.statistics.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import com.cil.shift.core.common.localization.StringResources
import com.cil.shift.core.common.localization.localized
import com.cil.shift.feature.statistics.presentation.components.LineChart
import com.cil.shift.feature.statistics.presentation.components.ChartData
import com.cil.shift.feature.statistics.presentation.components.PieChart
import com.cil.shift.feature.statistics.presentation.components.PieChartData
import kotlinx.datetime.*
import org.koin.compose.koinInject

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF00D9FF)
                )
            }
            state.error != null -> {
                Text(
                    text = state.error ?: "Unknown error",
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header with greeting
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (state.userName.isNotBlank()) {
                            Text(
                                text = "Hello, ${state.userName}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = StringResources.statistics.localized(),
                                fontSize = 16.sp,
                                color = textColor.copy(alpha = 0.6f)
                            )
                        } else {
                            Text(
                                text = StringResources.statistics.localized(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }

                    // Streak Calendar
                    StreakCalendarCard(
                        completedDates = state.completedDates,
                        currentStreak = state.currentStreak,
                        longestStreak = state.longestStreak
                    )

                    // Weekly Distribution
                    WeeklyDistributionCard(
                        weeklyData = state.weeklyData,
                        selectedWeekStart = state.selectedWeekStart,
                        chartType = state.weeklyChartType,
                        onPreviousWeek = { viewModel.onEvent(StatisticsEvent.PreviousWeek) },
                        onNextWeek = { viewModel.onEvent(StatisticsEvent.NextWeek) },
                        onChartTypeChange = { viewModel.onEvent(StatisticsEvent.ChangeWeeklyChartType(it)) }
                    )

                    // Monthly Chart
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = textColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = cardColor
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.onEvent(StatisticsEvent.PreviousMonth) }) {
                                    Text("←", fontSize = 20.sp, color = textColor)
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = StringResources.monthlyProgress.localized(),
                                        fontSize = 12.sp,
                                        color = textColor.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = state.selectedMonthStart?.let {
                                            "${it.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${it.year}"
                                        } ?: "",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor
                                    )
                                }
                                IconButton(onClick = { viewModel.onEvent(StatisticsEvent.NextMonth) }) {
                                    Text("→", fontSize = 20.sp, color = textColor)
                                }
                            }

                            // Chart type selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ChartTypeSelector(
                                    selectedType = state.monthlyChartType,
                                    onTypeSelected = { viewModel.onEvent(StatisticsEvent.ChangeMonthlyChartType(it)) }
                                )
                            }

                            AnimatedContent(
                                targetState = state.monthlyChartType,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(400)) +
                                        slideInVertically(
                                            animationSpec = tween(400, easing = FastOutSlowInEasing),
                                            initialOffsetY = { it / 3 }
                                        ) togetherWith fadeOut(animationSpec = tween(400)) +
                                        slideOutVertically(
                                            animationSpec = tween(400, easing = FastOutSlowInEasing),
                                            targetOffsetY = { -it / 3 }
                                        )
                                },
                                label = "monthly_chart_animation"
                            ) { type ->
                                when (type) {
                                    ChartType.LINE -> {
                                        LineChart(
                                            data = state.monthlyData.map {
                                                ChartData(it.dayName, it.completionRate * 100)
                                            }
                                        )
                                    }
                                    ChartType.BAR -> {
                                        MonthlyBarChart(monthlyData = state.monthlyData)
                                    }
                                    ChartType.PIE -> {
                                        MonthlyPieChart(monthlyData = state.monthlyData)
                                    }
                                }
                            }
                        }
                    }


                    // Streak Card
                    StreakCard(currentStreak = state.currentStreak)

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun StreakCalendarCard(
    completedDates: Set<kotlinx.datetime.LocalDate>,
    currentStreak: Int,
    longestStreak: Int
) {
    val today = com.cil.shift.core.common.currentDate()
    val cardColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = StringResources.activityCalendar.localized(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            // Streak stats header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StreakStat(
                    value = currentStreak,
                    label = StringResources.current.localized(),
                    emoji = "🔥"
                )
                StreakStat(
                    value = longestStreak,
                    label = StringResources.best.localized(),
                    emoji = "🏆"
                )
                StreakStat(
                    value = completedDates.size,
                    label = StringResources.total.localized(),
                    emoji = "✅"
                )
            }

            // 4 months grid (1x4)
            val months = (0..3).map { offset ->
                val date = today.minus(3 - offset, DateTimeUnit.MONTH)
                date.year to date.month
            }

            // Single row with all 4 months
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                months.forEach { (year, month) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${year}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        MiniMonthCalendarGrid(
                            year = year,
                            month = month,
                            completedDates = completedDates,
                            today = today
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniMonthCalendarGrid(
    year: Int,
    month: kotlinx.datetime.Month,
    completedDates: Set<kotlinx.datetime.LocalDate>,
    today: kotlinx.datetime.LocalDate
) {
    val firstDayOfMonth = kotlinx.datetime.LocalDate(year, month, 1)
    val daysInMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).dayOfMonth
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal // Monday = 0

    val textColor = MaterialTheme.colorScheme.onBackground

    Column(
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        // Days grid
        val weeksNeeded = ((daysInMonth + firstDayOfWeek + 6) / 7)

        repeat(weeksNeeded) { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                repeat(7) { dayOfWeek ->
                    val dayPosition = week * 7 + dayOfWeek
                    val dayNum = dayPosition - firstDayOfWeek + 1

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayNum in 1..daysInMonth) {
                            val date = kotlinx.datetime.LocalDate(year, month, dayNum)
                            val isCompleted = completedDates.contains(date)
                            val isToday = date == today
                            val isFuture = date > today

                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        color = when {
                                            isCompleted -> Color(0xFF4ECDC4)
                                            isFuture -> Color.Transparent
                                            else -> textColor.copy(alpha = 0.1f)
                                        },
                                        shape = RoundedCornerShape(2.dp)
                                    )
                                    .then(
                                        if (isToday) Modifier.border(
                                            width = 1.dp,
                                            color = Color(0xFF00D9FF),
                                            shape = RoundedCornerShape(2.dp)
                                        ) else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // No text - just colored boxes for mini view
                            }
                        } else {
                            Spacer(modifier = Modifier.size(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakStat(
    value: Int,
    label: String,
    emoji: String
) {
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp
        )
        Text(
            text = "$value",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00D9FF)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = textColor.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun WeeklyDistributionCard(
    weeklyData: List<DayCompletion>,
    selectedWeekStart: kotlinx.datetime.LocalDate?,
    chartType: ChartType,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onChartTypeChange: (ChartType) -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousWeek) {
                    Text("←", fontSize = 20.sp, color = textColor)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = StringResources.weeklyDistribution.localized(),
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.6f)
                    )
                    Text(
                        text = selectedWeekStart?.let { weekStart ->
                            val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
                            val startMonth = weekStart.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
                            val endMonth = weekEnd.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
                            val yearInfo = if (weekStart.year != weekEnd.year) {
                                " ${weekStart.year} - $endMonth ${weekEnd.dayOfMonth} ${weekEnd.year}"
                            } else {
                                " - $endMonth ${weekEnd.dayOfMonth}, ${weekEnd.year}"
                            }
                            "$startMonth ${weekStart.dayOfMonth}$yearInfo"
                        } ?: StringResources.weeklyDistribution.localized(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }
                IconButton(onClick = onNextWeek) {
                    Text("→", fontSize = 20.sp, color = textColor)
                }
            }

            // Chart type selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartTypeSelector(
                    selectedType = chartType,
                    onTypeSelected = onChartTypeChange
                )
            }

            AnimatedContent(
                targetState = chartType,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) +
                        slideInVertically(
                            animationSpec = tween(400, easing = FastOutSlowInEasing),
                            initialOffsetY = { it / 3 }
                        ) togetherWith fadeOut(animationSpec = tween(400)) +
                        slideOutVertically(
                            animationSpec = tween(400, easing = FastOutSlowInEasing),
                            targetOffsetY = { -it / 3 }
                        )
                },
                label = "weekly_chart_animation"
            ) { type ->
                when (type) {
                    ChartType.BAR -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            weeklyData.forEach { day ->
                                DayBar(
                                    dayName = day.dayName,
                                    completionRate = day.completionRate
                                )
                            }
                        }
                    }
                    ChartType.LINE -> {
                        WeeklyLineChart(weeklyData = weeklyData)
                    }
                    ChartType.PIE -> {
                        WeeklyPieChart(weeklyData = weeklyData)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayBar(
    dayName: String,
    completionRate: Float
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val animatedRate = androidx.compose.animation.core.animateFloatAsState(
        targetValue = completionRate.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "day_bar_height"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(100.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .fillMaxHeight(animatedRate.value.coerceIn(0f, 1f))
                    .background(
                        Color(0xFF00D9FF),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
            )
        }
        Text(
            text = dayName.take(1),
            fontSize = 12.sp,
            color = textColor.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun StreakCard(currentStreak: Int) {
    val cardColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val animatedStreak = androidx.compose.animation.core.animateIntAsState(
        targetValue = currentStreak.coerceAtLeast(0),
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "streak_count"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = StringResources.currentStreak.localized(),
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
                Text(
                    text = "${animatedStreak.value.coerceAtLeast(0)} ${StringResources.daysStreak.localized()}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00D9FF)
                )
            }

            Text(
                text = "🔥",
                fontSize = 48.sp
            )
        }
    }
}

@Composable
private fun ChartTypeSelector(
    selectedType: ChartType,
    onTypeSelected: (ChartType) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ChartTypeSelectorButton(
            text = "Line",
            isSelected = selectedType == ChartType.LINE,
            onClick = { onTypeSelected(ChartType.LINE) }
        )
        ChartTypeSelectorButton(
            text = "Bar",
            isSelected = selectedType == ChartType.BAR,
            onClick = { onTypeSelected(ChartType.BAR) }
        )
        ChartTypeSelectorButton(
            text = "Pie",
            isSelected = selectedType == ChartType.PIE,
            onClick = { onTypeSelected(ChartType.PIE) }
        )
    }
}

@Composable
private fun ChartTypeSelectorButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val scale = androidx.compose.runtime.remember { Animatable(1f) }

    androidx.compose.runtime.LaunchedEffect(isSelected) {
        if (isSelected) {
            scale.animateTo(
                targetValue = 1.1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (isSelected) Color(0xFF4E7CFF) else Color.Transparent,
            contentColor = if (isSelected) textColor else textColor.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier
            .height(24.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun WeeklyLineChart(weeklyData: List<DayCompletion>) {
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            val width = size.width
            val height = size.height
            val paddingHorizontal = 20f
            val paddingTop = 15f
            val paddingBottom = 15f
            val chartWidth = width - paddingHorizontal * 2
            val chartHeight = height - paddingTop - paddingBottom

            if (weeklyData.size > 1) {
                val path = androidx.compose.ui.graphics.Path()
                val stepX = chartWidth / (weeklyData.size - 1)

                weeklyData.forEachIndexed { index, data ->
                    val x = paddingHorizontal + (stepX * index)
                    val y = paddingTop + chartHeight - (data.completionRate.coerceIn(0f, 1f) * chartHeight)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }

                    // Draw points
                    drawCircle(
                        color = Color(0xFF00D9FF),
                        radius = 5f,
                        center = androidx.compose.ui.geometry.Offset(x, y)
                    )
                }

                drawPath(
                    path = path,
                    color = Color(0xFF00D9FF),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.5f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeklyData.forEach { data ->
                Text(
                    text = data.dayName,
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun WeeklyPieChart(weeklyData: List<DayCompletion>) {
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val width = size.width
        val height = size.height
        val radius = minOf(width, height) / 3
        val center = androidx.compose.ui.geometry.Offset(width / 2, height / 2)

        val total = weeklyData.sumOf { it.completionRate.toDouble() }.toFloat()
        var startAngle = -90f

        val colors = listOf(
            Color(0xFF4E7CFF),
            Color(0xFF00D9FF),
            Color(0xFF4ECDC4),
            Color(0xFFFF6B6B),
            Color(0xFFFFA07A),
            Color(0xFF98D8C8),
            Color(0xFFB4A7D6)
        )

        weeklyData.forEachIndexed { index, data ->
            val sweepAngle = (data.completionRate / total) * 360f

            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )

            drawArc(
                color = Color(0xFF1A2942),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            startAngle += sweepAngle
        }

        drawCircle(
            color = Color(0xFF1A2942),
            radius = radius * 0.5f,
            center = center
        )
    }
}

@Composable
private fun MonthlyBarChart(monthlyData: List<DayCompletion>) {
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val width = size.width
        val height = size.height
        val padding = 20f
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2
        val barWidth = chartWidth / monthlyData.size * 0.6f
        val barSpacing = chartWidth / monthlyData.size

        // Draw gradient background fill
        val gradientBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                Color(0xFF4ECDC4).copy(alpha = 0.3f),
                Color(0xFF4ECDC4).copy(alpha = 0.05f)
            ),
            startY = padding,
            endY = padding + chartHeight
        )
        drawRect(
            brush = gradientBrush,
            topLeft = androidx.compose.ui.geometry.Offset(padding, padding),
            size = androidx.compose.ui.geometry.Size(chartWidth, chartHeight)
        )

        // Draw bars with rounded tops
        monthlyData.forEachIndexed { index, data ->
            val x = padding + (barSpacing * index) + (barSpacing - barWidth) / 2
            val barHeight = (data.completionRate * chartHeight).coerceAtLeast(4f)
            val cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 3, barWidth / 3)

            // Draw bar with gradient
            val barGradient = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF4ECDC4),
                    Color(0xFF4ECDC4).copy(alpha = 0.7f)
                ),
                startY = padding + chartHeight - barHeight,
                endY = padding + chartHeight
            )

            drawRoundRect(
                brush = barGradient,
                topLeft = androidx.compose.ui.geometry.Offset(x, padding + chartHeight - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}

@Composable
private fun MonthlyPieChart(monthlyData: List<DayCompletion>) {
    // Group monthly data into weeks for better pie visualization
    val weeklyGroups = monthlyData.chunked(7)
    val weeklyAverages = weeklyGroups.mapIndexed { index, week ->
        "Week ${index + 1}" to week.map { it.completionRate }.average().toFloat()
    }

    val cardColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val width = size.width
        val height = size.height
        val radius = minOf(width, height) / 3
        val center = androidx.compose.ui.geometry.Offset(width / 2, height / 2)

        val total = weeklyAverages.sumOf { it.second.toDouble() }.toFloat()
        var startAngle = -90f

        val colors = listOf(
            Color(0xFF4ECDC4),
            Color(0xFF98D8C8),
            Color(0xFFB4A7D6),
            Color(0xFFFFA07A),
            Color(0xFF7FB3D5)
        )

        weeklyAverages.forEachIndexed { index, (_, value) ->
            val sweepAngle = (value / total) * 360f

            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )

            drawArc(
                color = cardColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            startAngle += sweepAngle
        }

        drawCircle(
            color = cardColor,
            radius = radius * 0.5f,
            center = center
        )
    }

    // Legend
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weeklyAverages.take(4).forEachIndexed { index, (label, _) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            when (index) {
                                0 -> Color(0xFF4ECDC4)
                                1 -> Color(0xFF98D8C8)
                                2 -> Color(0xFFB4A7D6)
                                3 -> Color(0xFFFFA07A)
                                else -> Color(0xFF7FB3D5)
                            },
                            androidx.compose.foundation.shape.CircleShape
                        )
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}
