package com.cil.shift.feature.pomodoro.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.haptic.HapticType
import com.cil.shift.core.common.haptic.getHapticFeedbackManager
import com.cil.shift.core.common.localization.StringResources
import com.cil.shift.core.common.localization.localized
import com.cil.shift.feature.pomodoro.presentation.components.CircularCountdown
import com.cil.shift.feature.pomodoro.presentation.components.PhaseControls
import com.cil.shift.feature.pomodoro.presentation.components.PomodoroSettingsSheet
import com.cil.shift.feature.pomodoro.presentation.components.SessionIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    onNavigateBack: () -> Unit,
    viewModel: PomodoroViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val hapticManager = remember { getHapticFeedbackManager() }

    val phaseLabel = when (state.phase) {
        PomodoroPhase.FOCUS -> StringResources.pomodoroFocus.localized()
        PomodoroPhase.SHORT_BREAK -> StringResources.pomodoroShortBreak.localized()
        PomodoroPhase.LONG_BREAK -> StringResources.pomodoroLongBreak.localized()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = StringResources.pomodoroFocusTimer.localized(),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        hapticManager.performHaptic(HapticType.LIGHT)
                        viewModel.onEvent(PomodoroEvent.ToggleSettings)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = StringResources.pomodoroSettings.localized()
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular countdown - centered in available space
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularCountdown(
                    remainingSeconds = state.remainingSeconds,
                    totalSeconds = state.totalPhaseSeconds,
                    phase = state.phase,
                    phaseLabel = phaseLabel
                )
            }

            // Session indicator
            SessionIndicator(
                totalSessions = state.sessionsBeforeLongBreak,
                completedSessions = state.completedSessions,
                currentSession = state.currentSession
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phase controls
            PhaseControls(
                status = state.status,
                onStart = {
                    hapticManager.performHaptic(HapticType.LIGHT)
                    viewModel.onEvent(PomodoroEvent.Start)
                },
                onPause = {
                    hapticManager.performHaptic(HapticType.LIGHT)
                    viewModel.onEvent(PomodoroEvent.Pause)
                },
                onResume = {
                    hapticManager.performHaptic(HapticType.LIGHT)
                    viewModel.onEvent(PomodoroEvent.Resume)
                },
                onReset = {
                    hapticManager.performHaptic(HapticType.MEDIUM)
                    viewModel.onEvent(PomodoroEvent.Reset)
                },
                onSkip = {
                    hapticManager.performHaptic(HapticType.MEDIUM)
                    viewModel.onEvent(PomodoroEvent.Skip)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = StringResources.pomodoroToday.localized(),
                    value = "${state.todayFocusMinutes} ${StringResources.pomodoroMinutes.localized()}"
                )
                StatItem(
                    label = StringResources.pomodoroTotalSessions.localized(),
                    value = "${state.totalSessions}"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Settings bottom sheet
    if (state.showSettings) {
        PomodoroSettingsSheet(
            state = state,
            onEvent = { viewModel.onEvent(it) },
            onDismiss = { viewModel.onEvent(PomodoroEvent.ToggleSettings) }
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}
