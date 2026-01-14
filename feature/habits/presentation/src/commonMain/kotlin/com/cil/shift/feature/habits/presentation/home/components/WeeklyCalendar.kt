package com.cil.shift.feature.habits.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.localization.Language
import com.cil.shift.core.common.localization.LocalizationHelpers
import com.cil.shift.core.common.localization.StringResources
import com.cil.shift.core.common.localization.localized
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()

    // Generate 61 days: 30 before today, today, 30 after today
    val days = (-30..30).map { offset ->
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
    val todayIndex = 30 // Today is at index 30 (0-based, after 30 past days)
    val density = LocalDensity.current

    // Calculate item dimensions (item width: 48.dp, spacing: 12.dp)
    val itemWidthPx = with(density) { 48.dp.toPx() }
    val itemSpacingPx = with(density) { 12.dp.toPx() }
    val itemTotalWidthPx = itemWidthPx + itemSpacingPx
    val contentPaddingPx = with(density) { 20.dp.toPx() } // horizontal padding

    // Check if today is visible in the current viewport
    val isTodayVisible by remember {
        derivedStateOf {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            visibleItems.any { it.index == todayIndex }
        }
    }

    // Calculate center offset based on viewport width
    fun calculateCenterOffset(): Int {
        val viewportWidth = listState.layoutInfo.viewportSize.width
        if (viewportWidth == 0) return 0
        // Offset to center the item: -(viewportWidth/2 - itemWidth/2)
        return -((viewportWidth / 2) - (itemWidthPx / 2)).toInt()
    }

    // Function to smoothly scroll to today with custom animation duration
    suspend fun animateScrollToToday() {
        val viewportWidth = listState.layoutInfo.viewportSize.width
        if (viewportWidth == 0) {
            // Fallback if layout not ready
            listState.animateScrollToItem(todayIndex, calculateCenterOffset())
            return
        }

        // Calculate target scroll position to center today
        val targetItemStart = contentPaddingPx + (todayIndex * itemTotalWidthPx)
        val targetCenterPosition = targetItemStart + (itemWidthPx / 2) - (viewportWidth / 2)

        // Calculate current scroll position
        val currentPosition = listState.firstVisibleItemIndex * itemTotalWidthPx +
            listState.firstVisibleItemScrollOffset + contentPaddingPx

        // Calculate how much we need to scroll
        val scrollDistance = targetCenterPosition - currentPosition

        // Animate scroll with custom duration (400ms - slightly slower)
        listState.animateScrollBy(
            value = scrollDistance,
            animationSpec = tween(durationMillis = 400)
        )
    }

    // Scroll to today on first composition, centering it in view
    LaunchedEffect(Unit) {
        // Wait for layout to be ready
        listState.scrollToItem(todayIndex, calculateCenterOffset())
    }

    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    Box(modifier = modifier) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
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

        // "Today" button - appears when today is not visible
        AnimatedVisibility(
            visible = !isTodayVisible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 24.dp, top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF4E7CFF))
                    .clickable {
                        scope.launch {
                            animateScrollToToday()
                        }
                        onDaySelected(today)
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = StringResources.today.localized(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}
