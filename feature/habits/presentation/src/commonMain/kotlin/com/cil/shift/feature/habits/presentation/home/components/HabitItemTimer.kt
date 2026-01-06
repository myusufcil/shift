package com.cil.shift.feature.habits.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.localization.Language
import com.cil.shift.core.common.localization.StringResources
import kotlinx.coroutines.delay

@Composable
fun HabitItemTimer(
    name: String,
    currentMinutes: Int,
    targetMinutes: Int,
    icon: String,
    color: Color,
    statusLabel: String,
    isCompleted: Boolean = false,
    streak: Int = 0,
    currentLanguage: Language = Language.ENGLISH,
    isTimerRunning: Boolean = false,
    onTimerToggle: () -> Unit = {},
    onTimerTick: () -> Unit = {},
    onTimerReset: () -> Unit = {},
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // When completed, always show 100% progress; otherwise show actual progress
    val progress = if (isCompleted) 1f else (currentMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f)
    val displayLabel = when {
        isCompleted -> StringResources.done.get(currentLanguage)
        isTimerRunning -> StringResources.running.get(currentLanguage)
        else -> statusLabel
    }

    // Real-time timer countdown
    LaunchedEffect(isTimerRunning, currentMinutes) {
        if (isTimerRunning && currentMinutes < targetMinutes) {
            delay(60_000L) // 1 minute
            onTimerTick()
        }
    }

    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getIconEmoji(icon),
                        fontSize = 20.sp,
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        maxLines = 1
                    )

                    // Streak badge
                    StreakBadge(streak = streak)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isCompleted) Color(0xFF4ECDC4).copy(alpha = 0.2f)
                        else color.copy(alpha = 0.2f)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = displayLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) Color(0xFF4ECDC4) else color
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        isCompleted -> Color(0xFF4ECDC4) // Green when completed
                        isTimerRunning -> Color(0xFF4ECDC4)
                        else -> color
                    },
                    trackColor = textColor.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )

                val remainingMinutes = (targetMinutes - currentMinutes).coerceAtLeast(0)
                Text(
                    text = if (isCompleted) {
                        "$targetMinutes ${StringResources.minCompleted.get(currentLanguage)}"
                    } else {
                        "$remainingMinutes ${StringResources.minLeft.get(currentLanguage)}"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isCompleted) Color(0xFF4ECDC4).copy(alpha = 0.7f) else textColor.copy(alpha = 0.5f)
                )
            }

            // Timer control buttons
            if (!isCompleted) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset button (only show if timer has progress)
                    if (currentMinutes > 0) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(textColor.copy(alpha = 0.1f))
                                .clickable { onTimerReset() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                tint = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Play/Pause button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isTimerRunning) Color(0xFF4ECDC4) else color)
                            .clickable { onTimerToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isTimerRunning) "Pause" else "Start",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getIconEmoji(icon: String): String {
    return when (icon.lowercase()) {
        "water", "wat" -> "💧"
        "vegetables", "veg" -> "🥦"
        "fruit", "fru" -> "🍉"
        "cooking", "coo" -> "🍳"
        "sunrise", "sun" -> "🌅"
        "sunset" -> "🌇"
        "pill", "med" -> "💊"
        "journal", "jou" -> "✍️"
        "pray", "pra" -> "🙏"
        "meditation", "me", "med" -> "🧘"
        "relaxed", "rel" -> "😌"
        "detox", "det" -> "🚫"
        "books", "book", "boo" -> "📚"
        "course", "cou" -> "📝"
        "instrument", "ins" -> "🎷"
        "study", "stu" -> "🧑‍🎓"
        "flute", "flu", "ute" -> "🎺"
        "running", "run" -> "🏃"
        "walking", "wal" -> "🚶"
        "dance", "dan" -> "💃"
        "pilates", "pil" -> "🤸"
        "gym", "dumbbell", "dum" -> "🏋️"
        "sports", "spo" -> "⚽"
        "stretching", "str" -> "🤾"
        "yoga", "yog" -> "🧘"
        "shower", "sho" -> "🚿"
        "skincare", "ski" -> "🧴"
        "haircare", "hai" -> "💆"
        "couple", "heart", "hea" -> "💕"
        "party", "par" -> "🥳"
        "family", "fam" -> "👨‍👩‍👧"
        "budget", "bud" -> "💰"
        "invest", "inv" -> "📊"
        "expenses", "exp" -> "💸"
        "clean", "cle" -> "🧹"
        "bed" -> "🛏️"
        "laundry", "lau" -> "🧺"
        "dishes", "dis" -> "🪣"
        "bills", "bil" -> "🧾"
        "leaf", "lea" -> "🍃"
        "brain", "bra" -> "🧠"
        "fire", "fir" -> "🔥"
        "moon", "mo" -> "🌙"
        "bulb", "bul" -> "💡"
        "smile", "smi" -> "😊"
        "check", "che" -> "✅"
        "coffee", "cof" -> "☕"
        "sleep", "sle" -> "😴"
        "music", "mus" -> "🎵"
        "art", "palette", "pale", "pal" -> "🎨"
        "briefcase", "bri", "work" -> "💼"
        else -> if (icon.any { it.code >= 0x1F300 }) icon else "✓"
    }
}
