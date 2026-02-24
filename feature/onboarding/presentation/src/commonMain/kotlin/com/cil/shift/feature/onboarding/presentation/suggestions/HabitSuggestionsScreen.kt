package com.cil.shift.feature.onboarding.presentation.suggestions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject

@Composable
fun HabitSuggestionsScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HabitSuggestionsViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    // Navigate when habits are created or skipped
    LaunchedEffect(state.isCreating) {
        if (!state.isCreating && state.selectedHabits.isNotEmpty() && state.error == null) {
            // Small delay to show success before navigating
            kotlinx.coroutines.delay(300)
            onComplete()
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with top padding for status bar safety
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Start with Some Habits",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Choose a few to get started. You can always add more later.",
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            // Category selector
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                items(SuggestionCategory.entries) { category ->
                    CategoryChip(
                        category = category,
                        isSelected = state.selectedCategory == category,
                        onClick = { viewModel.onEvent(HabitSuggestionsEvent.CategorySelected(category)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Habit suggestions
            val filteredSuggestions = habitSuggestions.filter { it.category == state.selectedCategory }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredSuggestions) { suggestion ->
                    HabitSuggestionCard(
                        suggestion = suggestion,
                        isSelected = state.selectedHabits.contains(suggestion.id),
                        onClick = { viewModel.onEvent(HabitSuggestionsEvent.HabitToggled(suggestion.id)) }
                    )
                }
            }

            // Error message
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            // Buttons with bottom padding for navigation bar safety
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Skip button
                OutlinedButton(
                    onClick = {
                        viewModel.onEvent(HabitSuggestionsEvent.SkipSuggestions)
                        onComplete()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = textColor
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = textColor.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !state.isCreating
                ) {
                    Text(
                        text = "Skip",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Create habits button
                Button(
                    onClick = { viewModel.onEvent(HabitSuggestionsEvent.CreateSelectedHabits) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4E7CFF),
                        disabledContainerColor = Color(0xFF4E7CFF).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !state.isCreating && state.selectedHabits.isNotEmpty()
                ) {
                    if (state.isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.selectedHabits.isEmpty()) {
                                    "Select Habits"
                                } else {
                                    "Create ${state.selectedHabits.size}"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (state.selectedHabits.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: SuggestionCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isSelected) Color(0xFF4E7CFF)
                else cardColor
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFF00D9FF) else Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.emoji,
                fontSize = 16.sp
            )
            Text(
                text = category.displayName,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else textColor
            )
        }
    }
}

@Composable
private fun HabitSuggestionCard(
    suggestion: HabitSuggestion,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    val backgroundColor = MaterialTheme.colorScheme.background
    val habitColor = suggestion.color.toComposeColor()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFF4E7CFF) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(habitColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = suggestion.icon.toEmoji(),
                        fontSize = 24.sp,
                        color = habitColor
                    )
                }

                // Content
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = suggestion.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )

                    Text(
                        text = suggestion.description,
                        fontSize = 13.sp,
                        color = textColor.copy(alpha = 0.6f),
                        lineHeight = 18.sp
                    )

                    // Badge for habit type and target
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(habitColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when (suggestion.habitType) {
                                    com.cil.shift.feature.habits.domain.model.HabitType.SIMPLE -> "Simple"
                                    com.cil.shift.feature.habits.domain.model.HabitType.MEASURABLE -> "${suggestion.targetValue}${suggestion.targetUnit}"
                                    com.cil.shift.feature.habits.domain.model.HabitType.TIMER -> "${suggestion.targetValue} min"
                                    com.cil.shift.feature.habits.domain.model.HabitType.QUIT -> "Quit"
                                    com.cil.shift.feature.habits.domain.model.HabitType.NEGATIVE -> "Max ${suggestion.targetValue} ${suggestion.targetUnit}"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = habitColor
                            )
                        }
                    }
                }
            }

            // Selection indicator
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color(0xFF4E7CFF)
                        else backgroundColor
                    )
                    .border(
                        width = 2.dp,
                        color = if (isSelected) Color(0xFF00D9FF) else textColor.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Helper function to convert icon ID to emoji
private fun String.toEmoji(): String {
    return when (this) {
        "water" -> "💧"
        "run" -> "🏃"
        "dumbbell" -> "🏋️"
        "utensils" -> "🍴"
        "code" -> "💻"
        "briefcase" -> "💼"
        "tools" -> "🛠"
        "meditation" -> "🧘"
        "heart" -> "❤️"
        "leaf" -> "🍃"
        "book" -> "📚"
        "brain" -> "🧠"
        "moon" -> "🌙"
        "palette" -> "🎨"
        "coffee" -> "☕"
        else -> "✓"
    }
}

// Helper to convert hex color to Compose Color
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
