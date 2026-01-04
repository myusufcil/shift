package com.cil.shift.feature.habits.presentation.create.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class IconData(
    val id: String,
    val emoji: String,
    val label: String
)

data class IconCategory(
    val name: String,
    val icons: List<IconData>
)

private val iconCategories = listOf(
    IconCategory(
        name = "Health",
        icons = listOf(
            IconData("water", "💧", "Water"),
            IconData("vegetables", "🥦", "Vegetables"),
            IconData("fruit", "🍉", "Fruit"),
            IconData("cooking", "🍳", "Cooking"),
            IconData("sunrise", "🌅", "Sunrise"),
            IconData("sunset", "🌇", "Sunset"),
            IconData("pill", "💊", "Medicine")
        )
    ),
    IconCategory(
        name = "Mindfulness",
        icons = listOf(
            IconData("journal", "✍️", "Journal"),
            IconData("pray", "🙏", "Pray"),
            IconData("meditation", "🧘", "Meditation"),
            IconData("relaxed", "😌", "Relaxed"),
            IconData("detox", "🚫", "Digital Detox")
        )
    ),
    IconCategory(
        name = "Learning",
        icons = listOf(
            IconData("books", "📚", "Books"),
            IconData("course", "📝", "Course"),
            IconData("instrument", "🎷", "Instrument"),
            IconData("study", "🧑‍🎓", "Study")
        )
    ),
    IconCategory(
        name = "Active",
        icons = listOf(
            IconData("running", "🏃", "Running"),
            IconData("walking", "🚶", "Walking"),
            IconData("dance", "💃", "Dance"),
            IconData("pilates", "🤸", "Pilates"),
            IconData("gym", "🏋️", "Gym"),
            IconData("sports", "⚽", "Sports"),
            IconData("stretching", "🤾", "Stretching"),
            IconData("yoga", "🧘", "Yoga")
        )
    ),
    IconCategory(
        name = "Self-care",
        icons = listOf(
            IconData("shower", "🚿", "Shower"),
            IconData("skincare", "🧴", "Skincare"),
            IconData("haircare", "💆", "Haircare")
        )
    ),
    IconCategory(
        name = "Social",
        icons = listOf(
            IconData("couple", "💕", "Partner time"),
            IconData("party", "🥳", "Social activities"),
            IconData("family", "👨‍👩‍👧", "Family time")
        )
    ),
    IconCategory(
        name = "Financial",
        icons = listOf(
            IconData("budget", "💰", "Budget"),
            IconData("invest", "📊", "Investments"),
            IconData("expenses", "💸", "Track expenses")
        )
    ),
    IconCategory(
        name = "Home",
        icons = listOf(
            IconData("clean", "🧹", "Clean"),
            IconData("bed", "🛏️", "Make bed"),
            IconData("laundry", "🧺", "Laundry"),
            IconData("dishes", "🪣", "Dishes"),
            IconData("bills", "🧾", "Bills")
        )
    )
)

@Composable
fun CategorizedIconPicker(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(iconCategories) { category ->
            CategorySection(
                category = category,
                selectedIcon = selectedIcon,
                onIconSelected = onIconSelected
            )
        }
    }
}

@Composable
private fun CategorySection(
    category: IconCategory,
    selectedIcon: String,
    onIconSelected: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = category.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.9f)
        )

        // Icons grid using FlowRow alternative
        IconGrid(
            icons = category.icons,
            selectedIcon = selectedIcon,
            onIconSelected = onIconSelected
        )
    }
}

@Composable
private fun IconGrid(
    icons: List<IconData>,
    selectedIcon: String,
    onIconSelected: (String) -> Unit
) {
    // Simple grid layout with rows
    val itemsPerRow = 4
    val rows = icons.chunked(itemsPerRow)

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rows.forEach { rowIcons ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowIcons.forEach { icon ->
                    IconItem(
                        icon = icon,
                        isSelected = icon.id == selectedIcon,
                        onIconSelected = onIconSelected,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if row is not complete
                repeat(itemsPerRow - rowIcons.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun IconItem(
    icon: IconData,
    isSelected: Boolean,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color(0xFF4E7CFF).copy(alpha = 0.2f)
                else Color(0xFF1A2942)
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFF4E7CFF) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onIconSelected(icon.id) }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon.emoji,
            fontSize = 28.sp
        )
        Text(
            text = icon.label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 1
        )
    }
}
