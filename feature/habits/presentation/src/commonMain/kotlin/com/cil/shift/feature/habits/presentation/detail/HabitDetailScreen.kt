package com.cil.shift.feature.habits.presentation.detail

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cil.shift.core.common.localization.LocalizationHelpers
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.localization.StringResources
import com.cil.shift.core.common.localization.localized
import com.cil.shift.feature.habits.domain.model.Habit
import com.cil.shift.feature.habits.domain.model.HabitType
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HabitDetailViewModel = viewModel {
        // TODO: Inject dependencies via DI
        throw IllegalStateException("ViewModel should be provided via DI")
    }
) {
    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()

    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.habit?.let {
                            LocalizationHelpers.getLocalizedHabitName(it.name, currentLanguage)
                        } ?: StringResources.habitDetails.localized(),
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = StringResources.back.localized(),
                            tint = textColor
                        )
                    }
                },
                actions = {
                    if (state.habit != null) {
                        IconButton(onClick = { viewModel.onEvent(HabitDetailEvent.ShowDeleteDialog) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = StringResources.delete.localized(),
                                tint = textColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                ),
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
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
                        text = state.error ?: StringResources.unknownError.localized(),
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                state.habit != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Habit Icon and Color
                        HabitHeader(
                            habit = state.habit!!,
                            currentLanguage = currentLanguage,
                            isEditingName = state.isEditingName,
                            editedName = state.editedName,
                            onEvent = viewModel::onEvent,
                            textColor = textColor,
                            cardColor = cardColor
                        )

                        // Today's Progress Controls
                        TodayProgressSection(
                            habit = state.habit!!,
                            todayCompletion = state.todayCompletion,
                            selectedDate = state.selectedDateString,
                            currentLanguage = currentLanguage,
                            isEditingNote = state.isEditingNote,
                            editedNote = state.editedNote,
                            onToggleCompletion = { viewModel.onEvent(HabitDetailEvent.ToggleCompletion) },
                            onIncrementValue = { amount -> viewModel.onEvent(HabitDetailEvent.IncrementValue(amount)) },
                            onDecrementValue = { amount -> viewModel.onEvent(HabitDetailEvent.DecrementValue(amount)) },
                            onEvent = viewModel::onEvent,
                            textColor = textColor,
                            cardColor = cardColor,
                            backgroundColor = backgroundColor
                        )

                        // Statistics Cards
                        StatisticsSection(
                            currentStreak = state.currentStreak,
                            bestStreak = state.bestStreak,
                            completionRate = state.completionRate,
                            currentLanguage = currentLanguage,
                            textColor = textColor,
                            cardColor = cardColor
                        )

                        // Notes Section
                        if (!state.habit!!.notes.isNullOrBlank()) {
                            NotesSection(
                                notes = state.habit!!.notes!!,
                                textColor = textColor,
                                cardColor = cardColor
                            )
                        }

                        // Completions Calendar (Placeholder)
                        CompletionsCalendar(
                            completions = state.completions,
                            currentLanguage = currentLanguage,
                            textColor = textColor,
                            cardColor = cardColor,
                            backgroundColor = backgroundColor
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(HabitDetailEvent.DismissDeleteDialog) },
            title = { Text(StringResources.deleteHabit.localized()) },
            text = { Text(StringResources.deleteHabitMessage.localized()) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(HabitDetailEvent.DeleteHabit) }
                ) {
                    Text(StringResources.delete.localized(), color = Color(0xFFFF6B6B))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onEvent(HabitDetailEvent.DismissDeleteDialog) }
                ) {
                    Text(StringResources.cancel.localized())
                }
            }
        )
    }
}

@Composable
private fun HabitHeader(
    habit: Habit,
    currentLanguage: com.cil.shift.core.common.localization.Language,
    isEditingName: Boolean,
    editedName: String,
    onEvent: (HabitDetailEvent) -> Unit,
    textColor: Color,
    cardColor: Color
) {
    val habitColor = habit.color.toComposeColor()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Icon with shadow
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(habitColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getIconEmoji(habit.icon),
                fontSize = 48.sp
            )
        }

        // Habit name (editable)
        if (isEditingName) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.OutlinedTextField(
                    value = editedName,
                    onValueChange = { onEvent(HabitDetailEvent.UpdateEditedName(it)) },
                    modifier = Modifier.weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4E7CFF),
                        unfocusedBorderColor = textColor.copy(alpha = 0.3f),
                        cursorColor = Color(0xFF4E7CFF),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    singleLine = true
                )
                androidx.compose.material3.IconButton(
                    onClick = { onEvent(HabitDetailEvent.SaveEditedName) }
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Check,
                        contentDescription = "Save",
                        tint = Color(0xFF4CAF50)
                    )
                }
                androidx.compose.material3.IconButton(
                    onClick = { onEvent(HabitDetailEvent.CancelEditingName) }
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = Color(0xFFFF6B6B)
                    )
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = LocalizationHelpers.getLocalizedHabitName(habit.name, currentLanguage),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                androidx.compose.material3.IconButton(
                    onClick = { onEvent(HabitDetailEvent.StartEditingName) },
                    modifier = Modifier.size(32.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                        contentDescription = "Edit name",
                        tint = textColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Reminder time with emoji
        if (habit.reminderTime != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏰",
                    fontSize = 16.sp
                )
                Text(
                    text = habit.reminderTime!!,
                    fontSize = 14.sp,
                    color = Color(0xFF4E7CFF),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Frequency with emoji
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📅",
                fontSize = 16.sp
            )
            Text(
                text = formatFrequency(habit.frequency, currentLanguage),
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun StatisticsSection(
    currentStreak: Int,
    bestStreak: Int,
    completionRate: Float,
    currentLanguage: com.cil.shift.core.common.localization.Language,
    textColor: Color,
    cardColor: Color
) {
    // Animate values
    val animatedCurrentStreak by animateIntAsState(
        targetValue = currentStreak,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    val animatedBestStreak by animateIntAsState(
        targetValue = bestStreak,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    val animatedCompletionRate by animateFloatAsState(
        targetValue = completionRate,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                emoji = "🔥",
                title = StringResources.currentStreak.get(currentLanguage),
                value = "$animatedCurrentStreak ${StringResources.days.get(currentLanguage)}",
                modifier = Modifier.weight(1f),
                textColor = textColor,
                cardColor = cardColor
        )
            StatCard(
                emoji = "⭐",
                title = StringResources.bestStreak.get(currentLanguage),
                value = "$animatedBestStreak ${StringResources.days.get(currentLanguage)}",
                modifier = Modifier.weight(1f),
                textColor = textColor,
                cardColor = cardColor
            )
        }

        StatCard(
            emoji = "✅",
            title = StringResources.completionRate.get(currentLanguage),
            value = "${(animatedCompletionRate * 100).toInt()}%",
            modifier = Modifier.fillMaxWidth(),
            textColor = textColor,
            cardColor = cardColor
        )
    }
}

@Composable
private fun StatCard(
    emoji: String = "",
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    textColor: Color,
    cardColor: Color
) {
    Card(
        modifier = modifier
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
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (emoji.isNotEmpty()) {
                Text(
                    text = emoji,
                    fontSize = 32.sp
                )
            }
            Text(
                text = title,
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00D9FF)
            )
        }
    }
}

@Composable
private fun CompletionsCalendar(
    completions: List<com.cil.shift.feature.habits.domain.model.HabitCompletion>,
    currentLanguage: com.cil.shift.core.common.localization.Language,
    textColor: Color,
    cardColor: Color,
    backgroundColor: Color
) {
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅",
                    fontSize = 20.sp
                )
                Text(
                    text = StringResources.completionHistory.get(currentLanguage),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }

            // Filter only completed entries
            val completedWithDetails = completions
                .filter { it.isCompleted }
                .sortedByDescending { it.date }
                .take(10) // Show last 10 completions

            if (completedWithDetails.isEmpty()) {
                Text(
                    text = StringResources.noCompletionsYet.get(currentLanguage),
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                Text(
                    text = "${completedWithDetails.size} ${StringResources.completions.get(currentLanguage)}",
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (completedWithDetails.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        completedWithDetails.forEach { completion ->
                            CompletionHistoryItem(
                                completion = completion,
                                currentLanguage = currentLanguage,
                                textColor = textColor,
                                backgroundColor = backgroundColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionHistoryItem(
    completion: com.cil.shift.feature.habits.domain.model.HabitCompletion,
    currentLanguage: com.cil.shift.core.common.localization.Language,
    textColor: Color,
    backgroundColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date
            Text(
                text = formatCompletionDate(completion.date, currentLanguage),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )

            // Completion indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show value if it exists (for measurable habits)
                if (completion.currentValue > 0) {
                    Text(
                        text = "${completion.currentValue}",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "✓",
                    fontSize = 16.sp,
                    color = Color(0xFF4CAF50)
                )
            }
        }

        // Note if exists
        if (!completion.note.isNullOrBlank()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "📝",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = completion.note!!,
                    fontSize = 13.sp,
                    color = textColor.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

private fun formatCompletionDate(dateString: String, language: com.cil.shift.core.common.localization.Language): String {
    return try {
        val date = kotlinx.datetime.LocalDate.parse(dateString)
        val monthName = LocalizationHelpers.getMonthName(date.monthNumber, language)
        "${date.dayOfMonth} $monthName ${date.year}"
    } catch (e: Exception) {
        dateString
    }
}

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

@Composable
private fun TodayProgressSection(
    habit: Habit,
    todayCompletion: com.cil.shift.feature.habits.domain.model.HabitCompletion?,
    selectedDate: String?,
    currentLanguage: com.cil.shift.core.common.localization.Language,
    isEditingNote: Boolean,
    editedNote: String,
    onToggleCompletion: () -> Unit,
    onIncrementValue: (Int) -> Unit,
    onDecrementValue: (Int) -> Unit,
    onEvent: (HabitDetailEvent) -> Unit,
    textColor: Color,
    cardColor: Color,
    backgroundColor: Color
) {
    val habitColor = habit.color.toComposeColor()
    val isCompleted = todayCompletion?.isCompleted ?: false
    val currentValue = todayCompletion?.currentValue ?: 0

    // Animate current value
    val animatedCurrentValue by animateIntAsState(
        targetValue = currentValue,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getIconEmoji(habit.icon),
                    fontSize = 24.sp
                )

                // Dinamik başlık - seçili tarihe göre
                val progressTitle = remember(selectedDate, currentLanguage) {
                    if (selectedDate != null) {
                        try {
                            val date = kotlinx.datetime.LocalDate.parse(selectedDate)
                            val today = com.cil.shift.core.common.currentDate()
                            if (date.toString() == today.toString()) {
                                StringResources.todaysProgress.get(currentLanguage)
                            } else {
                                formatCompletionDate(selectedDate, currentLanguage)
                            }
                        } catch (e: Exception) {
                            StringResources.todaysProgress.get(currentLanguage)
                        }
                    } else {
                        StringResources.todaysProgress.get(currentLanguage)
                    }
                }

                Text(
                    text = progressTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            when (habit.habitType) {
                HabitType.SIMPLE -> {
                    // Animated icon size and rotation
                    val iconSize by animateIntAsState(
                        targetValue = if (isCompleted) 22 else 20,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )

                    val iconRotation by animateFloatAsState(
                        targetValue = if (isCompleted) 360f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )

                    // Toggle button for simple habits
                    Button(
                        onClick = onToggleCompletion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompleted) Color(0xFF4ECDC4) else habitColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.Check else androidx.compose.material.icons.Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(iconSize.dp)
                                    .graphicsLayer {
                                        rotationZ = iconRotation
                                    },
                                tint = Color.White
                            )
                            Text(
                                text = if (isCompleted) {
                                    StringResources.completed.get(currentLanguage)
                                } else {
                                    StringResources.markAsComplete.get(currentLanguage)
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                HabitType.MEASURABLE -> {
                    // Progress bar and increment/decrement controls
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Calculate progress
                        val progress = if (habit.targetValue != null && habit.targetValue!! > 0) {
                            (currentValue.toFloat() / habit.targetValue!!).coerceIn(0f, 1f)
                        } else 0f

                        // Animate progress
                        val animatedProgress by animateFloatAsState(
                            targetValue = progress,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )

                        // Progress display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${animatedCurrentValue}${habit.targetUnit ?: ""}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = habitColor
                            )

                            if (habit.targetValue != null && habit.targetValue!! > 0) {
                                val animatedPercentage by animateIntAsState(
                                    targetValue = ((currentValue.toFloat() / habit.targetValue!!) * 100).toInt(),
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "/ ${habit.targetValue}${habit.targetUnit ?: ""}",
                                        fontSize = 18.sp,
                                        color = textColor.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "$animatedPercentage%",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = habitColor
                                    )
                                }
                            } else {
                                Text(
                                    text = "",
                                    fontSize = 18.sp,
                                    color = textColor.copy(alpha = 0.6f)
                                )
                            }
                        }

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = habitColor,
                            trackColor = textColor.copy(alpha = 0.1f)
                        )

                        // Increment/Decrement buttons
                        val incrementAmount = when {
                            habit.targetUnit?.contains("ml", ignoreCase = true) == true -> 250
                            habit.targetUnit?.contains("steps", ignoreCase = true) == true -> 500
                            habit.targetUnit?.contains("cal", ignoreCase = true) == true -> 100
                            else -> 1
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onDecrementValue(incrementAmount) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                enabled = currentValue > 0,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = textColor
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = textColor.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = StringResources.decrease.get(currentLanguage),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Button(
                                onClick = { onIncrementValue(incrementAmount) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = habitColor
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = StringResources.increase.get(currentLanguage),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                HabitType.TIMER -> {
                    // Timer controls
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (isCompleted) {
                                StringResources.sessionCompleted.get(currentLanguage)
                            } else {
                                StringResources.startYourSession.get(currentLanguage)
                            },
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )

                        Button(
                            onClick = onToggleCompletion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCompleted) Color(0xFF4ECDC4) else habitColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = if (isCompleted) {
                                        StringResources.completed.get(currentLanguage)
                                    } else {
                                        StringResources.markAsComplete.get(currentLanguage)
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                HabitType.QUIT -> {
                    // Quit habit - show days since quit
                    val daysSinceQuit = habit.quitStartDate?.let { startDate ->
                        val now = com.cil.shift.core.common.currentTimestamp()
                        ((now - startDate) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                    } ?: 0

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$daysSinceQuit",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = habitColor
                        )
                        Text(
                            text = if (daysSinceQuit == 1) "day clean" else "days clean",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Keep going! You're doing great!",
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.5f)
                        )
                    }
                }

                HabitType.NEGATIVE -> {
                    // Negative/reduce habit - show current vs limit
                    val limitValue = habit.targetValue ?: 2
                    val isExceeded = currentValue > limitValue
                    val statusColor = if (isExceeded) Color(0xFFFF6B6B) else habitColor

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "$currentValue",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Text(
                                text = " / $limitValue ${habit.targetUnit ?: "times"}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor.copy(alpha = 0.5f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        if (isExceeded) {
                            Text(
                                text = "⚠️ You've exceeded your daily limit",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = statusColor,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onIncrementValue(1) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = statusColor,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${StringResources.logUsage.get(currentLanguage)} (+1)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Completion Note Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor, RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = textColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📝 ${StringResources.note.get(currentLanguage)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    if (!isEditingNote) {
                        androidx.compose.material3.IconButton(
                            onClick = { onEvent(HabitDetailEvent.StartEditingNote) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                                contentDescription = "Edit note",
                                tint = textColor.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (isEditingNote) {
                    // Editing mode
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.OutlinedTextField(
                            value = editedNote,
                            onValueChange = { onEvent(HabitDetailEvent.UpdateEditedNote(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = StringResources.addNoteForToday.get(currentLanguage),
                                    color = textColor.copy(alpha = 0.4f),
                                    fontSize = 14.sp
                                )
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                color = textColor
                            ),
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4E7CFF),
                                unfocusedBorderColor = textColor.copy(alpha = 0.2f),
                                cursorColor = Color(0xFF4E7CFF),
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { onEvent(HabitDetailEvent.CancelEditingNote) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = textColor
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = textColor.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = StringResources.cancel.get(currentLanguage),
                                    fontSize = 14.sp
                                )
                            }
                            Button(
                                onClick = { onEvent(HabitDetailEvent.SaveEditedNote) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4E7CFF)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = StringResources.save.get(currentLanguage),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    // Display mode
                    val noteText = todayCompletion?.note
                    if (!noteText.isNullOrBlank()) {
                        Text(
                            text = noteText,
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    } else {
                        // Dinamik note placeholder - seçili tarihe göre
                        val notePlaceholder = remember(selectedDate, currentLanguage) {
                            StringResources.noNoteForToday.get(currentLanguage)
                        }

                        Text(
                            text = notePlaceholder,
                            fontSize = 13.sp,
                            color = textColor.copy(alpha = 0.4f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesSection(
    notes: String,
    textColor: Color,
    cardColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardColor, RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📝 Notes",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor.copy(alpha = 0.8f)
        )
        Text(
            text = notes,
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.7f),
            lineHeight = 20.sp
        )
    }
}

private fun getIconEmoji(icon: String): String {
    return when (icon.lowercase()) {
        // Health
        "water", "wat", "hydration" -> "💧"
        "vegetables", "veg" -> "🥦"
        "fruit", "fru" -> "🍉"
        "cooking", "coo" -> "🍳"
        "breakfast", "bre", "kahvaltı", "kah" -> "🍳"
        "sunrise", "sun" -> "🌅"
        "sunset" -> "🌇"
        "pill", "med" -> "💊"
        "nutrition", "food" -> "🥗"
        // Mindfulness
        "journal", "jou" -> "✍️"
        "pray", "pra" -> "🙏"
        "meditation", "me", "mindfulness" -> "🧘"
        "relaxed", "rel" -> "😌"
        "detox", "det" -> "🚫"
        // Learning
        "books", "book", "boo", "read" -> "📚"
        "course", "cou" -> "📝"
        "instrument", "ins" -> "🎷"
        "study", "stu" -> "🧑‍🎓"
        "flute", "flu", "ute" -> "🎺"
        // Active
        "running", "run" -> "🏃"
        "walking", "wal" -> "🚶"
        "dance", "dan" -> "💃"
        "pilates", "pil" -> "🤸"
        "gym", "dumbbell", "dum", "fitness", "workout" -> "🏋️"
        "sports", "spo" -> "⚽"
        "stretching", "str" -> "🤾"
        "yoga", "yog" -> "🧘"
        // Self-care
        "shower", "sho" -> "🚿"
        "skincare", "ski" -> "🧴"
        "haircare", "hai" -> "💆"
        // Social
        "couple", "heart", "hea" -> "💕"
        "party", "par" -> "🥳"
        "family", "fam" -> "👨‍👩‍👧"
        // Financial
        "budget", "bud" -> "💰"
        "invest", "inv" -> "📊"
        "expenses", "exp" -> "💸"
        // Home
        "clean", "cle" -> "🧹"
        "bed" -> "🛏️"
        "laundry", "lau" -> "🧺"
        "dishes", "dis" -> "🪣"
        "bills", "bil" -> "🧾"
        // Additional
        "leaf", "lea" -> "🍃"
        "brain", "bra" -> "🧠"
        "fire", "fir" -> "🔥"
        "moon", "mo" -> "🌙"
        "bulb", "bul" -> "💡"
        "smile", "smi" -> "😊"
        "check", "che" -> "✅"
        "coffee", "cof" -> "☕"
        "sleep", "sle" -> "😴"
        "music", "mus" -> "🎵"
        "art", "palette", "pale", "pal" -> "🎨"
        "briefcase", "bri", "work" -> "💼"
        else -> {
            if (icon.any { it.code >= 0x1F300 }) icon else "✓"
        }
    }
}

private fun formatFrequency(
    frequency: com.cil.shift.feature.habits.domain.model.Frequency,
    language: com.cil.shift.core.common.localization.Language
): String {
    return when (frequency) {
        is com.cil.shift.feature.habits.domain.model.Frequency.Daily -> {
            StringResources.daily.get(language)
        }
        is com.cil.shift.feature.habits.domain.model.Frequency.Weekly -> {
            val dayNames = frequency.days.map { day ->
                LocalizationHelpers.getDayNameShort(day.ordinal + 1, language)
            }.joinToString(", ")
            "${StringResources.weekly.get(language)} ($dayNames)"
        }
        is com.cil.shift.feature.habits.domain.model.Frequency.Custom -> {
            "${frequency.daysInterval} ${StringResources.days.get(language)}"
        }
    }
}
