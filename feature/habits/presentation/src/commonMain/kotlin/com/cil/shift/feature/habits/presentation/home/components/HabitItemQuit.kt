package com.cil.shift.feature.habits.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.currentTimestamp

/**
 * Habit card for QUIT type habits.
 * Displays a "days clean" counter showing how long since the user quit.
 */
@Composable
fun HabitItemQuit(
    name: String,
    quitStartDate: Long?,
    icon: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    // Calculate days since quit
    val daysSinceQuit = if (quitStartDate != null) {
        val now = currentTimestamp()
        val diffMs = now - quitStartDate
        (diffMs / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    } else {
        0
    }

    // Milestone check
    val milestone = getMilestone(daysSinceQuit)
    val isMilestone = milestone != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(
                width = if (isMilestone) 2.dp else 1.dp,
                color = if (isMilestone) color else textColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getQuitIconEmoji(icon),
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Name
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )

        // Days counter - right aligned, compact
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Milestone emoji
            if (isMilestone) {
                Text(text = "🎉", fontSize = 16.sp)
            }

            Text(
                text = "$daysSinceQuit",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = if (daysSinceQuit == 1) "day" else "days",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.5f)
            )
        }
    }
}

private fun getMilestone(days: Int): String? {
    return when (days) {
        1 -> "First Day!"
        7 -> "1 Week!"
        14 -> "2 Weeks!"
        30 -> "1 Month!"
        60 -> "2 Months!"
        90 -> "3 Months!"
        180 -> "6 Months!"
        365 -> "1 Year!"
        730 -> "2 Years!"
        else -> null
    }
}

private fun getQuitIconEmoji(icon: String): String {
    return when (icon.lowercase()) {
        // Quit specific
        "cigarette", "cig" -> "🚬"
        "wine", "alcohol", "alc" -> "🍷"
        "beer" -> "🍺"
        "phone", "social", "soc" -> "📱"
        "coffee", "cof", "caffeine" -> "☕"
        "junk", "food", "foo", "utensils" -> "🍴"
        "soda", "drink", "cola" -> "🥤"
        "sugar", "candy" -> "🍬"
        "gaming", "game" -> "🎮"
        "tv", "netflix" -> "📺"
        "shopping" -> "🛍️"
        "gambling" -> "🎰"
        "cookie" -> "🍪"
        "chocolate" -> "🍫"
        "ice" -> "🍦"
        "donut" -> "🍩"
        "pizza" -> "🍕"
        "burger" -> "🍔"
        else -> if (icon.any { it.code >= 0x1F300 }) icon else "🚭"
    }
}
