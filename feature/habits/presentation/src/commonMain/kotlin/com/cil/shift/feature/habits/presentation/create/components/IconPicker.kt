package com.cil.shift.feature.habits.presentation.create.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.feature.habits.presentation.create.availableIcons

@Composable
fun IconPicker(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Choose an icon",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            items(availableIcons) { icon ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (icon == selectedIcon) Color(0xFF00D9FF).copy(alpha = 0.2f)
                            else Color(0xFF1A2942)
                        )
                        .border(
                            width = if (icon == selectedIcon) 2.dp else 0.dp,
                            color = if (icon == selectedIcon) Color(0xFF00D9FF) else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onIconSelected(icon) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getIconEmoji(icon),
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}

private fun getIconEmoji(icon: String): String {
    return when (icon) {
        "work", "briefcase" -> "💼"
        "fitness", "workout" -> "🏋️"
        "book", "read" -> "📚"
        "meditation", "mindfulness" -> "🧘"
        "water", "hydration" -> "💧"
        "sleep" -> "😴"
        "nutrition", "food" -> "🥗"
        "study" -> "📖"
        "music" -> "🎵"
        "art" -> "🎨"
        "check" -> "✓"
        else -> "✓"
    }
}
