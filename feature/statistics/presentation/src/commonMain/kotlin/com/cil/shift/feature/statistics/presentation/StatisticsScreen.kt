package com.cil.shift.feature.statistics.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.cil.shift.core.common.localization.Language
import com.cil.shift.core.common.localization.LocalizationHelpers
import com.cil.shift.core.common.localization.LocalizationManager
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
    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()
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
                    // Header
                    Text(
                        text = StringResources.statistics.localized(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    // Streak Calendar
                    StreakCalendarCard(
                        completedDates = state.completedDates,
                        completionRatesByDate = state.completionRatesByDate,
                        currentStreak = state.currentStreak,
                        longestStreak = state.longestStreak
                    )

                    // Unified Progress Chart (Weekly/Monthly Toggle)
                    UnifiedProgressChart(
                        weeklyData = state.weeklyData,
                        monthlyData = state.monthlyData,
                        selectedWeekStart = state.selectedWeekStart,
                        selectedMonthStart = state.selectedMonthStart,
                        chartPeriod = state.chartPeriod,
                        chartType = if (state.chartPeriod == ChartPeriod.WEEKLY) state.weeklyChartType else state.monthlyChartType,
                        currentLanguage = currentLanguage,
                        onPreviousPeriod = {
                            if (state.chartPeriod == ChartPeriod.WEEKLY) {
                                viewModel.onEvent(StatisticsEvent.PreviousWeek)
                            } else {
                                viewModel.onEvent(StatisticsEvent.PreviousMonth)
                            }
                        },
                        onNextPeriod = {
                            if (state.chartPeriod == ChartPeriod.WEEKLY) {
                                viewModel.onEvent(StatisticsEvent.NextWeek)
                            } else {
                                viewModel.onEvent(StatisticsEvent.NextMonth)
                            }
                        },
                        onPeriodChange = { viewModel.onEvent(StatisticsEvent.ChangeChartPeriod(it)) },
                        onChartTypeChange = { type ->
                            if (state.chartPeriod == ChartPeriod.WEEKLY) {
                                viewModel.onEvent(StatisticsEvent.ChangeWeeklyChartType(type))
                            } else {
                                viewModel.onEvent(StatisticsEvent.ChangeMonthlyChartType(type))
                            }
                        }
                    )


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
    completionRatesByDate: Map<kotlinx.datetime.LocalDate, Float>,
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
                            completionRatesByDate = completionRatesByDate,
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
    completionRatesByDate: Map<kotlinx.datetime.LocalDate, Float>,
    today: kotlinx.datetime.LocalDate
) {
    val firstDayOfMonth = kotlinx.datetime.LocalDate(year, month, 1)
    val daysInMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).dayOfMonth
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal // Monday = 0

    val textColor = MaterialTheme.colorScheme.onBackground
    val completedColor = Color(0xFF4ECDC4)
    val todayBorderColor = Color(0xFFFFD700) // Gold for today

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
                            val completionRate = completionRatesByDate[date]
                            val isToday = date == today
                            val isFuture = date > today

                            // Calculate color based on completion rate
                            val backgroundColor = when {
                                completionRate != null && completionRate > 0f -> {
                                    // Color intensity based on completion rate (0.3 to 1.0 alpha)
                                    completedColor.copy(alpha = 0.3f + (completionRate * 0.7f))
                                }
                                isFuture -> textColor.copy(alpha = 0.05f) // Subtle bg for future days
                                else -> textColor.copy(alpha = 0.1f) // Past days without completion
                            }

                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        color = backgroundColor,
                                        shape = RoundedCornerShape(2.dp)
                                    )
                                    .then(
                                        if (isToday) Modifier.border(
                                            width = 1.5.dp,
                                            color = todayBorderColor,
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
    currentLanguage: Language,
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
                            val startMonth = LocalizationHelpers.getMonthName(weekStart.monthNumber, currentLanguage).take(3)
                            val endMonth = LocalizationHelpers.getMonthName(weekEnd.monthNumber, currentLanguage).take(3)
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
                    onTypeSelected = onChartTypeChange,
                    currentLanguage = currentLanguage
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
    onTypeSelected: (ChartType) -> Unit,
    currentLanguage: Language
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ChartTypeSelectorButton(
            text = StringResources.chartLine.get(currentLanguage),
            isSelected = selectedType == ChartType.LINE,
            onClick = { onTypeSelected(ChartType.LINE) }
        )
        ChartTypeSelectorButton(
            text = StringResources.chartBar.get(currentLanguage),
            isSelected = selectedType == ChartType.BAR,
            onClick = { onTypeSelected(ChartType.BAR) }
        )
        ChartTypeSelectorButton(
            text = StringResources.chartPie.get(currentLanguage),
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
    val accentColor = Color(0xFF4ECDC4)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) accentColor else textColor.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        // Underline indicator
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(2.dp)
                .background(
                    color = if (isSelected) accentColor else Color.Transparent,
                    shape = RoundedCornerShape(1.dp)
                )
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

@Composable
private fun UnifiedProgressChart(
    weeklyData: List<DayCompletion>,
    monthlyData: List<DayCompletion>,
    selectedWeekStart: kotlinx.datetime.LocalDate?,
    selectedMonthStart: kotlinx.datetime.LocalDate?,
    chartPeriod: ChartPeriod,
    chartType: ChartType,
    currentLanguage: Language,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onPeriodChange: (ChartPeriod) -> Unit,
    onChartTypeChange: (ChartType) -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    val data = if (chartPeriod == ChartPeriod.WEEKLY) weeklyData else monthlyData

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with navigation and period toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousPeriod) {
                    Text("←", fontSize = 20.sp, color = textColor)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Period Toggle (Weekly/Monthly)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PeriodToggleButton(
                            text = StringResources.weekly.get(currentLanguage),
                            isSelected = chartPeriod == ChartPeriod.WEEKLY,
                            onClick = { onPeriodChange(ChartPeriod.WEEKLY) }
                        )
                        PeriodToggleButton(
                            text = StringResources.monthly.get(currentLanguage),
                            isSelected = chartPeriod == ChartPeriod.MONTHLY,
                            onClick = { onPeriodChange(ChartPeriod.MONTHLY) }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Date range display
                    Text(
                        text = if (chartPeriod == ChartPeriod.WEEKLY) {
                            selectedWeekStart?.let { weekStart ->
                                val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
                                val startMonth = LocalizationHelpers.getMonthName(weekStart.monthNumber, currentLanguage).take(3)
                                val endMonth = LocalizationHelpers.getMonthName(weekEnd.monthNumber, currentLanguage).take(3)
                                if (startMonth == endMonth) {
                                    "$startMonth ${weekStart.dayOfMonth} - ${weekEnd.dayOfMonth}"
                                } else {
                                    "$startMonth ${weekStart.dayOfMonth} - $endMonth ${weekEnd.dayOfMonth}"
                                }
                            } ?: ""
                        } else {
                            selectedMonthStart?.let {
                                "${LocalizationHelpers.getMonthName(it.monthNumber, currentLanguage)} ${it.year}"
                            } ?: ""
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }

                IconButton(onClick = onNextPeriod) {
                    Text("→", fontSize = 20.sp, color = textColor)
                }
            }

            // Chart type selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ChartTypeSelector(
                    selectedType = chartType,
                    onTypeSelected = onChartTypeChange,
                    currentLanguage = currentLanguage
                )
            }

            // Chart content with animation
            AnimatedContent(
                targetState = Pair(chartPeriod, chartType),
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) +
                        slideInHorizontally(
                            animationSpec = tween(300),
                            initialOffsetX = { if (targetState.first != initialState.first) it / 4 else 0 }
                        ) togetherWith fadeOut(animationSpec = tween(300)) +
                        slideOutHorizontally(
                            animationSpec = tween(300),
                            targetOffsetX = { if (targetState.first != initialState.first) -it / 4 else 0 }
                        )
                },
                label = "chart_animation"
            ) { (period, type) ->
                val chartData = if (period == ChartPeriod.WEEKLY) weeklyData else monthlyData
                when (type) {
                    ChartType.LINE -> {
                        LineChart(
                            data = chartData.map { ChartData(it.dayName, it.completionRate * 100) }
                        )
                    }
                    ChartType.BAR -> {
                        if (period == ChartPeriod.WEEKLY) {
                            WeeklyBarChart(weeklyData = chartData)
                        } else {
                            MonthlyBarChart(monthlyData = chartData)
                        }
                    }
                    ChartType.PIE -> {
                        if (period == ChartPeriod.WEEKLY) {
                            WeeklyPieChart(weeklyData = chartData)
                        } else {
                            MonthlyPieChart(monthlyData = chartData)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val textColorBase = MaterialTheme.colorScheme.onBackground
    val accentColor = Color(0xFF4ECDC4)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) accentColor else textColorBase.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Underline indicator
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(2.dp)
                .background(
                    color = if (isSelected) accentColor else Color.Transparent,
                    shape = RoundedCornerShape(1.dp)
                )
        )
    }
}

@Composable
private fun WeeklyBarChart(weeklyData: List<DayCompletion>) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val maxRate = weeklyData.maxOfOrNull { it.completionRate }?.coerceAtLeast(0.01f) ?: 1f
    val barHeight = 120.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        weeklyData.forEach { day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.width(36.dp)
            ) {
                // Fixed height container for bars with bottom alignment
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(barHeight),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val animatedRate = androidx.compose.animation.core.animateFloatAsState(
                        targetValue = (day.completionRate / maxRate).coerceIn(0f, 1f),
                        animationSpec = tween(600, easing = FastOutSlowInEasing),
                        label = "bar_height"
                    )
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .fillMaxHeight(animatedRate.value.coerceAtLeast(0.03f))
                            .background(
                                Color(0xFF4ECDC4),
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = day.dayName.take(3),
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}
