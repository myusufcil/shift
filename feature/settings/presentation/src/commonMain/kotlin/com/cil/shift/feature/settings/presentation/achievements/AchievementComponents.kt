package com.cil.shift.feature.settings.presentation.achievements

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.achievement.Achievement
import com.cil.shift.core.common.achievement.AchievementTier
import com.cil.shift.core.common.localization.Language

/**
 * Get tier colors for gradients and accents
 */
fun getTierColors(tier: AchievementTier): Pair<Color, Color> {
    return when (tier) {
        AchievementTier.BRONZE -> Color(0xFFCD7F32) to Color(0xFF8B4513)
        AchievementTier.SILVER -> Color(0xFFC0C0C0) to Color(0xFF808080)
        AchievementTier.GOLD -> Color(0xFFFFD700) to Color(0xFFDAA520)
        AchievementTier.PLATINUM -> Color(0xFFE5E4E2) to Color(0xFF9EA2A8)
        AchievementTier.DIAMOND -> Color(0xFFB9F2FF) to Color(0xFF4ECDC4)
    }
}

/**
 * Get emoji icon for achievement
 */
fun getAchievementEmoji(icon: String): String {
    return when (icon.lowercase()) {
        "fire" -> "🔥"
        "check" -> "✅"
        "plus" -> "➕"
        "star" -> "⭐"
        "sunrise" -> "🌅"
        "moon" -> "🌙"
        "heart" -> "❤️"
        "trophy" -> "🏆"
        "medal" -> "🏅"
        "crown" -> "👑"
        else -> "🎖️"
    }
}

@Composable
fun AchievementCard(
    achievement: Achievement,
    isUnlocked: Boolean,
    progress: Int,
    currentLanguage: Language,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    val (primaryColor, secondaryColor) = getTierColors(achievement.tier)

    // Animation for unlocked badges
    val infiniteTransition = rememberInfiniteTransition(label = "achievement_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val title = if (currentLanguage == Language.TURKISH) achievement.titleTr else achievement.title
    val description = if (currentLanguage == Language.TURKISH) achievement.descriptionTr else achievement.description

    val progressPercent = if (achievement.requiredValue > 0) {
        (progress.toFloat() / achievement.requiredValue * 100).coerceAtMost(100f)
    } else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .then(
                if (isUnlocked) {
                    Modifier.border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(primaryColor.copy(alpha = glowAlpha), secondaryColor.copy(alpha = glowAlpha))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = textColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            )
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
                        if (isUnlocked) {
                            Brush.linearGradient(colors = listOf(primaryColor, secondaryColor))
                        } else {
                            Brush.linearGradient(colors = listOf(
                                textColor.copy(alpha = 0.1f),
                                textColor.copy(alpha = 0.05f)
                            ))
                        }
                    )
                    .alpha(if (isUnlocked) 1f else 0.5f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getAchievementEmoji(achievement.icon),
                    fontSize = 28.sp
                )
            }

            // Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isUnlocked) textColor else textColor.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Tier badge
                    Text(
                        text = achievement.tier.name,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(primaryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Progress bar (only show if not unlocked)
                if (!isUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(textColor.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressPercent / 100f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(primaryColor, secondaryColor)
                                        )
                                    )
                            )
                        }
                        Text(
                            text = "$progress/${achievement.requiredValue}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    // Show "Unlocked" badge
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentLanguage == Language.TURKISH) "KAZANILDI" else "UNLOCKED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4ECDC4),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF4ECDC4).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementMiniCard(
    achievement: Achievement,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    val (primaryColor, secondaryColor) = getTierColors(achievement.tier)

    Box(
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isUnlocked) {
                    Brush.linearGradient(colors = listOf(primaryColor, secondaryColor))
                } else {
                    Brush.linearGradient(colors = listOf(
                        Color.Gray.copy(alpha = 0.3f),
                        Color.Gray.copy(alpha = 0.1f)
                    ))
                }
            )
            .alpha(if (isUnlocked) 1f else 0.4f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = getAchievementEmoji(achievement.icon),
            fontSize = 28.sp
        )
    }
}

private val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
