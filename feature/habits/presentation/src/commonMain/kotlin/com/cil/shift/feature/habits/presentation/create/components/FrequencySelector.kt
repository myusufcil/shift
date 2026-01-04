package com.cil.shift.feature.habits.presentation.create.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.feature.habits.domain.model.Frequency
import com.cil.shift.feature.habits.presentation.create.daysOfWeek
import kotlinx.datetime.DayOfWeek

@Composable
fun FrequencySelector(
    frequency: Frequency,
    onFrequencyChanged: (Frequency) -> Unit,
    onWeekdayToggled: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Frequency",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FrequencyChip(
                text = "Daily",
                isSelected = frequency is Frequency.Daily,
                onClick = { onFrequencyChanged(Frequency.Daily) },
                modifier = Modifier.weight(1f)
            )
            FrequencyChip(
                text = "Weekly",
                isSelected = frequency is Frequency.Weekly,
                onClick = { onFrequencyChanged(Frequency.Weekly(emptyList())) },
                modifier = Modifier.weight(1f)
            )
        }

        if (frequency is Frequency.Weekly) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                daysOfWeek.forEach { day ->
                    DayChip(
                        day = day,
                        isSelected = day in frequency.days,
                        onClick = { onWeekdayToggled(day) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FrequencyChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isSelected) Color(0xFF00D9FF)
                else Color(0xFF1A2942)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color.Black else Color.White
        )
    }
}

@Composable
private fun DayChip(
    day: DayOfWeek,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) Color(0xFF00D9FF)
                else Color(0xFF1A2942)
            )
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.name.take(1),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.7f)
        )
    }
}
