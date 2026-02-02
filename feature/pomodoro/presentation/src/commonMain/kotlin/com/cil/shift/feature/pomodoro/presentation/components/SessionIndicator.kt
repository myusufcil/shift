package com.cil.shift.feature.pomodoro.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ActiveColor = Color(0xFF4ECDC4)

@Composable
fun SessionIndicator(
    totalSessions: Int,
    completedSessions: Int,
    currentSession: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        for (i in 1..totalSessions) {
            when {
                i <= completedSessions -> {
                    // Completed - filled circle
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(ActiveColor)
                    )
                }
                i == currentSession -> {
                    // Current - outlined with pulse
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .alpha(pulseAlpha)
                            .clip(CircleShape)
                            .border(2.dp, ActiveColor, CircleShape)
                    )
                }
                else -> {
                    // Remaining - dim
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(ActiveColor.copy(alpha = 0.2f))
                    )
                }
            }
        }
    }
}
