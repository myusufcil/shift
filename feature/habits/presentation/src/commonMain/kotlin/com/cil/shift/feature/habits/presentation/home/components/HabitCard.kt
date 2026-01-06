package com.cil.shift.feature.habits.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.localization.LocalizationHelpers
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.feature.habits.presentation.home.HabitWithCompletion
import org.koin.compose.koinInject

@Composable
fun HabitCard(
    habitWithCompletion: HabitWithCompletion,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = habitWithCompletion.habit
    val isCompleted = habitWithCompletion.isCompletedToday
    val habitColor = habit.color.toComposeColor()

    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()
    val localizedHabitName = LocalizationHelpers.getLocalizedHabitName(habit.name, currentLanguage)

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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(habitColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getIconEmoji(habit.icon),
                    fontSize = 24.sp
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = localizedHabitName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )

                habit.reminderTime?.let { time ->
                    Text(
                        text = time,
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) habitColor
                    else textColor.copy(alpha = 0.1f)
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Converts hex color string (e.g., "#6C63FF") to Compose Color.
 */
private fun String.toComposeColor(): Color {
    return try {
        val colorString = this.removePrefix("#")
        val colorInt = colorString.toLong(16)
        if (colorString.length == 6) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        Color(0xFF6C63FF) // Default purple color
    }
}

private fun getIconEmoji(icon: String): String {
    return when (icon.lowercase()) {
        // Health
        "water", "wat" -> "💧"
        "vegetables", "veg" -> "🥦"
        "fruit", "fru" -> "🍉"
        "cooking", "coo" -> "🍳"
        "breakfast", "bre", "kahvaltı", "kah" -> "🍳"
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
        // Additional from suggestions
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
            // Try to match if the icon contains any emoji
            if (icon.any { it.code >= 0x1F300 }) {
                icon
            } else {
                // Return first emoji from icon mapping or default
                "✓"
            }
        }
    }
}
