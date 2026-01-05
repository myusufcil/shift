package com.cil.shift.feature.settings.presentation.achievements

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.achievement.Achievement
import com.cil.shift.core.common.localization.Language
import kotlinx.coroutines.delay

@Composable
fun AchievementUnlockPopup(
    achievement: Achievement?,
    currentLanguage: Language,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(achievement) {
        if (achievement != null) {
            isVisible = true
            delay(4000) // Show for 4 seconds
            isVisible = false
            delay(300) // Wait for exit animation
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible && achievement != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut(),
        modifier = modifier
    ) {
        achievement?.let { ach ->
            AchievementUnlockCard(
                achievement = ach,
                currentLanguage = currentLanguage
            )
        }
    }
}

@Composable
private fun AchievementUnlockCard(
    achievement: Achievement,
    currentLanguage: Language
) {
    val (primaryColor, secondaryColor) = getTierColors(achievement.tier)
    val title = if (currentLanguage == Language.TURKISH) achievement.titleTr else achievement.title

    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "unlock_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = glowAlpha),
                        secondaryColor.copy(alpha = glowAlpha)
                    )
                )
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A2942))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Badge icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(colors = listOf(primaryColor, secondaryColor))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getAchievementEmoji(achievement.icon),
                        fontSize = 28.sp
                    )
                }

                // Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (currentLanguage == Language.TURKISH) "BASARI KAZANILDI!" else "ACHIEVEMENT UNLOCKED!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = achievement.tier.name,
                        fontSize = 12.sp,
                        color = primaryColor.copy(alpha = 0.8f)
                    )
                }

                // Trophy animation
                Text(
                    text = "🎉",
                    fontSize = 32.sp,
                    modifier = Modifier.scale(scale)
                )
            }
        }
    }
}

private val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
