package com.cil.shift.feature.pomodoro.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cil.shift.feature.pomodoro.presentation.TimerStatus

private val AccentColor = Color(0xFF4ECDC4)

@Composable
fun PhaseControls(
    status: TimerStatus,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // Reset button - visible when not idle
        if (status != TimerStatus.IDLE) {
            IconButton(
                onClick = onReset,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Play/Pause button
        FilledIconButton(
            onClick = {
                when (status) {
                    TimerStatus.IDLE -> onStart()
                    TimerStatus.RUNNING -> onPause()
                    TimerStatus.PAUSED -> onResume()
                }
            },
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = AccentColor,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = if (status == TimerStatus.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = when (status) {
                    TimerStatus.RUNNING -> "Pause"
                    TimerStatus.PAUSED -> "Resume"
                    TimerStatus.IDLE -> "Start"
                },
                modifier = Modifier.size(32.dp)
            )
        }

        // Skip button - visible when running or paused
        if (status == TimerStatus.RUNNING || status == TimerStatus.PAUSED) {
            IconButton(
                onClick = onSkip,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Skip",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
