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
import androidx.compose.ui.draw.shadow
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
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val accentColor = Color(0xFF4E7CFF)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Title
        Text(
            text = "Name your habit",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        // Habit name input card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardColor)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Habit Name",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.6f),
                letterSpacing = 0.5.sp
            )

            TextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                placeholder = {
                    Text(
                        text = "e.g., Read 10 pages...",
                        fontSize = 15.sp,
                        color = textColor.copy(alpha = 0.35f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = accentColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Text(
                text = "💡 Be specific, it helps stick to the plan.",
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.45f)
            )
        }

        // Icon selector card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardColor)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Choose an icon",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )

                // Preview of selected icon
                if (selectedIcon.isNotEmpty()) {
                    val selectedEmoji = availableIcons.find { it.id == selectedIcon }?.emoji ?: ""
                    if (selectedEmoji.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(accentColor.copy(alpha = 0.1f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = selectedEmoji,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Selected",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = accentColor
                            )
                        }
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(borderColor)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableIcons) { icon ->
                    IconItem(
                        icon = icon.emoji,
                        isSelected = selectedIcon == icon.id,
                        onClick = { onIconSelect(icon.id) },
                        cardColor = MaterialTheme.colorScheme.background,
                        accentColor = accentColor
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
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) accentColor.copy(alpha = 0.15f)
                else cardColor
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else Color.Gray.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 22.sp
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
