package com.cil.shift.feature.statistics.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A1628))
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
                        color = Color.White
                    )

                    // Completion Rate Card
                    CompletionRateCard(
                        completionRate = state.completionRate,
                        completedToday = state.completedToday,
                        totalHabits = state.totalHabits
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
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A2942)
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
                                    Text("←", fontSize = 20.sp, color = Color.White)
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Monthly Progress",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = state.selectedMonthStart?.let {
                                            "${it.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${it.year}"
                                        } ?: "",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                                IconButton(onClick = { viewModel.onEvent(StatisticsEvent.NextMonth) }) {
                                    Text("→", fontSize = 20.sp, color = Color.White)
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

                    // Pie Chart - Completion Distribution
                    PieChart(
                        data = listOf(
                            PieChartData("Completed", state.completedToday.toFloat() * 100f / maxOf(state.totalHabits, 1), Color(0xFF4ECDC4)),
                            PieChartData("Pending", (state.totalHabits - state.completedToday).toFloat() * 100f / maxOf(state.totalHabits, 1), Color(0xFFFF6B6B))
                        ),
                        title = "Today's Completion"
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
private fun CompletionRateCard(
    completionRate: Float,
    completedToday: Int,
    totalHabits: Int
) {
    val animatedRate = androidx.compose.animation.core.animateFloatAsState(
        targetValue = completionRate,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "completion_rate"
    )

    val animatedCompletedToday = androidx.compose.animation.core.animateIntAsState(
        targetValue = completedToday,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "completed_today"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A2942)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = StringResources.todaysProgress.localized(),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.7f)
            )

            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { animatedRate.value },
                    modifier = Modifier.size(120.dp),
                    color = Color(0xFF00D9FF),
                    strokeWidth = 12.dp,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Text(
                    text = "${(animatedRate.value * 100).toInt()}%",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = "${animatedCompletedToday.value} ${StringResources.of.localized()} $totalHabits ${StringResources.habitsCompleted.localized()}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A2942)
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
                    Text("←", fontSize = 20.sp, color = Color.White)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = StringResources.weeklyDistribution.localized(),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
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
                        color = Color.White
                    )
                }
                IconButton(onClick = onNextWeek) {
                    Text("→", fontSize = 20.sp, color = Color.White)
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
    val animatedRate = androidx.compose.animation.core.animateFloatAsState(
        targetValue = completionRate,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
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
                    .fillMaxHeight(animatedRate.value)
                    .background(
                        Color(0xFF00D9FF),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
            )
        }
        Text(
            text = dayName.take(1),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun StreakCard(currentStreak: Int) {
    val animatedStreak = androidx.compose.animation.core.animateIntAsState(
        targetValue = currentStreak,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "streak_count"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A2942)
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
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = "${animatedStreak.value} ${StringResources.daysStreak.localized()}",
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
            contentColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
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
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val width = size.width
        val height = size.height
        val padding = 20f
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        if (weeklyData.size > 1) {
            val path = androidx.compose.ui.graphics.Path()
            val stepX = chartWidth / (weeklyData.size - 1)

            weeklyData.forEachIndexed { index, data ->
                val x = padding + (stepX * index)
                val y = padding + chartHeight - (data.completionRate * chartHeight)

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
                drawCircle(
                    color = Color.White,
                    radius = 2f,
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weeklyData.forEach { data ->
            Text(
                text = data.dayName,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
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
        val barWidth = chartWidth / monthlyData.size * 0.8f

        monthlyData.forEachIndexed { index, data ->
            val x = padding + (chartWidth / monthlyData.size * index) + (chartWidth / monthlyData.size - barWidth) / 2
            val barHeight = data.completionRate * chartHeight

            drawRect(
                color = Color(0xFF00D9FF),
                topLeft = androidx.compose.ui.geometry.Offset(x, padding + chartHeight - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
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
            Color(0xFF4E7CFF),
            Color(0xFF00D9FF),
            Color(0xFF4ECDC4),
            Color(0xFFFF6B6B),
            Color(0xFFFFA07A)
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
                                0 -> Color(0xFF4E7CFF)
                                1 -> Color(0xFF00D9FF)
                                2 -> Color(0xFF4ECDC4)
                                3 -> Color(0xFFFF6B6B)
                                else -> Color(0xFFFFA07A)
                            },
                            androidx.compose.foundation.shape.CircleShape
                        )
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}
