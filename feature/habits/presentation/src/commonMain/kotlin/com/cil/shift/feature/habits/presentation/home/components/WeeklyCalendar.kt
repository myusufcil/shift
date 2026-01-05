package com.cil.shift.feature.habits.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.localization.Language
import com.cil.shift.core.common.localization.LocalizationHelpers
import kotlinx.datetime.*

data class CalendarDay(
    val dayOfWeek: String,
    val dayOfMonth: Int,
    val date: LocalDate,
    val isToday: Boolean,
    val isPast: Boolean,
    val isFuture: Boolean,
    val isSelected: Boolean = false
)

@Composable
fun WeeklyCalendar(
    currentDayOfWeek: Int, // Not used anymore, calculated from actual date
    selectedDate: LocalDate? = null,
    onDaySelected: (LocalDate) -> Unit = {},
    currentLanguage: Language,
    modifier: Modifier = Modifier
) {
    val today = com.cil.shift.core.common.currentDate()

    // Generate 10 days: 6 before today, today, 3 after today
    val days = (-6..3).map { offset ->
        val date = today.plus(offset, DateTimeUnit.DAY)

        // Get localized 3-letter day name
        val dayOfWeekNumber = date.dayOfWeek.isoDayNumber // 1=Monday, 7=Sunday
        val dayOfWeekShort = LocalizationHelpers.getDayNameShort(dayOfWeekNumber, currentLanguage)
            .take(3) // Take first 3 characters for short form

        CalendarDay(
            dayOfWeek = dayOfWeekShort,
            dayOfMonth = date.dayOfMonth,
            date = date,
            isToday = offset == 0,
            isPast = offset < 0,
            isFuture = offset > 0,
            isSelected = selectedDate == date
        )
    }

    val listState = rememberLazyListState()

    // Scroll to today (index 6) on first composition to center it
    LaunchedEffect(Unit) {
        listState.scrollToItem(6)
    }

    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(days) { day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(48.dp)
            ) {
                Text(
                    text = day.dayOfWeek,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = textColor.copy(alpha = 0.5f)
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                day.isSelected -> Color(0xFF00D9FF) // Cyan for selected
                                day.isToday -> Color(0xFF4E7CFF)    // Blue for today
                                else -> cardColor                    // Theme card color for others
                            }
                        )
                        .clickable { onDaySelected(day.date) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.dayOfMonth.toString(),
                        fontSize = 14.sp,
                        fontWeight = if (day.isToday || day.isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (day.isToday || day.isSelected) Color.White else textColor
                    )
                }
            }
        }
    }
}
