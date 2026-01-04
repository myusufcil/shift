package com.cil.shift.feature.habits.presentation.create.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.localization.StringResources
import com.cil.shift.core.common.localization.localized
import com.cil.shift.feature.habits.domain.model.HabitType

@Composable
fun Step3CustomizeAndPreview(
    name: String,
    icon: String,
    selectedColor: String,
    habitType: HabitType,
    targetValue: Int?,
    targetUnit: String?,
    notes: String,
    onColorSelect: (String) -> Unit,
    onHabitTypeSelect: (HabitType) -> Unit,
    onTargetValueChange: (Int?) -> Unit,
    onTargetUnitChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
        // Title
        Text(
            text = "Make it yours",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "Choose a color to identify your habit and set a goal type.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            lineHeight = 20.sp
        )

        // Preview card
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "PREVIEW",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A2942))
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(selectedColor.toComposeColor().copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = icon.toEmoji(),
                            fontSize = 24.sp,
                            color = selectedColor.toComposeColor()
                        )
                    }

                    Text(
                        text = name.ifBlank { "Morning Meditation" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        // Color palette
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "COLOR PALETTE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp), // 2 rows * 48dp + 1 gap * 12dp + padding
                userScrollEnabled = false
            ) {
                items(colorPalette) { colorHex ->
                    ColorCircle(
                        color = colorHex.toComposeColor(),
                        isSelected = selectedColor == colorHex,
                        onClick = { onColorSelect(colorHex) }
                    )
                }
            }
        }

        // Habit Type selector
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "HABIT TYPE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HabitTypeChip(
                    text = "Simple",
                    isSelected = habitType == HabitType.SIMPLE,
                    onClick = { onHabitTypeSelect(HabitType.SIMPLE) },
                    modifier = Modifier.weight(1f)
                )

                HabitTypeChip(
                    text = "Measurable",
                    isSelected = habitType == HabitType.MEASURABLE,
                    onClick = { onHabitTypeSelect(HabitType.MEASURABLE) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HabitTypeChip(
                    text = "Timer",
                    isSelected = habitType == HabitType.TIMER,
                    onClick = { onHabitTypeSelect(HabitType.TIMER) },
                    modifier = Modifier.weight(1f)
                )

                HabitTypeChip(
                    text = "Session",
                    isSelected = habitType == HabitType.SESSION,
                    onClick = { onHabitTypeSelect(HabitType.SESSION) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Type description
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF4E7CFF).copy(alpha = 0.1f))
                .padding(12.dp)
        ) {
            Text(
                text = when (habitType) {
                    HabitType.SIMPLE -> "Simple checkbox - just mark it done"
                    HabitType.MEASURABLE -> "Track numbers (e.g., water intake, pages read)"
                    HabitType.TIMER -> "Track time spent (e.g., deep work, study)"
                    HabitType.SESSION -> "Start/stop sessions (e.g., meditation, workout)"
                },
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 16.sp
            )
        }

        // Target value input (for Measurable, Timer, Session)
        if (habitType != HabitType.SIMPLE) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "TARGET & UNIT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Target value
                    TextField(
                        value = targetValue?.toString() ?: "",
                        onValueChange = { value ->
                            onTargetValueChange(if (value.isBlank()) null else value.toIntOrNull())
                        },
                        modifier = Modifier
                            .weight(0.7f)
                            .height(108.dp),
                        placeholder = {
                            Text(
                                text = when (habitType) {
                                    HabitType.MEASURABLE -> "2000"
                                    HabitType.TIMER -> "120"
                                    HabitType.SESSION -> "15"
                                    else -> "0"
                                },
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1A2942),
                            unfocusedContainerColor = Color(0xFF1A2942),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF00D9FF),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    // Unit - Different for Timer/Session vs Measurable
                    if (habitType == HabitType.TIMER || habitType == HabitType.SESSION) {
                        // Time unit selector (hr, min, sec)
                        Column(
                            modifier = Modifier.weight(0.6f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val timeUnits = listOf(
                                "hr" to StringResources.hr.localized(),
                                "min" to StringResources.min.localized(),
                                "sec" to StringResources.sec.localized()
                            )

                            timeUnits.forEach { (unit, label) ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (targetUnit == unit) Color(0xFF4E7CFF)
                                            else Color(0xFF1A2942)
                                        )
                                        .clickable { onTargetUnitChange(unit) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (targetUnit == unit) FontWeight.SemiBold else FontWeight.Normal,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        // Unit selector for Measurable - Column 1
                        Column(
                            modifier = Modifier.weight(0.65f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val measurableUnits = listOf(
                                "ml", "L", "g", "kg", "steps", "pages", "reps", "mins"
                            )

                            // Show first 3 units
                            measurableUnits.take(3).forEach { unit ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (targetUnit == unit) Color(0xFF4E7CFF)
                                            else Color(0xFF1A2942)
                                        )
                                        .clickable { onTargetUnitChange(unit) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = unit,
                                        fontSize = 12.sp,
                                        fontWeight = if (targetUnit == unit) FontWeight.SemiBold else FontWeight.Normal,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Show remaining units in a second column if needed
                    if (habitType == HabitType.MEASURABLE) {
                        Column(
                            modifier = Modifier.weight(0.65f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val measurableUnits = listOf(
                                "ml", "L", "g", "kg", "steps", "pages", "reps", "mins"
                            )

                            // Show units 4-6
                            measurableUnits.slice(3..5).forEach { unit ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (targetUnit == unit) Color(0xFF4E7CFF)
                                            else Color(0xFF1A2942)
                                        )
                                        .clickable { onTargetUnitChange(unit) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = unit,
                                        fontSize = 12.sp,
                                        fontWeight = if (targetUnit == unit) FontWeight.SemiBold else FontWeight.Normal,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Third column for remaining units
                    if (habitType == HabitType.MEASURABLE) {
                        Column(
                            modifier = Modifier.weight(0.65f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val measurableUnits = listOf(
                                "ml", "L", "g", "kg", "steps", "pages", "reps", "mins"
                            )

                            // Show remaining units
                            measurableUnits.drop(6).forEach { unit ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (targetUnit == unit) Color(0xFF4E7CFF)
                                            else Color(0xFF1A2942)
                                        )
                                        .clickable { onTargetUnitChange(unit) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = unit,
                                        fontSize = 12.sp,
                                        fontWeight = if (targetUnit == unit) FontWeight.SemiBold else FontWeight.Normal,
                                        color = Color.White
                                    )
                                }
                            }
                            // Add spacer for alignment with other columns
                            if (measurableUnits.size - 6 < 3) {
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
            }
        }

        // Notes field
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "NOTES (OPTIONAL)",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )

            TextField(
                value = notes,
                onValueChange = onNotesChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = {
                    Text(
                        text = "Add notes or motivation for this habit...",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1A2942),
                    unfocusedContainerColor = Color(0xFF1A2942),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF00D9FF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )
        }
            }
        }
    }
}

@Composable
private fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}

@Composable
private fun HabitTypeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color(0xFF4E7CFF)
                else Color(0xFF1A2942)
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFF00D9FF) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = Color.White
        )
    }
}

private val colorPalette = listOf(
    "#FF9E9E", // Soft Pink
    "#FFB88C", // Peach
    "#FFE5A3", // Cream
    "#D4E7C5", // Mint
    "#9FE7DD", // Cyan
    "#B5B9FF", // Lavender
    "#E0BBE4", // Light Purple
    "#FFDFD3", // Light Coral
    "#C1E1C1", // Pale Green
    "#AED9E0", // Powder Blue
    "#FAE7B5", // Vanilla
    "#4E7CFF", // Blue
)

// Helper functions
private fun String.toComposeColor(): Color {
    return try {
        val colorString = this.removePrefix("#")
        val colorInt = colorString.toLong(16)
        if (colorString.length == 6) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        Color(0xFF6C63FF)
    }
}

private fun String.toEmoji(): String {
    // Map icon IDs to emojis
    return when (this) {
        "water" -> "💧"
        "tools" -> "🛠"
        "camera" -> "📷"
        "umbrella" -> "☂️"
        "run" -> "🏃"
        "moon" -> "🌙"
        "code" -> "💻"
        "book" -> "📚"
        "utensils" -> "🍴"
        "leaf" -> "🍃"
        "dumbbell" -> "🏋️"
        "meditation" -> "🧘"
        "music" -> "🎵"
        "palette" -> "🎨"
        "briefcase" -> "💼"
        "coffee" -> "☕"
        "brain" -> "🧠"
        "heart" -> "❤️"
        else -> "✓"
    }
}
