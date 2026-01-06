package com.cil.shift.feature.habits.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Habit card for NEGATIVE/reduce type habits.
 * Displays current value vs limit with warning when exceeded.
 */
@Composable
fun HabitItemNegative(
    name: String,
    currentValue: Int,
    limitValue: Int,
    unit: String,
    icon: String,
    color: Color,
    onIncrement: (Int) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    val isExceeded = currentValue > limitValue
    val progress = if (limitValue > 0) (currentValue.toFloat() / limitValue).coerceIn(0f, 1f) else 0f

    // Colors based on status
    val statusColor = when {
        isExceeded -> Color(0xFFFF6B6B) // Red when exceeded
        currentValue == limitValue -> Color(0xFFFFB347) // Amber when at limit
        else -> color // Normal color when under limit
    }

    // Dialog state
    var showInputDialog by remember { mutableStateOf(false) }

    // Smart increment values based on unit
    val incrementValues = remember(unit) {
        when (unit.lowercase()) {
            "g", "grams", "ml", "cal" -> listOf(1, 5, 10, 25)
            "cups", "glasses", "times", "cigarettes", "drinks", "snacks", "cans" -> listOf(1, 2, 3)
            "hours" -> listOf(1, 2)
            else -> listOf(1, 5, 10)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(
                width = if (isExceeded) 2.dp else 1.dp,
                color = if (isExceeded) statusColor else textColor.copy(alpha = 0.1f),
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
                .background(statusColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getNegativeIconEmoji(icon),
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Name with warning
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    maxLines = 1
                )
                if (isExceeded) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Progress bar with value overlay
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(textColor.copy(alpha = 0.08f))
                )
                // Progress fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusColor.copy(alpha = 0.3f))
                )
                // Value text centered
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$currentValue / $limitValue $unit",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isExceeded) statusColor else textColor.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Add button
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.15f))
                .clickable { showInputDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Log usage",
                tint = statusColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }

    // Input Dialog
    if (showInputDialog) {
        NegativeInputDialog(
            habitName = name,
            currentValue = currentValue,
            limitValue = limitValue,
            unit = unit,
            color = statusColor,
            incrementValues = incrementValues,
            onIncrement = { amount ->
                onIncrement(amount)
            },
            onDismiss = { showInputDialog = false }
        )
    }
}

@Composable
private fun NegativeInputDialog(
    habitName: String,
    currentValue: Int,
    limitValue: Int,
    unit: String,
    color: Color,
    incrementValues: List<Int>,
    onIncrement: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(cardColor)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = habitName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            // Current value display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$currentValue",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = " / $limitValue $unit",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Quick add buttons
            Text(
                text = "Add",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.5f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                incrementValues.forEach { amount ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(color.copy(alpha = 0.15f))
                            .clickable {
                                onIncrement(amount)
                                onDismiss()
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+$amount",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = color
                        )
                    }
                }
            }

            // Cancel button
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = textColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}

private fun getNegativeIconEmoji(icon: String): String {
    return when (icon.lowercase()) {
        // Drinks
        "coffee", "cof" -> "☕"
        "wine", "alcohol", "alc" -> "🍷"
        "beer" -> "🍺"
        "water" -> "💧"
        "soda", "cola" -> "🥤"

        // Tech
        "phone", "social", "soc" -> "📱"
        "tv", "netflix" -> "📺"
        "game", "gaming" -> "🎮"

        // Food
        "utensils", "food", "foo" -> "🍴"
        "snack" -> "🍿"
        "sugar", "candy" -> "🍬"
        "chocolate" -> "🍫"
        "cookie" -> "🍪"
        "cake" -> "🍰"
        "fastfood", "burger" -> "🍔"
        "fries" -> "🍟"
        "pizza" -> "🍕"
        "icecream", "ice" -> "🍦"
        "donut" -> "🍩"

        // Other
        "cigarette", "cig" -> "🚬"
        "shopping" -> "🛍️"

        else -> if (icon.any { it.code >= 0x1F300 }) icon else "📉"
    }
}
