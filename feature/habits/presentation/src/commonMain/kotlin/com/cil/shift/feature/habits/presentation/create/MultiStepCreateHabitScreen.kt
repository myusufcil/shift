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
import com.cil.shift.core.common.localization.LocalizationHelpers
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.localization.StringResources
import org.koin.compose.koinInject
import com.cil.shift.core.designsystem.components.HoneyBottomSheetStrings
import com.cil.shift.core.designsystem.components.NotEnoughHoneyBottomSheet
import com.cil.shift.feature.habits.presentation.create.steps.Step1NameAndIcon
import com.cil.shift.feature.habits.presentation.create.steps.Step2Schedule
import com.cil.shift.feature.habits.presentation.create.steps.Step3CustomizeAndPreview

@Composable
fun MultiStepCreateHabitScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CreateEditHabitViewModel = viewModel {
        throw IllegalStateException("ViewModel should be provided via DI")
    }
) {
    val state by viewModel.state.collectAsState()
    var currentStep by remember { mutableStateOf(0) }

    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()

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
                        onIconSelect = { viewModel.onEvent(CreateEditHabitEvent.IconSelected(it)) },
                        onSuggestionSelect = { suggestion ->
                            // Pass the suggestion with localized name
                            val localizedName = LocalizationHelpers.getLocalizedHabitName(suggestion.name, currentLanguage)
                            val localizedSuggestion = suggestion.copy(name = localizedName)
                            viewModel.onEvent(CreateEditHabitEvent.SuggestionSelected(localizedSuggestion))
                        }
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
                        isNegative = state.isNegative,
                        quitStartDate = state.quitStartDate,
                        onColorSelect = { viewModel.onEvent(CreateEditHabitEvent.ColorSelected(it)) },
                        onHabitTypeSelect = { viewModel.onEvent(CreateEditHabitEvent.HabitTypeSelected(it)) },
                        onTargetValueChange = { viewModel.onEvent(CreateEditHabitEvent.TargetValueChanged(it)) },
                        onTargetUnitChange = { viewModel.onEvent(CreateEditHabitEvent.TargetUnitChanged(it)) },
                        onNotesChange = { viewModel.onEvent(CreateEditHabitEvent.NotesChanged(it)) },
                        onIsNegativeChange = { viewModel.onEvent(CreateEditHabitEvent.IsNegativeChanged(it)) },
                        onQuitStartDateChange = { viewModel.onEvent(CreateEditHabitEvent.QuitStartDateChanged(it)) }
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

            // Honey requirement banner (show on last step)
            if (currentStep == 2 && state.honeyRequired != null && !state.isPremium && state.habitId == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFD700))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\uD83C\uDF6F",
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = StringResources.thisHabitCosts.get(currentLanguage).replace("%d", state.honeyRequired.toString()),
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        if (!state.canAffordHabit) {
                            Text(
                                text = " (${state.honeyRequired!! - state.honeyBalance} ${StringResources.missing.get(currentLanguage)})",
                                color = Color.Black.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
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
                    containerColor = if (currentStep == 2 && state.honeyRequired != null && !state.canAffordHabit)
                        Color(0xFFFF6B6B) // Red if can't afford
                    else Color(0xFF4E7CFF)
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
                                0 -> StringResources.nextStep.get(currentLanguage)
                                1 -> StringResources.next.get(currentLanguage)
                                else -> {
                                    if (state.habitId != null) StringResources.editHabit.get(currentLanguage)
                                    else if (state.honeyRequired != null && !state.isPremium) {
                                        if (state.canAffordHabit) StringResources.createWithHoney.get(currentLanguage).replace("%d", state.honeyRequired.toString())
                                        else StringResources.honeyNotEnough.get(currentLanguage)
                                    } else StringResources.createHabit.get(currentLanguage)
                                }
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (currentStep == 2 && (state.honeyRequired == null || state.canAffordHabit)) {
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

        // Not Enough Honey BottomSheet
        NotEnoughHoneyBottomSheet(
            visible = state.showNotEnoughHoneyDialog,
            featureName = StringResources.newHabit.get(currentLanguage),
            cost = state.honeyRequired ?: 0,
            currentBalance = state.honeyBalance,
            strings = HoneyBottomSheetStrings(
                title = StringResources.honeyNotEnough.get(currentLanguage),
                yourBalance = StringResources.yourBalance.get(currentLanguage),
                needed = StringResources.needed.get(currentLanguage),
                howToEarn = StringResources.howToEarnHoney.get(currentLanguage),
                dailyLogin = StringResources.dailyLogin.get(currentLanguage),
                completeHabit = StringResources.completeHabit.get(currentLanguage),
                sevenDayStreak = StringResources.sevenDayStreak.get(currentLanguage),
                getPremium = StringResources.getPremiumUnlimited.get(currentLanguage),
                watchAd = StringResources.watchAdForHoney.get(currentLanguage),
                later = StringResources.earnMoreLater.get(currentLanguage)
            ),
            onGetPremium = {
                viewModel.onEvent(CreateEditHabitEvent.DismissNotEnoughHoneyDialog)
                onNavigateToPremium()
            },
            onWatchAd = null, // TODO: Implement ad watching
            onDismiss = {
                viewModel.onEvent(CreateEditHabitEvent.DismissNotEnoughHoneyDialog)
            }
        )
    }
}
