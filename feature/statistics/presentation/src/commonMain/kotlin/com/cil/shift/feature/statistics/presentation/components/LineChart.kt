package com.cil.shift.feature.statistics.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChartData(
    val label: String,
    val value: Float
)

@Composable
fun LineChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF4E7CFF),
    gridColor: Color = Color.White.copy(alpha = 0.1f),
    labelColor: Color = Color.White.copy(alpha = 0.6f)
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1A2942), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Monthly Progress",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (data.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No data available",
                    color = labelColor
                )
            }
            return@Column
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val width = size.width
            val height = size.height
            val padding = 40f
            val chartWidth = width - padding * 2
            val chartHeight = height - padding * 2

            // Find max value for scaling
            val maxValue = data.maxOfOrNull { it.value } ?: 1f
            val minValue = 0f

            // Draw horizontal grid lines
            val gridLines = 5
            for (i in 0..gridLines) {
                val y = padding + (chartHeight * i / gridLines)
                drawLine(
                    color = gridColor,
                    start = Offset(padding, y),
                    end = Offset(width - padding, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }

            // Draw line chart
            if (data.size > 1) {
                val path = Path()
                val stepX = chartWidth / (data.size - 1)

                data.forEachIndexed { index, chartData ->
                    val x = padding + (stepX * index)
                    val normalizedValue = if (maxValue > minValue) {
                        (chartData.value - minValue) / (maxValue - minValue)
                    } else {
                        0f
                    }
                    val y = padding + chartHeight - (normalizedValue * chartHeight)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }

                    // Draw points
                    drawCircle(
                        color = lineColor,
                        radius = 6f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3f,
                        center = Offset(x, y)
                    )
                }

                // Draw the line
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = 3f,
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        // Labels - show every 5th day to avoid overcrowding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Show labels at intervals to avoid overcrowding (1, 5, 10, 15, 20, 25, 30)
            val labelIndices = if (data.size > 7) {
                (0 until data.size step 5).toList() + if (data.size - 1 !in (0 until data.size step 5)) listOf(data.size - 1) else emptyList()
            } else {
                data.indices.toList()
            }

            labelIndices.forEach { index ->
                if (index < data.size) {
                    Text(
                        text = data[index].label,
                        fontSize = 10.sp,
                        color = labelColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
