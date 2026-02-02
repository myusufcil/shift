package com.cil.shift.feature.pomodoro.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.localization.StringResources
import com.cil.shift.core.common.localization.localized
import com.cil.shift.feature.pomodoro.presentation.LinkableHabit
import com.cil.shift.feature.pomodoro.presentation.PomodoroEvent
import com.cil.shift.feature.pomodoro.presentation.PomodoroState
import com.cil.shift.feature.pomodoro.presentation.TimerStatus

private val AccentColor = Color(0xFF4ECDC4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroSettingsSheet(
    state: PomodoroState,
    onEvent: (PomodoroEvent) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditable = state.status == TimerStatus.IDLE

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = StringResources.pomodoroSettings.localized(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Focus Duration
            DurationStepper(
                label = StringResources.pomodoroFocusDuration.localized(),
                value = state.focusDuration,
                min = 5,
                max = 60,
                step = 5,
                enabled = isEditable,
                suffix = StringResources.pomodoroMinutes.localized(),
                onValueChange = { onEvent(PomodoroEvent.UpdateFocusDuration(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Short Break
            DurationStepper(
                label = StringResources.pomodoroShortBreak.localized(),
                value = state.shortBreakDuration,
                min = 1,
                max = 15,
                step = 1,
                enabled = isEditable,
                suffix = StringResources.pomodoroMinutes.localized(),
                onValueChange = { onEvent(PomodoroEvent.UpdateShortBreak(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Long Break
            DurationStepper(
                label = StringResources.pomodoroLongBreak.localized(),
                value = state.longBreakDuration,
                min = 5,
                max = 30,
                step = 5,
                enabled = isEditable,
                suffix = StringResources.pomodoroMinutes.localized(),
                onValueChange = { onEvent(PomodoroEvent.UpdateLongBreak(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sessions before long break
            DurationStepper(
                label = StringResources.pomodoroSessionCount.localized(),
                value = state.sessionsBeforeLongBreak,
                min = 2,
                max = 8,
                step = 1,
                enabled = isEditable,
                suffix = "",
                onValueChange = { onEvent(PomodoroEvent.UpdateSessionCount(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // Auto-start breaks
            ToggleRow(
                label = StringResources.pomodoroAutoStartBreaks.localized(),
                checked = state.autoStartBreaks,
                onCheckedChange = { onEvent(PomodoroEvent.SetAutoStartBreaks(it)) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Auto-start focus
            ToggleRow(
                label = StringResources.pomodoroAutoStartFocus.localized(),
                checked = state.autoStartFocus,
                onCheckedChange = { onEvent(PomodoroEvent.SetAutoStartFocus(it)) }
            )

            if (state.availableHabits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Habit linking
                Text(
                    text = StringResources.pomodoroLinkHabit.localized(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                // "None" option
                TextButton(
                    onClick = { onEvent(PomodoroEvent.LinkHabit(null)) }
                ) {
                    Text(
                        text = "—",
                        color = if (state.linkedHabitId == null) AccentColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                state.availableHabits.forEach { habit ->
                    TextButton(
                        onClick = { onEvent(PomodoroEvent.LinkHabit(habit.id)) }
                    ) {
                        Text(
                            text = habit.name,
                            color = if (state.linkedHabitId == habit.id) AccentColor
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DurationStepper(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    step: Int,
    enabled: Boolean,
    suffix: String,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onValueChange((value - step).coerceAtLeast(min)) },
                enabled = enabled && value - step >= min,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = if (suffix.isNotEmpty()) "$value $suffix" else "$value",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                modifier = Modifier.width(60.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            IconButton(
                onClick = { onValueChange((value + step).coerceAtMost(max)) },
                enabled = enabled && value + step <= max,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = AccentColor,
                checkedThumbColor = Color.White
            )
        )
    }
}
