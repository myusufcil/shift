package com.cil.shift.feature.habits.presentation.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HabitItemMeasurable(
    name: String,
    currentValue: Int,
    targetValue: Int,
    unit: String,
    icon: String,
    color: Color,
    streak: Int = 0,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animate current value
    val animatedCurrentValue by animateIntAsState(
        targetValue = currentValue,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    // Animate percentage
    val percentage = if (targetValue > 0) (currentValue.toFloat() / targetValue * 100).toInt() else 0
    val animatedPercentage by animateIntAsState(
        targetValue = percentage,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    Row(
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
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

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (unit.isNotBlank()) {
                            "$animatedCurrentValue$unit / $targetValue$unit"
                        } else {
                            "$animatedCurrentValue / $targetValue"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.6f)
                    )

                    if (currentValue >= targetValue && targetValue > 0) {
                        Text(
                            text = "DONE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4ECDC4),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF4ECDC4).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = 0.1f))
                    .clickable(onClick = onDecrement),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "−",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = textColor
                )
            }

            // Show checkmark when completed, otherwise show percentage
            if (currentValue >= targetValue && targetValue > 0) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4ECDC4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Text(
                    text = "$animatedPercentage%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    modifier = Modifier.widthIn(min = 40.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun getIconEmoji(icon: String): String {
    return when (icon.lowercase()) {
        // Health
        "water", "wat" -> "💧"
        "vegetables", "veg" -> "🥦"
        "fruit", "fru" -> "🍉"
        "cooking", "coo" -> "🍳"
        "sunrise", "sun" -> "🌅"
        "sunset" -> "🌇"
        "pill", "med" -> "💊"
        // Mindfulness
        "journal", "jou" -> "✍️"
        "pray", "pra" -> "🙏"
        "meditation", "me", "med" -> "🧘"
        "relaxed", "rel" -> "😌"
        "detox", "det" -> "🚫"
        // Learning
        "books", "book", "boo" -> "📚"
        "course", "cou" -> "📝"
        "instrument", "ins" -> "🎷"
        "study", "stu" -> "🧑‍🎓"
        "flute", "flu", "ute" -> "🎺"
        // Active
        "running", "run" -> "🏃"
        "walking", "wal" -> "🚶"
        "dance", "dan" -> "💃"
        "pilates", "pil" -> "🤸"
        "gym", "dumbbell", "dum" -> "🏋️"
        "sports", "spo" -> "⚽"
        "stretching", "str" -> "🤾"
        "yoga", "yog" -> "🧘"
        // Self-care
        "shower", "sho" -> "🚿"
        "skincare", "ski" -> "🧴"
        "haircare", "hai" -> "💆"
        // Social
        "couple", "heart", "hea" -> "💕"
        "party", "par" -> "🥳"
        "family", "fam" -> "👨‍👩‍👧"
        // Financial
        "budget", "bud" -> "💰"
        "invest", "inv" -> "📊"
        "expenses", "exp" -> "💸"
        // Home
        "clean", "cle" -> "🧹"
        "bed" -> "🛏️"
        "laundry", "lau" -> "🧺"
        "dishes", "dis" -> "🪣"
        "bills", "bil" -> "🧾"
        // Additional
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
        else -> {
            if (icon.any { it.code >= 0x1F300 }) icon else "✓"
        }
    }
}
