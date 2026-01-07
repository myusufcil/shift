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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.localization.StringResources
import com.cil.shift.core.common.localization.localized
import com.cil.shift.feature.habits.domain.model.HabitType
import org.koin.compose.koinInject

@Composable
fun Step3CustomizeAndPreview(
    name: String,
    icon: String,
    selectedColor: String,
    habitType: HabitType,
    targetValue: Int?,
    targetUnit: String?,
    notes: String,
    isNegative: Boolean = false,
    quitStartDate: Long? = null,
    onColorSelect: (String) -> Unit,
    onHabitTypeSelect: (HabitType) -> Unit,
    onTargetValueChange: (Int?) -> Unit,
    onTargetUnitChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onIsNegativeChange: (Boolean) -> Unit = {},
    onQuitStartDateChange: (Long?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()

    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    val accentColor = Color(0xFF4E7CFF)

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
            text = StringResources.makeItYours.get(currentLanguage),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Text(
            text = StringResources.chooseColorAndGoalType.get(currentLanguage),
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.7f),
            lineHeight = 20.sp
        )

        // Preview card
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = StringResources.preview.get(currentLanguage),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardColor)
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
                        color = textColor
                    )
                }
            }
        }

        // Color palette
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = StringResources.colorPalette.get(currentLanguage),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.5f),
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
                text = StringResources.habitType.get(currentLanguage),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HabitTypeChip(
                    text = StringResources.typeSimple.get(currentLanguage),
                    isSelected = habitType == HabitType.SIMPLE,
                    onClick = { onHabitTypeSelect(HabitType.SIMPLE) },
                    modifier = Modifier.weight(1f),
                    textColor = textColor,
                    cardColor = cardColor,
                    accentColor = accentColor
                )

                HabitTypeChip(
                    text = StringResources.typeMeasurable.get(currentLanguage),
                    isSelected = habitType == HabitType.MEASURABLE,
                    onClick = { onHabitTypeSelect(HabitType.MEASURABLE) },
                    modifier = Modifier.weight(1f),
                    textColor = textColor,
                    cardColor = cardColor,
                    accentColor = accentColor
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HabitTypeChip(
                    text = StringResources.typeTimer.get(currentLanguage),
                    isSelected = habitType == HabitType.TIMER,
                    onClick = { onHabitTypeSelect(HabitType.TIMER) },
                    modifier = Modifier.weight(1f),
                    textColor = textColor,
                    cardColor = cardColor,
                    accentColor = accentColor
                )

                HabitTypeChip(
                    text = StringResources.typeQuit.get(currentLanguage),
                    isSelected = habitType == HabitType.QUIT,
                    onClick = { onHabitTypeSelect(HabitType.QUIT) },
                    modifier = Modifier.weight(1f),
                    textColor = textColor,
                    cardColor = cardColor,
                    accentColor = Color(0xFFFF6B6B) // Red for quit
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HabitTypeChip(
                    text = StringResources.typeReduce.get(currentLanguage),
                    isSelected = habitType == HabitType.NEGATIVE,
                    onClick = { onHabitTypeSelect(HabitType.NEGATIVE) },
                    modifier = Modifier.weight(1f),
                    textColor = textColor,
                    cardColor = cardColor,
                    accentColor = Color(0xFFFFB347) // Amber for reduce
                )

                // Empty spacer for balance
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Type description
        val typeDescriptionColor = when (habitType) {
            HabitType.QUIT -> Color(0xFFFF6B6B)
            HabitType.NEGATIVE -> Color(0xFFFFB347)
            else -> accentColor
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(typeDescriptionColor.copy(alpha = 0.1f))
                .padding(12.dp)
        ) {
            Text(
                text = when (habitType) {
                    HabitType.SIMPLE -> StringResources.simpleDescription.get(currentLanguage)
                    HabitType.MEASURABLE -> StringResources.measurableDescription.get(currentLanguage)
                    HabitType.TIMER -> StringResources.timerDescription.get(currentLanguage)
                    HabitType.QUIT -> StringResources.quitDescription.get(currentLanguage)
                    HabitType.NEGATIVE -> StringResources.reduceDescription.get(currentLanguage)
                },
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.7f),
                lineHeight = 16.sp
            )
        }

        // Target value input (for Measurable, Timer, Session, Negative)
        // QUIT type doesn't need target values - it tracks days automatically
        // SIMPLE type doesn't need target values either
        if (habitType != HabitType.SIMPLE && habitType != HabitType.QUIT) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = StringResources.targetAndUnit.get(currentLanguage),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor.copy(alpha = 0.5f),
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
                                    HabitType.TIMER -> "30"
                                    HabitType.NEGATIVE -> "2"
                                    else -> "0"
                                },
                                color = textColor.copy(alpha = 0.3f)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = cardColor,
                            unfocusedContainerColor = cardColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = Color(0xFF00D9FF),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    // Unit selection based on habit type
                    val negativeAccentColor = Color(0xFFFFB347)
                    when (habitType) {
                        HabitType.TIMER -> {
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
                                                if (targetUnit == unit) accentColor
                                                else cardColor
                                            )
                                            .clickable { onTargetUnitChange(unit) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (targetUnit == unit) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (targetUnit == unit) Color.White else textColor
                                        )
                                    }
                                }
                            }
                        }
                        HabitType.NEGATIVE -> {
                            // Negative/Reduce type units (cups, glasses, times, cigarettes, etc.)
                            val negativeUnits = listOf(
                                "cups", "glasses", "times", "cigarettes", "drinks", "hours", "snacks", "servings"
                            )

                            // First column - show first 3 units
                            Column(
                                modifier = Modifier.weight(0.65f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                negativeUnits.take(3).forEach { unit ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (targetUnit == unit) negativeAccentColor
                                                else cardColor
                                            )
                                            .clickable { onTargetUnitChange(unit) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = unit,
                                            fontSize = 12.sp,
                                            fontWeight = if (targetUnit == unit) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (targetUnit == unit) Color.White else textColor
                                        )
                                    }
                                }
                            }
                            // Second column - show units 4-6
                            Column(
                                modifier = Modifier.weight(0.65f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                negativeUnits.slice(3..5).forEach { unit ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (targetUnit == unit) negativeAccentColor
                                                else cardColor
                                            )
                                            .clickable { onTargetUnitChange(unit) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = unit,
                                            fontSize = 12.sp,
                                            fontWeight = if (targetUnit == unit) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (targetUnit == unit) Color.White else textColor
                                        )
                                    }
                                }
                            }
                            // Third column - show remaining units
                            Column(
                                modifier = Modifier.weight(0.65f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                negativeUnits.drop(6).forEach { unit ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (targetUnit == unit) negativeAccentColor
                                                else cardColor
                                            )
                                            .clickable { onTargetUnitChange(unit) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = unit,
                                            fontSize = 12.sp,
                                            fontWeight = if (targetUnit == unit) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (targetUnit == unit) Color.White else textColor
                                        )
                                    }
                                }
                                // Add spacer for alignment
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                        else -> {
                            // Unit selector for Measurable - Column 1
                            val measurableUnits = listOf(
                                "ml", "L", "g", "kg", "steps", "pages", "reps", "cal"
                            )
                            Column(
                                modifier = Modifier.weight(0.65f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Show first 3 units
                                measurableUnits.take(3).forEach { unit ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (targetUnit == unit) accentColor
                                                else cardColor
                                            )
                                            .clickable { onTargetUnitChange(unit) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = unit,
                                            fontSize = 12.sp,
                                            fontWeight = if (targetUnit == unit) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (targetUnit == unit) Color.White else textColor
                                        )
                                    }
                                }
                            }
                            // Column 2
                            Column(
                                modifier = Modifier.weight(0.65f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Show units 4-6
                                measurableUnits.slice(3..5).forEach { unit ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (targetUnit == unit) accentColor
                                                else cardColor
                                            )
                                            .clickable { onTargetUnitChange(unit) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = unit,
                                            fontSize = 12.sp,
                                            fontWeight = if (targetUnit == unit) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (targetUnit == unit) Color.White else textColor
                                        )
                                    }
                                }
                            }
                            // Column 3
                            Column(
                                modifier = Modifier.weight(0.65f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Show remaining units
                                measurableUnits.drop(6).forEach { unit ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (targetUnit == unit) accentColor
                                                else cardColor
                                            )
                                            .clickable { onTargetUnitChange(unit) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = unit,
                                            fontSize = 12.sp,
                                            fontWeight = if (targetUnit == unit) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (targetUnit == unit) Color.White else textColor
                                        )
                                    }
                                }
                                // Add spacer for alignment
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
                text = StringResources.notesOptional.get(currentLanguage),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.5f),
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
                        text = StringResources.addNotesPlaceholder.get(currentLanguage),
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.4f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = cardColor,
                    unfocusedContainerColor = cardColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
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
    val textColor = MaterialTheme.colorScheme.onBackground
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color.White else textColor.copy(alpha = 0.1f),
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
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else textColor
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
        // Health & Fitness
        "water" -> "💧"
        "run" -> "🏃"
        "dumbbell" -> "🏋️"
        "heart" -> "❤️"
        "apple" -> "🍎"
        "salad" -> "🥗"
        "bike" -> "🚴"
        "swim" -> "🏊"

        // Mindfulness & Wellness
        "meditation" -> "🧘"
        "moon" -> "🌙"
        "sun" -> "☀️"
        "sparkles" -> "✨"
        "brain" -> "🧠"
        "lotus" -> "🪷"
        "pray" -> "🙏"
        "sleep" -> "😴"

        // Productivity & Work
        "code" -> "💻"
        "book" -> "📚"
        "briefcase" -> "💼"
        "pencil" -> "✏️"
        "target" -> "🎯"
        "clock" -> "⏰"
        "calendar" -> "📅"
        "chart" -> "📈"

        // Food & Drinks
        "coffee" -> "☕"
        "utensils" -> "🍴"
        "pizza" -> "🍕"
        "burger" -> "🍔"
        "candy" -> "🍬"
        "soda" -> "🥤"
        "beer" -> "🍺"
        "wine" -> "🍷"

        // Lifestyle & Home
        "home" -> "🏠"
        "bed" -> "🛏️"
        "clean" -> "🧹"
        "laundry" -> "🧺"
        "plant" -> "🌱"
        "leaf" -> "🍃"
        "flower" -> "🌸"
        "dog" -> "🐕"

        // Entertainment & Hobbies
        "music" -> "🎵"
        "palette" -> "🎨"
        "camera" -> "📷"
        "game" -> "🎮"
        "guitar" -> "🎸"
        "movie" -> "🎬"
        "headphones" -> "🎧"
        "mic" -> "🎤"

        // Quit & Reduce
        "cigarette" -> "🚬"
        "phone" -> "📱"
        "tv" -> "📺"
        "shopping" -> "🛍️"
        "cookie" -> "🍪"
        "chocolate" -> "🍫"
        "ice" -> "🍦"
        "donut" -> "🍩"

        // Tools & Misc
        "tools" -> "🛠"
        "umbrella" -> "☂️"
        "car" -> "🚗"
        "plane" -> "✈️"
        "star" -> "⭐"
        "fire" -> "🔥"
        "trophy" -> "🏆"
        "check" -> "✅"

        else -> "✓"
    }
}
