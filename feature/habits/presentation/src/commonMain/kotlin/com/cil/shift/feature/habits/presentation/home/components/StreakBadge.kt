package com.cil.shift.feature.habits.presentation.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StreakBadge(
    streak: Int,
    modifier: Modifier = Modifier
) {
    if (streak < 2) return // Only show badge for streaks of 2+ days

    // Pulsing animation for the flame
    val infiniteTransition = rememberInfiniteTransition(label = "streak_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Color based on streak length
    val (flameColor, bgColor) = when {
        streak >= 30 -> Color(0xFFFF4500) to Color(0xFFFF4500).copy(alpha = 0.15f) // Red-orange for 30+ days
        streak >= 14 -> Color(0xFFFF6B35) to Color(0xFFFF6B35).copy(alpha = 0.15f) // Orange for 14+ days
        streak >= 7 -> Color(0xFFFFAB00) to Color(0xFFFFAB00).copy(alpha = 0.15f) // Amber for 7+ days
        else -> Color(0xFFFFD54F) to Color(0xFFFFD54F).copy(alpha = 0.15f) // Yellow for 2-6 days
    }

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Animated flame emoji
        Text(
            text = "🔥",
            fontSize = 12.sp,
            modifier = Modifier.scale(scale)
        )

        // Streak count
        Text(
            text = streak.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = flameColor
        )
    }
}

@Composable
fun StreakBadgeLarge(
    streak: Int,
    modifier: Modifier = Modifier
) {
    if (streak < 2) return

    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "streak_pulse_large")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Gradient colors based on streak
    val gradientColors = when {
        streak >= 30 -> listOf(Color(0xFFFF4500), Color(0xFFFF6B35))
        streak >= 14 -> listOf(Color(0xFFFF6B35), Color(0xFFFFAB00))
        streak >= 7 -> listOf(Color(0xFFFFAB00), Color(0xFFFFD54F))
        else -> listOf(Color(0xFFFFD54F), Color(0xFFFFE082))
    }

    Box(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = gradientColors.map { it.copy(alpha = glowAlpha) }
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "🔥",
                fontSize = 18.sp,
                modifier = Modifier.scale(scale)
            )
            Text(
                text = "$streak days",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = gradientColors[0]
            )
        }
    }
}

private val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
