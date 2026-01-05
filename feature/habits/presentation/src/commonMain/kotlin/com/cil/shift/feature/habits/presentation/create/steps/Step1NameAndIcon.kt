package com.cil.shift.feature.habits.presentation.create.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Step1NameAndIcon(
    name: String,
    selectedIcon: String,
    onNameChange: (String) -> Unit,
    onIconSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Title
        Text(
            text = "Name your habit",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        // Habit name input
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text(
                        text = "e.g., Read 10 pages...",
                        fontSize = 15.sp,
                        color = textColor.copy(alpha = 0.4f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = cardColor,
                    unfocusedContainerColor = cardColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = Color(0xFF00D9FF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Text(
                text = "Be specific, it helps stick to the plan.",
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.5f)
            )
        }

        // Icon selector
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Choose an icon",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )

                // Preview of selected icon
                if (selectedIcon.isNotEmpty()) {
                    val selectedEmoji = availableIcons.find { it.id == selectedIcon }?.emoji ?: ""
                    if (selectedEmoji.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SELECTED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00D9FF),
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF00D9FF).copy(alpha = 0.2f))
                                    .border(
                                        width = 1.5.dp,
                                        color = Color(0xFF00D9FF),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedEmoji,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableIcons) { icon ->
                    IconItem(
                        icon = icon.emoji,
                        isSelected = selectedIcon == icon.id,
                        onClick = { onIconSelect(icon.id) },
                        cardColor = cardColor
                    )
                }
            }
        }
    }
}

@Composable
private fun IconItem(
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    cardColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color(0xFF4E7CFF).copy(alpha = 0.2f)
                else cardColor
            )
            .border(
                width = 2.dp,
                color = if (isSelected) Color(0xFF4E7CFF) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
    }
}

data class IconData(val id: String, val emoji: String)

private val availableIcons = listOf(
    IconData("water", "💧"),
    IconData("tools", "🛠"),
    IconData("camera", "📷"),
    IconData("umbrella", "☂️"),
    IconData("run", "🏃"),
    IconData("moon", "🌙"),
    IconData("code", "💻"),
    IconData("book", "📚"),
    IconData("utensils", "🍴"),
    IconData("leaf", "🍃"),
    IconData("dumbbell", "🏋️"),
    IconData("meditation", "🧘"),
    IconData("music", "🎵"),
    IconData("palette", "🎨"),
    IconData("briefcase", "💼"),
    IconData("coffee", "☕"),
    IconData("brain", "🧠"),
    IconData("heart", "❤️")
)
