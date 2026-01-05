package com.cil.shift.feature.habits.presentation.home.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeeklyProgressChart(
    weeklyData: List<Pair<String, Float>>,
    chartType: com.cil.shift.feature.habits.presentation.home.WeeklyChartType = com.cil.shift.feature.habits.presentation.home.WeeklyChartType.LINE,
    selectedDayIndex: Int? = null,
    onChartTypeChange: (com.cil.shift.feature.habits.presentation.home.WeeklyChartType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(cardColor, RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "This Week",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.8f)
            )

            // Chart type selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ChartTypeButton(
                    text = "Line",
                    isSelected = chartType == com.cil.shift.feature.habits.presentation.home.WeeklyChartType.LINE,
                    onClick = { onChartTypeChange(com.cil.shift.feature.habits.presentation.home.WeeklyChartType.LINE) }
                )
                ChartTypeButton(
                    text = "Bar",
                    isSelected = chartType == com.cil.shift.feature.habits.presentation.home.WeeklyChartType.BAR,
                    onClick = { onChartTypeChange(com.cil.shift.feature.habits.presentation.home.WeeklyChartType.BAR) }
                )
                ChartTypeButton(
                    text = "Pie",
                    isSelected = chartType == com.cil.shift.feature.habits.presentation.home.WeeklyChartType.PIE,
                    onClick = { onChartTypeChange(com.cil.shift.feature.habits.presentation.home.WeeklyChartType.PIE) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (weeklyData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No data",
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
            return@Column
        }

        AnimatedContent(
            targetState = chartType,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) +
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { it / 2 }
                    ) togetherWith fadeOut(animationSpec = tween(300)) +
                    slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { -it / 2 }
                    )
            },
            label = "chart_type_animation"
        ) { type ->
            when (type) {
                com.cil.shift.feature.habits.presentation.home.WeeklyChartType.LINE -> {
                    AnimatedLineChart(weeklyData, selectedDayIndex)
                }
                com.cil.shift.feature.habits.presentation.home.WeeklyChartType.BAR -> {
                    AnimatedBarChart(weeklyData, selectedDayIndex)
                }
                com.cil.shift.feature.habits.presentation.home.WeeklyChartType.PIE -> {
                    AnimatedPieChart(weeklyData)
                }
            }
        }

        // Day labels - only show for line and bar charts
        if (chartType != com.cil.shift.feature.habits.presentation.home.WeeklyChartType.PIE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyData.forEach { data ->
                    Text(
                        text = data.first,
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            // Legend for pie chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weeklyData.take(4).forEachIndexed { index, data ->
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
                            text = data.first,
                            fontSize = 10.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartTypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val scale = remember { Animatable(1f) }

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

    androidx.compose.material3.TextButton(
        onClick = onClick,
        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
            containerColor = if (isSelected) Color(0xFF4E7CFF) else Color.Transparent,
            contentColor = if (isSelected) Color.White else textColor.copy(alpha = 0.6f)
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
private fun AnimatedLineChart(weeklyData: List<Pair<String, Float>>, selectedDayIndex: Int? = null) {
    // Animate each data point
    val animatedValues = weeklyData.map { (_, value) ->
        val animatedValue = remember { Animatable(0f) }
        LaunchedEffect(value) {
            animatedValue.animateTo(
                targetValue = value,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        animatedValue.value
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val width = size.width
        val height = size.height
        val paddingHorizontal = 20f
        val paddingTop = 30f // More top padding for glow effects
        val paddingBottom = 10f
        val chartWidth = width - paddingHorizontal * 2
        val chartHeight = height - paddingTop - paddingBottom

        // Draw line
        if (animatedValues.size > 1) {
            val path = Path()
            val stepX = chartWidth / (animatedValues.size - 1)

            animatedValues.forEachIndexed { index, value ->
                val x = paddingHorizontal + (stepX * index)
                val y = paddingTop + chartHeight - (value * chartHeight)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                // Highlight selected day
                val isSelected = selectedDayIndex == index

                // Draw stronger glow effect for selected day (multiple layers)
                if (isSelected) {
                    // Outer glow
                    drawCircle(
                        color = Color(0xFF00D9FF).copy(alpha = 0.2f),
                        radius = 18f,
                        center = Offset(x, y)
                    )
                    // Middle glow
                    drawCircle(
                        color = Color(0xFF00D9FF).copy(alpha = 0.4f),
                        radius = 13f,
                        center = Offset(x, y)
                    )
                }

                // Draw points
                drawCircle(
                    color = if (isSelected) Color(0xFF00D9FF) else Color(0xFF4E7CFF),
                    radius = if (isSelected) 10f else 5f,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 4f else 2f,
                    center = Offset(x, y)
                )
            }

            drawPath(
                path = path,
                color = Color(0xFF4E7CFF),
                style = Stroke(
                    width = 2.5f,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

@Composable
private fun AnimatedBarChart(weeklyData: List<Pair<String, Float>>, selectedDayIndex: Int? = null) {
    // Animate each bar height
    val animatedHeights = weeklyData.map { (_, value) ->
        val animatedValue = remember { Animatable(0f) }
        LaunchedEffect(value) {
            animatedValue.animateTo(
                targetValue = value,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        animatedValue.value
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val width = size.width
        val height = size.height
        val padding = 20f
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2
        val barWidth = chartWidth / weeklyData.size * 0.6f

        animatedHeights.forEachIndexed { index, animatedHeight ->
            val isSelected = selectedDayIndex == index
            val x = padding + (chartWidth / weeklyData.size * index) + (chartWidth / weeklyData.size - barWidth) / 2
            val barHeight = animatedHeight * chartHeight

            // Draw stronger glow effect for selected bar (multiple layers)
            if (isSelected) {
                // Outer glow
                drawRect(
                    color = Color(0xFF00D9FF).copy(alpha = 0.15f),
                    topLeft = Offset(x - 8f, padding + chartHeight - barHeight - 8f),
                    size = androidx.compose.ui.geometry.Size(barWidth + 16f, barHeight + 8f)
                )
                // Inner glow
                drawRect(
                    color = Color(0xFF00D9FF).copy(alpha = 0.3f),
                    topLeft = Offset(x - 4f, padding + chartHeight - barHeight - 4f),
                    size = androidx.compose.ui.geometry.Size(barWidth + 8f, barHeight + 4f)
                )
            }

            drawRect(
                color = if (isSelected) Color(0xFF00D9FF) else Color(0xFF4E7CFF),
                topLeft = Offset(x, padding + chartHeight - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
private fun AnimatedPieChart(weeklyData: List<Pair<String, Float>>) {
    val cardColor = MaterialTheme.colorScheme.surface
    val total = weeklyData.sumOf { it.second.toDouble() }.toFloat()

    // Animate each slice
    val animatedSlices = weeklyData.map { (_, value) ->
        val animatedValue = remember { Animatable(0f) }
        LaunchedEffect(value) {
            animatedValue.animateTo(
                targetValue = value,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        animatedValue.value
    }

    val centerColor = cardColor

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val width = size.width
        val height = size.height
        val radius = minOf(width, height) / 3
        val center = Offset(width / 2, height / 2)

        val animatedTotal = animatedSlices.sum()
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

        animatedSlices.forEachIndexed { index, animatedValue ->
            val sweepAngle = if (animatedTotal > 0f) {
                (animatedValue / animatedTotal) * 360f
            } else {
                0f
            }

            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )

            // Draw border
            drawArc(
                color = centerColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = 2f)
            )

            startAngle += sweepAngle
        }

        // Center circle (donut effect)
        drawCircle(
            color = centerColor,
            radius = radius * 0.5f,
            center = center
        )
    }
}
