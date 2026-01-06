package com.cil.shift.feature.habits.presentation.create.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.feature.habits.domain.model.Frequency
import kotlinx.datetime.DayOfWeek

@Composable
fun Step2Schedule(
    frequency: Frequency,
    timeOfDay: com.cil.shift.feature.habits.presentation.create.TimeOfDay,
    hasReminder: Boolean,
    reminderTime: String?,
    reminderTimes: List<String>,
    onFrequencyChange: (Frequency) -> Unit,
    onTimeOfDayChange: (com.cil.shift.feature.habits.presentation.create.TimeOfDay) -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onReminderTimeChange: (String) -> Unit,
    onReminderTimeAdded: (String) -> Unit,
    onReminderTimeRemoved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    val accentColor = Color(0xFF4E7CFF)

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
        // Title
        Text(
            text = "Set your schedule",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Text(
            text = "Consistency is key. Choose a rhythm that works for you.",
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.7f),
            lineHeight = 20.sp
        )

        // Frequency selector
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Frequency",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FrequencyChip(
                    text = "Everyday",
                    isSelected = frequency is Frequency.Daily,
                    onClick = { onFrequencyChange(Frequency.Daily) },
                    modifier = Modifier.weight(1f),
                    textColor = textColor,
                    cardColor = cardColor,
                    accentColor = accentColor
                )

                FrequencyChip(
                    text = "Specific Days",
                    isSelected = frequency is Frequency.Weekly,
                    onClick = {
                        onFrequencyChange(Frequency.Weekly(listOf(DayOfWeek.MONDAY)))
                    },
                    modifier = Modifier.weight(1f),
                    textColor = textColor,
                    cardColor = cardColor,
                    accentColor = accentColor
                )

                FrequencyChip(
                    text = "Weekdays",
                    isSelected = false,
                    onClick = {
                        onFrequencyChange(
                            Frequency.Weekly(
                                listOf(
                                    DayOfWeek.MONDAY,
                                    DayOfWeek.TUESDAY,
                                    DayOfWeek.WEDNESDAY,
                                    DayOfWeek.THURSDAY,
                                    DayOfWeek.FRIDAY
                                )
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    textColor = textColor,
                    cardColor = cardColor,
                    accentColor = accentColor
                )
            }
        }

        // Days of week (shown when Specific Days is selected)
        if (frequency is Frequency.Weekly) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { index, dayLabel ->
                        val day = DayOfWeek.entries[index]
                        val isSelected = frequency.days.contains(day)

                        DayCircle(
                            day = dayLabel,
                            isSelected = isSelected,
                            onClick = {
                                val newDays = if (isSelected) {
                                    frequency.days - day
                                } else {
                                    frequency.days + day
                                }
                                if (newDays.isNotEmpty()) {
                                    onFrequencyChange(Frequency.Weekly(newDays))
                                }
                            },
                            textColor = textColor,
                            cardColor = cardColor,
                            accentColor = accentColor
                        )
                    }
                }
            }
        }

        // Time of Day selector
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "TIME OF DAY",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.cil.shift.feature.habits.presentation.create.TimeOfDay.entries.forEach { time ->
                    TimeOfDayChip(
                        timeOfDay = time,
                        isSelected = timeOfDay == time,
                        onClick = { onTimeOfDayChange(time) },
                        modifier = Modifier.weight(1f),
                        textColor = textColor,
                        cardColor = cardColor,
                        accentColor = accentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Reminder toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardColor)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Reminder",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = "Get notified to stay on track",
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.5f)
                )
            }

            Switch(
                checked = hasReminder,
                onCheckedChange = onReminderToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = textColor.copy(alpha = 0.7f),
                    uncheckedTrackColor = textColor.copy(alpha = 0.2f)
                )
            )
        }

        // Time picker (shown when reminder is enabled)
        if (hasReminder) {
            TimePicker(
                time = reminderTime ?: "09:00",
                onTimeChange = onReminderTimeChange,
                textColor = textColor,
                cardColor = cardColor,
                accentColor = accentColor
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
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    cardColor: Color = Color(0xFF1A2942),
    accentColor: Color = Color(0xFF4E7CFF)
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (isSelected) accentColor
                else cardColor
            )
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else textColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else textColor
        )
    }
}

@Composable
private fun DayCircle(
    day: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    cardColor: Color = Color(0xFF1A2942),
    accentColor: Color = Color(0xFF4E7CFF)
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) accentColor
                else cardColor
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF00D9FF) else textColor.copy(alpha = 0.15f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else textColor
        )
    }
}

@Composable
private fun TimePicker(
    time: String,
    onTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    cardColor: Color = Color(0xFF1A2942),
    accentColor: Color = Color(0xFF4E7CFF)
) {
    // Parse time (format: "HH:mm" - 24 hour format)
    val parts = time.split(":")
    var hour by remember { mutableStateOf(parts.getOrNull(0)?.toIntOrNull() ?: 9) }
    var minute by remember { mutableStateOf(parts.getOrNull(1)?.toIntOrNull() ?: 0) }

    // Update callback when values change
    LaunchedEffect(hour, minute) {
        val formattedTime = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
        onTimeChange(formattedTime)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hour picker (24-hour format: 00-23)
            TimePickerColumn(
                value = hour,
                onIncrement = {
                    hour = (hour + 1) % 24
                },
                onDecrement = {
                    hour = if (hour == 0) 23 else hour - 1
                },
                textColor = textColor
            )

            Text(
                text = ":",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            // Minute picker
            TimePickerColumn(
                value = minute,
                onIncrement = {
                    minute = (minute + 5) % 60
                },
                onDecrement = {
                    minute = if (minute == 0) 55 else minute - 5
                },
                textColor = textColor
            )
        }
    }
}

@Composable
private fun TimePickerColumn(
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(onClick = onIncrement) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase",
                tint = textColor.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = value.toString().padStart(2, '0'),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        IconButton(onClick = onDecrement) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease",
                tint = textColor.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun TimeOfDayChip(
    timeOfDay: com.cil.shift.feature.habits.presentation.create.TimeOfDay,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    cardColor: Color = Color(0xFF1A2942),
    accentColor: Color = Color(0xFF4E7CFF)
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) accentColor
                else cardColor
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF00D9FF) else textColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = timeOfDay.emoji,
                fontSize = 18.sp
            )
            Text(
                text = timeOfDay.displayName,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else textColor
            )
        }
    }
}
