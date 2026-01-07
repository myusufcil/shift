package com.cil.shift.feature.habits.presentation.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cil.shift.core.designsystem.components.NotEnoughHoneyDialog
import com.cil.shift.feature.habits.presentation.create.components.ColorPicker
import com.cil.shift.feature.habits.presentation.create.components.FrequencySelector
import com.cil.shift.feature.habits.presentation.create.components.CategorizedIconPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditHabitScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateEditHabitViewModel = viewModel {
        // TODO: Inject dependencies via DI
        throw IllegalStateException("ViewModel should be provided via DI")
    }
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.habitId == null) "Create New Habit" else "Edit Habit",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A1628)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0A1628))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Name Input
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Name your habit",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.onEvent(CreateEditHabitEvent.NameChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., Morning Meditation") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00D9FF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                            cursorColor = Color(0xFF00D9FF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.1f))

                // Icon Picker
                CategorizedIconPicker(
                    selectedIcon = state.selectedIcon,
                    onIconSelected = { viewModel.onEvent(CreateEditHabitEvent.IconSelected(it)) }
                )

                Divider(color = Color.White.copy(alpha = 0.1f))

                // Color Picker
                ColorPicker(
                    selectedColor = state.selectedColor,
                    onColorSelected = { viewModel.onEvent(CreateEditHabitEvent.ColorSelected(it)) }
                )

                Divider(color = Color.White.copy(alpha = 0.1f))

                // Frequency Selector
                FrequencySelector(
                    frequency = state.frequency,
                    onFrequencyChanged = { viewModel.onEvent(CreateEditHabitEvent.FrequencyChanged(it)) },
                    onWeekdayToggled = { viewModel.onEvent(CreateEditHabitEvent.WeekdayToggled(it)) }
                )

                Divider(color = Color.White.copy(alpha = 0.1f))

                // Reminder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set reminder",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Switch(
                        checked = state.hasReminder,
                        onCheckedChange = { viewModel.onEvent(CreateEditHabitEvent.ReminderToggled(it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF00D9FF),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF1A2942)
                        )
                    )
                }

                if (state.hasReminder) {
                    OutlinedTextField(
                        value = state.reminderTime ?: "",
                        onValueChange = { viewModel.onEvent(CreateEditHabitEvent.ReminderTimeChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("HH:MM (e.g., 09:00)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00D9FF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                            cursorColor = Color(0xFF00D9FF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Error message
                if (state.error != null) {
                    Text(
                        text = state.error!!,
                        color = Color(0xFFFF6B6B),
                        fontSize = 14.sp
                    )
                }

                // Save Button
                Button(
                    onClick = { viewModel.onEvent(CreateEditHabitEvent.SaveHabit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = state.isValid && !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00D9FF),
                        disabledContainerColor = Color(0xFF00D9FF).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black
                        )
                    } else {
                        Text(
                            text = "Create Habit",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Honey requirement banner
            if (state.honeyRequired != null && !state.isPremium && state.habitId == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color(0xFFFFD700).copy(alpha = 0.9f))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\uD83C\uDF6F",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "This habit costs ${state.honeyRequired} honey",
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!state.canAffordHabit) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(Need ${state.honeyRequired!! - state.honeyBalance} more)",
                                color = Color.Black.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Not Enough Honey Dialog
        NotEnoughHoneyDialog(
            visible = state.showNotEnoughHoneyDialog,
            featureName = "Create New Habit",
            cost = state.honeyRequired ?: 0,
            currentBalance = state.honeyBalance,
            onGetPremium = {
                viewModel.onEvent(CreateEditHabitEvent.DismissNotEnoughHoneyDialog)
                // TODO: Navigate to premium screen
            },
            onDismiss = {
                viewModel.onEvent(CreateEditHabitEvent.DismissNotEnoughHoneyDialog)
            }
        )
    }
}
