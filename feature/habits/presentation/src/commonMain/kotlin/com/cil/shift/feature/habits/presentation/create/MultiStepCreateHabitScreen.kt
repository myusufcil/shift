package com.cil.shift.feature.habits.presentation.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cil.shift.feature.habits.presentation.create.steps.Step1NameAndIcon
import com.cil.shift.feature.habits.presentation.create.steps.Step2Schedule
import com.cil.shift.feature.habits.presentation.create.steps.Step3CustomizeAndPreview

@Composable
fun MultiStepCreateHabitScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateEditHabitViewModel = viewModel {
        throw IllegalStateException("ViewModel should be provided via DI")
    }
) {
    val state by viewModel.state.collectAsState()
    var currentStep by remember { mutableStateOf(0) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    // Navigate back when saved
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with back button and progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentStep > 0) {
                            currentStep--
                        } else {
                            onNavigateBack()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }

                // Progress indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentStep) Color(0xFF4E7CFF)
                                    else if (index < currentStep) Color(0xFF00D9FF)
                                    else textColor.copy(alpha = 0.2f)
                                )
                        )
                    }
                }

                // Placeholder for symmetry
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Step content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentStep) {
                    0 -> Step1NameAndIcon(
                        name = state.name,
                        selectedIcon = state.selectedIcon,
                        onNameChange = { viewModel.onEvent(CreateEditHabitEvent.NameChanged(it)) },
                        onIconSelect = { viewModel.onEvent(CreateEditHabitEvent.IconSelected(it)) }
                    )

                    1 -> Step2Schedule(
                        frequency = state.frequency,
                        timeOfDay = state.timeOfDay,
                        hasReminder = state.hasReminder,
                        reminderTime = state.reminderTime,
                        reminderTimes = state.reminderTimes,
                        onFrequencyChange = { viewModel.onEvent(CreateEditHabitEvent.FrequencyChanged(it)) },
                        onTimeOfDayChange = { viewModel.onEvent(CreateEditHabitEvent.TimeOfDaySelected(it)) },
                        onReminderToggle = { viewModel.onEvent(CreateEditHabitEvent.ReminderToggled(it)) },
                        onReminderTimeChange = { viewModel.onEvent(CreateEditHabitEvent.ReminderTimeChanged(it)) },
                        onReminderTimeAdded = { viewModel.onEvent(CreateEditHabitEvent.ReminderTimeAdded(it)) },
                        onReminderTimeRemoved = { viewModel.onEvent(CreateEditHabitEvent.ReminderTimeRemoved(it)) }
                    )

                    2 -> Step3CustomizeAndPreview(
                        name = state.name,
                        icon = state.selectedIcon,
                        selectedColor = state.selectedColor,
                        habitType = state.habitType,
                        targetValue = state.targetValue,
                        targetUnit = state.targetUnit,
                        notes = state.notes,
                        onColorSelect = { viewModel.onEvent(CreateEditHabitEvent.ColorSelected(it)) },
                        onHabitTypeSelect = { viewModel.onEvent(CreateEditHabitEvent.HabitTypeSelected(it)) },
                        onTargetValueChange = { viewModel.onEvent(CreateEditHabitEvent.TargetValueChanged(it)) },
                        onTargetUnitChange = { viewModel.onEvent(CreateEditHabitEvent.TargetUnitChanged(it)) },
                        onNotesChange = { viewModel.onEvent(CreateEditHabitEvent.NotesChanged(it)) }
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

            // Next/Create button
            Button(
                onClick = {
                    if (currentStep < 2) {
                        currentStep++
                    } else {
                        viewModel.onEvent(CreateEditHabitEvent.SaveHabit)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4E7CFF)
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !state.isLoading && when (currentStep) {
                    0 -> state.name.isNotBlank()
                    else -> true
                }
            ) {
                if (state.isLoading) {
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
                            text = when (currentStep) {
                                0 -> "Next Step"
                                1 -> "Next"
                                else -> if (state.habitId != null) "Update Habit" else "Create Habit"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (currentStep == 2) {
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
