package com.cil.shift.feature.habits.presentation.create.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.localization.LocalizationHelpers
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.localization.StringResources
import com.cil.shift.feature.onboarding.presentation.suggestions.HabitSuggestion
import com.cil.shift.feature.onboarding.presentation.suggestions.SuggestionCategory
import com.cil.shift.feature.onboarding.presentation.suggestions.habitSuggestions
import org.koin.compose.koinInject

@Composable
fun Step1NameAndIcon(
    name: String,
    selectedIcon: String,
    onNameChange: (String) -> Unit,
    onIconSelect: (String) -> Unit,
    onSuggestionSelect: (HabitSuggestion) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()

    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val accentColor = Color(0xFF4E7CFF)

    // Selected category for filtering suggestions
    var selectedCategory by remember { mutableStateOf<SuggestionCategory?>(null) }

    // Filtered suggestions based on selected category
    val filteredSuggestions = remember(selectedCategory) {
        if (selectedCategory == null) {
            habitSuggestions.take(12) // Show first 12 if no category selected
        } else {
            habitSuggestions.filter { it.category == selectedCategory }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = StringResources.nameYourHabit.get(currentLanguage),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        // Quick Suggestions Section
        QuickSuggestions(
            selectedCategory = selectedCategory,
            suggestions = filteredSuggestions,
            onCategorySelected = { category ->
                selectedCategory = if (selectedCategory == category) null else category
            },
            onSuggestionSelected = onSuggestionSelect,
            cardColor = cardColor,
            borderColor = borderColor,
            textColor = textColor,
            currentLanguage = currentLanguage
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
                text = StringResources.habitName.get(currentLanguage),
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
                        text = StringResources.habitNamePlaceholder.get(currentLanguage),
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
                text = "💡 ${StringResources.beSpecific.get(currentLanguage)}",
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
            Text(
                text = StringResources.chooseAnIcon.get(currentLanguage),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.6f),
                letterSpacing = 0.5.sp
            )

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(borderColor)
            )

            // Use fixed rows instead of LazyVerticalGrid for scroll compatibility
            val iconRows = availableIcons.chunked(6)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                iconRows.forEach { rowIcons ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowIcons.forEach { icon ->
                            IconItem(
                                icon = icon.emoji,
                                isSelected = selectedIcon == icon.id,
                                onClick = { onIconSelect(icon.id) },
                                cardColor = MaterialTheme.colorScheme.background,
                                accentColor = accentColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill remaining spaces if row is not complete
                        repeat(6 - rowIcons.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Bottom spacer for scroll padding
        Spacer(modifier = Modifier.height(16.dp))
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
    // Health & Fitness
    IconData("water", "💧"),
    IconData("run", "🏃"),
    IconData("dumbbell", "🏋️"),
    IconData("heart", "❤️"),
    IconData("apple", "🍎"),
    IconData("salad", "🥗"),
    IconData("bike", "🚴"),
    IconData("swim", "🏊"),

    // Mindfulness & Wellness
    IconData("meditation", "🧘"),
    IconData("moon", "🌙"),
    IconData("sun", "☀️"),
    IconData("sparkles", "✨"),
    IconData("brain", "🧠"),
    IconData("lotus", "🪷"),
    IconData("pray", "🙏"),
    IconData("sleep", "😴"),

    // Productivity & Work
    IconData("code", "💻"),
    IconData("book", "📚"),
    IconData("briefcase", "💼"),
    IconData("pencil", "✏️"),
    IconData("target", "🎯"),
    IconData("clock", "⏰"),
    IconData("calendar", "📅"),
    IconData("chart", "📈"),

    // Food & Drinks
    IconData("coffee", "☕"),
    IconData("utensils", "🍴"),
    IconData("pizza", "🍕"),
    IconData("burger", "🍔"),
    IconData("candy", "🍬"),
    IconData("soda", "🥤"),
    IconData("beer", "🍺"),
    IconData("wine", "🍷"),

    // Lifestyle & Home
    IconData("home", "🏠"),
    IconData("bed", "🛏️"),
    IconData("clean", "🧹"),
    IconData("laundry", "🧺"),
    IconData("plant", "🌱"),
    IconData("leaf", "🍃"),
    IconData("flower", "🌸"),
    IconData("dog", "🐕"),

    // Entertainment & Hobbies
    IconData("music", "🎵"),
    IconData("palette", "🎨"),
    IconData("camera", "📷"),
    IconData("game", "🎮"),
    IconData("guitar", "🎸"),
    IconData("movie", "🎬"),
    IconData("headphones", "🎧"),
    IconData("mic", "🎤"),

    // Quit & Reduce
    IconData("cigarette", "🚬"),
    IconData("phone", "📱"),
    IconData("tv", "📺"),
    IconData("shopping", "🛍️"),
    IconData("cookie", "🍪"),
    IconData("chocolate", "🍫"),
    IconData("ice", "🍦"),
    IconData("donut", "🍩"),

    // Tools & Misc
    IconData("tools", "🛠"),
    IconData("umbrella", "☂️"),
    IconData("car", "🚗"),
    IconData("plane", "✈️"),
    IconData("star", "⭐"),
    IconData("fire", "🔥"),
    IconData("trophy", "🏆"),
    IconData("check", "✅")
)

@Composable
private fun QuickSuggestions(
    selectedCategory: SuggestionCategory?,
    suggestions: List<HabitSuggestion>,
    onCategorySelected: (SuggestionCategory) -> Unit,
    onSuggestionSelected: (HabitSuggestion) -> Unit,
    cardColor: Color,
    borderColor: Color,
    textColor: Color,
    currentLanguage: com.cil.shift.core.common.localization.Language,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = StringResources.quickSuggestions.get(currentLanguage),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = textColor.copy(alpha = 0.6f),
            letterSpacing = 0.5.sp
        )

        // Category chips - horizontal scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuggestionCategory.entries.forEach { category ->
                CategoryChip(
                    category = category,
                    isSelected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    currentLanguage = currentLanguage
                )
            }
        }

        // Suggestion chips grid - using 2 columns for better text fit
        val suggestionRows = suggestions.chunked(2)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestionRows.forEach { rowSuggestions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowSuggestions.forEach { suggestion ->
                        SuggestionChip(
                            suggestion = suggestion,
                            onClick = { onSuggestionSelected(suggestion) },
                            currentLanguage = currentLanguage,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining spaces if row is not complete
                    repeat(2 - rowSuggestions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: SuggestionCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    currentLanguage: com.cil.shift.core.common.localization.Language,
    modifier: Modifier = Modifier
) {
    val chipColor = try {
        Color(category.colorHex.removePrefix("#").toLong(16) or 0xFF000000)
    } catch (e: Exception) {
        Color(0xFF4E7CFF)
    }

    // Get localized category name
    val localizedCategoryName = when (category) {
        SuggestionCategory.HEALTH -> StringResources.health.get(currentLanguage)
        SuggestionCategory.PRODUCTIVITY -> StringResources.productivity.get(currentLanguage)
        SuggestionCategory.MINDFULNESS -> StringResources.mindfulness.get(currentLanguage)
        SuggestionCategory.LEARNING -> StringResources.learning.get(currentLanguage)
        SuggestionCategory.LIFESTYLE -> StringResources.lifestyle.get(currentLanguage)
        SuggestionCategory.QUIT -> StringResources.quit.get(currentLanguage)
        SuggestionCategory.REDUCE -> StringResources.reduce.get(currentLanguage)
        SuggestionCategory.CHORES -> StringResources.chores.get(currentLanguage)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) chipColor.copy(alpha = 0.25f)
                else chipColor.copy(alpha = 0.15f)
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) chipColor else chipColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.emoji,
                fontSize = 14.sp
            )
            Text(
                text = localizedCategoryName,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) chipColor else chipColor.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun SuggestionChip(
    suggestion: HabitSuggestion,
    onClick: () -> Unit,
    currentLanguage: com.cil.shift.core.common.localization.Language,
    modifier: Modifier = Modifier
) {
    val chipColor = try {
        Color(suggestion.color.removePrefix("#").toLong(16) or 0xFF000000)
    } catch (e: Exception) {
        Color(0xFF4E7CFF)
    }

    // Get localized habit name
    val localizedName = LocalizationHelpers.getLocalizedHabitName(suggestion.name, currentLanguage)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(chipColor.copy(alpha = 0.18f))
            .border(
                width = 1.dp,
                color = chipColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = localizedName,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = chipColor.copy(alpha = 0.9f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
