package com.cil.shift.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.currentDate
import com.cil.shift.core.common.currentTimestamp
import com.cil.shift.core.common.localization.Language
import com.cil.shift.core.common.localization.LocalizationHelpers
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.localization.StringResources
import com.cil.shift.core.common.localization.localized
import com.cil.shift.core.common.notification.NotificationHistoryRepository
import com.cil.shift.core.common.notification.NotificationManager
import com.cil.shift.core.designsystem.components.CoachMarkController
import com.cil.shift.core.designsystem.components.coachMarkTarget
import com.cil.shift.feature.habits.domain.model.Habit
import com.cil.shift.feature.habits.domain.model.HabitSchedule
import com.cil.shift.feature.habits.domain.model.RepeatType
import com.cil.shift.feature.habits.domain.repository.HabitRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.koin.compose.koinInject

// View mode enum
enum class ScheduleViewMode {
    DAY_1,
    DAY_3,
    WEEK,
    MONTH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToCreateHabit: (LocalDate) -> Unit,
    coachMarkController: CoachMarkController? = null,
    modifier: Modifier = Modifier
) {
    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()
    val habitRepository = koinInject<HabitRepository>()
    val notificationManager = koinInject<NotificationManager>()
    val notificationHistoryRepository = koinInject<NotificationHistoryRepository>()
    val onboardingPreferences = koinInject<com.cil.shift.core.common.onboarding.OnboardingPreferences>()
    val scope = rememberCoroutineScope()

    val today = currentDate()

    // Load saved view mode from preferences
    var viewMode by remember {
        val savedMode = onboardingPreferences.getCalendarViewMode()
        mutableStateOf(
            try {
                ScheduleViewMode.valueOf(savedMode)
            } catch (e: Exception) {
                ScheduleViewMode.WEEK
            }
        )
    }

    // Set initial date based on view mode: today for day views, week start for week/month views
    var selectedWeekStart by remember(viewMode) {
        val initialDate = when (viewMode) {
            ScheduleViewMode.DAY_1, ScheduleViewMode.DAY_3 -> today
            ScheduleViewMode.WEEK, ScheduleViewMode.MONTH -> today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        }
        mutableStateOf(initialDate)
    }
    var showViewModeMenu by remember { mutableStateOf(false) }
    var showEventDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedHour by remember { mutableStateOf(12) }

    // Grid settings dialog
    var showGridSettingsDialog by remember { mutableStateOf(false) }

    // Load grid dimensions from preferences
    var weekViewColumnWidth by remember { mutableStateOf(onboardingPreferences.getWeekViewColumnWidth()) }
    var weekViewRowHeight by remember { mutableStateOf(onboardingPreferences.getWeekViewRowHeight()) }
    var monthViewColumnWidth by remember { mutableStateOf(onboardingPreferences.getMonthViewColumnWidth()) }
    var monthViewRowHeight by remember { mutableStateOf(onboardingPreferences.getMonthViewRowHeight()) }
    var dayViewColumnWidth by remember { mutableStateOf(onboardingPreferences.getDayViewColumnWidth()) }
    var dayViewRowHeight by remember { mutableStateOf(onboardingPreferences.getDayViewRowHeight()) }
    var day3ViewColumnWidth by remember { mutableStateOf(onboardingPreferences.getDay3ViewColumnWidth()) }
    var day3ViewRowHeight by remember { mutableStateOf(onboardingPreferences.getDay3ViewRowHeight()) }

    // Calculate dimensions based on view mode
    val currentColumnWidth = when (viewMode) {
        ScheduleViewMode.DAY_1 -> dayViewColumnWidth.dp
        ScheduleViewMode.DAY_3 -> day3ViewColumnWidth.dp
        ScheduleViewMode.WEEK -> weekViewColumnWidth.dp
        ScheduleViewMode.MONTH -> monthViewColumnWidth.dp
    }
    val currentRowHeight = when (viewMode) {
        ScheduleViewMode.DAY_1 -> dayViewRowHeight.dp
        ScheduleViewMode.DAY_3 -> day3ViewRowHeight.dp
        ScheduleViewMode.WEEK -> weekViewRowHeight.dp
        ScheduleViewMode.MONTH -> monthViewRowHeight.dp
    }

    // Dynamic day cell sizes based on current view mode settings
    val scaledDayCellWidth = (currentColumnWidth.value * 0.75f).dp
    val scaledDayCellHeight = (currentRowHeight.value * 0.5f).dp

    var allHabits by remember { mutableStateOf<List<Habit>>(emptyList()) }
    var schedules by remember { mutableStateOf<List<HabitSchedule>>(emptyList()) }

    // Selection mode for deleting
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedScheduleIds by remember { mutableStateOf(setOf<String>()) }

    // Tooltip for schedule info
    var tooltipSchedule by remember { mutableStateOf<HabitSchedule?>(null) }

    // Load habits
    LaunchedEffect(Unit) {
        habitRepository.getHabits().collect { habits ->
            allHabits = habits
        }
    }

    // Load schedules based on view mode
    LaunchedEffect(selectedWeekStart, viewMode) {
        val endDate = when (viewMode) {
            ScheduleViewMode.DAY_1 -> selectedWeekStart
            ScheduleViewMode.DAY_3 -> selectedWeekStart.plus(2, DateTimeUnit.DAY)
            ScheduleViewMode.WEEK -> selectedWeekStart.plus(6, DateTimeUnit.DAY)
            ScheduleViewMode.MONTH -> {
                // Load entire month plus buffer for scrolling
                val monthStart = LocalDate(selectedWeekStart.year, selectedWeekStart.monthNumber, 1)
                val daysInMonth = when (selectedWeekStart.monthNumber) {
                    1, 3, 5, 7, 8, 10, 12 -> 31
                    4, 6, 9, 11 -> 30
                    2 -> if (selectedWeekStart.year % 4 == 0) 29 else 28
                    else -> 31
                }
                monthStart.plus(daysInMonth - 1, DateTimeUnit.DAY)
            }
        }
        val startStr = formatDate(selectedWeekStart)
        val endStr = formatDate(endDate)
        habitRepository.getSchedulesForDateRange(startStr, endStr).collect {
            schedules = it
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    val gridLineColor = textColor.copy(alpha = 0.06f) // Lighter grid lines (Google Calendar style)
    val accentColor = Color(0xFF4E7CFF) // App theme blue
    val weekendColor = Color(0xFFB8A9C9).copy(alpha = 0.15f) // Soft lavender for weekends

    // Calculate visible days based on view mode
    val visibleDays = when (viewMode) {
        ScheduleViewMode.DAY_1 -> 1
        ScheduleViewMode.DAY_3 -> 3
        ScheduleViewMode.WEEK -> 7
        ScheduleViewMode.MONTH -> 31 // Show full month with scroll
    }

    val daysToShow = if (viewMode == ScheduleViewMode.MONTH) {
        // For month view, show from first day of month
        val firstOfMonth = LocalDate(selectedWeekStart.year, selectedWeekStart.month, 1)
        val daysInMonth = when (selectedWeekStart.month) {
            Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY, Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
            Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
            Month.FEBRUARY -> if (selectedWeekStart.year % 4 == 0 && (selectedWeekStart.year % 100 != 0 || selectedWeekStart.year % 400 == 0)) 29 else 28
            else -> 30
        }
        (0 until daysInMonth).map { offset ->
            firstOfMonth.plus(offset, DateTimeUnit.DAY)
        }
    } else {
        (0 until visibleDays).map { offset ->
            selectedWeekStart.plus(offset, DateTimeUnit.DAY)
        }
    }

    // For month view, use horizontal scroll
    val isMonthView = viewMode == ScheduleViewMode.MONTH
    val horizontalScrollState = rememberScrollState()
    val density = LocalDensity.current

    // Function to scroll to center today's column
    fun scrollToTodayCenter(todayDate: LocalDate, days: List<LocalDate>) {
        val todayIndex = days.indexOfFirst { it == todayDate }
        if (todayIndex >= 0 && viewMode != ScheduleViewMode.DAY_1) {
            val columnWidthPx = with(density) { currentColumnWidth.toPx() }
            val timeColumnPx = with(density) { 50.dp.toPx() }

            // Calculate scroll position to center today
            // Position of today's column start
            val todayStartPx = timeColumnPx + (todayIndex * columnWidthPx)
            // We want today's center to be at viewport center
            val viewportWidth = horizontalScrollState.viewportSize
            val centerOffset = if (viewportWidth > 0) {
                (todayStartPx + columnWidthPx / 2 - viewportWidth / 2).toInt().coerceAtLeast(0)
            } else {
                (todayStartPx - columnWidthPx).toInt().coerceAtLeast(0)
            }

            scope.launch {
                horizontalScrollState.animateScrollTo(centerOffset, animationSpec = tween(durationMillis = 400))
            }
        }
    }

    // Auto-scroll to center today on initial load (for week/month views)
    LaunchedEffect(viewMode) {
        if (viewMode != ScheduleViewMode.DAY_1) {
            // Small delay to ensure layout is ready
            kotlinx.coroutines.delay(100)
            scrollToTodayCenter(today, daysToShow)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    // Selection mode top bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                isSelectionMode = false
                                selectedScheduleIds = emptySet()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = textColor
                            )
                        }
                        Text(
                            text = "${selectedScheduleIds.size} seçildi",
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                    }
                } else {
                    // View Mode Toggle + Grid Settings buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.let { mod ->
                                coachMarkController?.let {
                                    mod.coachMarkTarget(it, CalendarTutorialTargets.VIEW_MODE_BUTTON)
                                } ?: mod
                            }
                        ) {
                            IconButton(
                                onClick = { showViewModeMenu = true },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(accentColor.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewWeek,
                                    contentDescription = "View Mode",
                                    tint = accentColor
                                )
                            }

                            DropdownMenu(
                                expanded = showViewModeMenu,
                                onDismissRequest = { showViewModeMenu = false },
                                containerColor = cardColor
                            ) {
                                ViewModeMenuItem(StringResources.viewDay1.localized(), Icons.Default.ViewDay, viewMode == ScheduleViewMode.DAY_1) {
                                    viewMode = ScheduleViewMode.DAY_1
                                    selectedWeekStart = today
                                    onboardingPreferences.setCalendarViewMode(ScheduleViewMode.DAY_1.name)
                                    showViewModeMenu = false
                                }
                                ViewModeMenuItem(StringResources.viewDay3.localized(), Icons.Default.ViewWeek, viewMode == ScheduleViewMode.DAY_3) {
                                    viewMode = ScheduleViewMode.DAY_3
                                    selectedWeekStart = today
                                    onboardingPreferences.setCalendarViewMode(ScheduleViewMode.DAY_3.name)
                                    showViewModeMenu = false
                                }
                                ViewModeMenuItem(StringResources.viewWeek.localized(), Icons.Default.ViewWeek, viewMode == ScheduleViewMode.WEEK) {
                                    viewMode = ScheduleViewMode.WEEK
                                    selectedWeekStart = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
                                    onboardingPreferences.setCalendarViewMode(ScheduleViewMode.WEEK.name)
                                    showViewModeMenu = false
                                }
                                ViewModeMenuItem(StringResources.viewMonth.localized(), Icons.Default.CalendarMonth, viewMode == ScheduleViewMode.MONTH) {
                                    viewMode = ScheduleViewMode.MONTH
                                    selectedWeekStart = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
                                    onboardingPreferences.setCalendarViewMode(ScheduleViewMode.MONTH.name)
                                    showViewModeMenu = false
                                }
                            }
                        }

                        // Grid settings button
                        IconButton(
                            onClick = { showGridSettingsDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentColor.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Grid Settings",
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (isSelectionMode) "" else StringResources.schedule.localized(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                if (isSelectionMode) {
                    // Delete button
                    IconButton(
                        onClick = {
                            scope.launch {
                                selectedScheduleIds.forEach { id ->
                                    habitRepository.deleteSchedule(id)
                                }
                                isSelectionMode = false
                                selectedScheduleIds = emptySet()
                            }
                        },
                        enabled = selectedScheduleIds.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = if (selectedScheduleIds.isNotEmpty()) Color.Red else textColor.copy(alpha = 0.3f)
                        )
                    }
                } else {
                    // Empty spacer for layout balance
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Date Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .let { mod ->
                        coachMarkController?.let {
                            mod.coachMarkTarget(it, CalendarTutorialTargets.DATE_NAVIGATION)
                        } ?: mod
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Month/Year Display
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(textColor.copy(alpha = 0.05f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    val monthYearText = getMonthYearText(selectedWeekStart, daysToShow.last(), currentLanguage)
                    Text(
                        text = monthYearText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Navigation Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.1f))
                            .clickable {
                                selectedWeekStart = selectedWeekStart.minus(visibleDays, DateTimeUnit.DAY)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Button(
                        onClick = {
                            // Always use fresh current date to handle app being open across midnight
                            val currentToday = currentDate()

                            // Calculate new days list based on view mode
                            val newWeekStart = when (viewMode) {
                                ScheduleViewMode.DAY_1, ScheduleViewMode.DAY_3 -> currentToday
                                ScheduleViewMode.WEEK, ScheduleViewMode.MONTH -> currentToday.minus(currentToday.dayOfWeek.ordinal, DateTimeUnit.DAY)
                            }
                            selectedWeekStart = newWeekStart

                            // Calculate the days list that will be shown after state update
                            val newDays = if (viewMode == ScheduleViewMode.MONTH) {
                                val firstOfMonth = LocalDate(newWeekStart.year, newWeekStart.month, 1)
                                val daysInMonth = when (newWeekStart.month) {
                                    Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY, Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
                                    Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
                                    Month.FEBRUARY -> if (newWeekStart.year % 4 == 0 && (newWeekStart.year % 100 != 0 || newWeekStart.year % 400 == 0)) 29 else 28
                                    else -> 30
                                }
                                (0 until daysInMonth).map { offset -> firstOfMonth.plus(offset, DateTimeUnit.DAY) }
                            } else {
                                val days = when (viewMode) {
                                    ScheduleViewMode.DAY_1 -> 1
                                    ScheduleViewMode.DAY_3 -> 3
                                    ScheduleViewMode.WEEK -> 7
                                    else -> 7
                                }
                                (0 until days).map { offset -> newWeekStart.plus(offset, DateTimeUnit.DAY) }
                            }

                            // Scroll to center today with animation
                            scrollToTodayCenter(currentToday, newDays)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = StringResources.today.localized(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.1f))
                            .clickable {
                                selectedWeekStart = selectedWeekStart.plus(visibleDays, DateTimeUnit.DAY)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Divider
            HorizontalDivider(
                color = gridLineColor,
                thickness = 1.dp
            )

            // Week Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (viewMode != ScheduleViewMode.DAY_1)
                            Modifier.horizontalScroll(horizontalScrollState)
                        else
                            Modifier
                    )
            ) {
                // Time column spacer
                Spacer(modifier = Modifier.width(50.dp))

                // Only DAY_1 uses weight to fill available space
                val useWeight = viewMode == ScheduleViewMode.DAY_1

                daysToShow.forEachIndexed { index, date ->
                    val isToday = date == today
                    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

                    Box(
                        modifier = Modifier
                            .then(
                                if (useWeight) Modifier.weight(1f)
                                else Modifier.width(currentColumnWidth)
                            )
                            .background(if (isWeekend) weekendColor else Color.Transparent)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = getDayAbbreviation(date.dayOfWeek, currentLanguage),
                                fontSize = if (viewMode == ScheduleViewMode.DAY_1) 16.sp
                                          else if (isMonthView) 11.sp
                                          else 14.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) textColor else textColor.copy(alpha = 0.6f)
                            )
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .then(
                                        if (viewMode == ScheduleViewMode.DAY_1)
                                            Modifier.size(48.dp)
                                        else
                                            Modifier.size(width = scaledDayCellWidth, height = scaledDayCellHeight)
                                    )
                                    .clip(RoundedCornerShape(if (viewMode == ScheduleViewMode.DAY_1) 12.dp else 10.dp))
                                    .background(
                                        if (isToday) accentColor
                                        else Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    fontSize = if (viewMode == ScheduleViewMode.DAY_1) 22.sp
                                              else if (isMonthView) 13.sp
                                              else 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToday) Color.White else textColor
                                )
                            }
                        }
                    }
                }
            }

            // Divider
            HorizontalDivider(
                color = gridLineColor,
                thickness = 1.dp
            )

            // Time Grid
            val scrollState = rememberLazyListState()

            // Get current time for indicator
            val currentDateTime = kotlinx.datetime.Clock.System.now()
                .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
            val currentHour = currentDateTime.hour
            val currentMinute = currentDateTime.minute

            // Scroll to current time on first load
            LaunchedEffect(Unit) {
                scrollState.scrollToItem((currentHour - 2).coerceAtLeast(0))
            }

            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .let { mod ->
                        coachMarkController?.let {
                            mod.coachMarkTarget(it, CalendarTutorialTargets.CALENDAR_GRID)
                        } ?: mod
                    }
            ) {
                items(24) { hour ->
                    val isCurrentHourRow = hour == currentHour

                    Box(modifier = Modifier.fillMaxWidth()) {
                        TimeRow(
                            hour = hour,
                            days = daysToShow,
                            today = today,
                            schedules = schedules,
                            gridLineColor = gridLineColor,
                            textColor = textColor,
                            backgroundColor = backgroundColor,
                            weekendColor = weekendColor,
                            isMonthView = isMonthView,
                            viewMode = viewMode,
                            horizontalScrollState = horizontalScrollState,
                            isSelectionMode = isSelectionMode,
                            selectedScheduleIds = selectedScheduleIds,
                            currentRowHeight = currentRowHeight,
                            currentColumnWidth = currentColumnWidth,
                            onCellClick = { date, clickedHour ->
                                if (!isSelectionMode) {
                                    selectedDate = date
                                    selectedHour = clickedHour
                                    showEventDialog = true
                                }
                            },
                            onScheduleLongPress = { scheduleId ->
                                isSelectionMode = true
                                selectedScheduleIds = selectedScheduleIds + scheduleId
                            },
                            onScheduleClick = { schedule ->
                                if (isSelectionMode) {
                                    selectedScheduleIds = if (schedule.id in selectedScheduleIds) {
                                        selectedScheduleIds - schedule.id
                                    } else {
                                        selectedScheduleIds + schedule.id
                                    }
                                    if (selectedScheduleIds.isEmpty()) {
                                        isSelectionMode = false
                                    }
                                } else {
                                    // Show tooltip when not in selection mode
                                    tooltipSchedule = schedule
                                }
                            }
                        )

                        // Current time indicator line (Google Calendar style)
                        if (isCurrentHourRow) {
                            val lineYOffset = (currentRowHeight.value * currentMinute / 60f).dp
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = lineYOffset),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(modifier = Modifier.width(44.dp))
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEA4335)) // Google red
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(2.dp)
                                        .background(Color(0xFFEA4335))
                                )
                            }
                        }
                    }
                }
            }
        }

    }

    // Event Creation Dialog
    if (showEventDialog && selectedDate != null) {
        EventCreationDialog(
            selectedDate = selectedDate!!,
            selectedHour = selectedHour,
            habits = allHabits,
            currentLanguage = currentLanguage,
            onDismiss = {
                showEventDialog = false
                selectedDate = null
            },
            onSave = { habitId, startTime, endTime, hasReminder, repeatType ->
                scope.launch {
                    val baseDate = selectedDate!!

                    // Calculate dates based on repeat type
                    val datesToCreate = when (repeatType) {
                        RepeatType.NEVER -> listOf(baseDate)
                        RepeatType.DAILY -> (0 until 30).map { baseDate.plus(it, DateTimeUnit.DAY) }
                        RepeatType.WEEKLY -> (0 until 12).map { baseDate.plus(it * 7, DateTimeUnit.DAY) }
                        RepeatType.MONTHLY -> (0 until 12).map {
                            try {
                                LocalDate(baseDate.year + (baseDate.monthNumber + it - 1) / 12,
                                         ((baseDate.monthNumber + it - 1) % 12) + 1,
                                         baseDate.dayOfMonth.coerceAtMost(28))
                            } catch (e: Exception) {
                                baseDate.plus(it * 30, DateTimeUnit.DAY)
                            }
                        }
                    }

                    // Get habit name for notifications
                    val habit = allHabits.find { it.id == habitId }
                    val habitName = habit?.name ?: ""

                    // Create schedule for each date
                    datesToCreate.forEach { date ->
                        val schedule = HabitSchedule(
                            id = "",
                            habitId = habitId,
                            habitName = "",
                            habitIcon = "",
                            habitColor = "",
                            date = formatDate(date),
                            startTime = startTime,
                            endTime = endTime,
                            hasReminder = hasReminder,
                            repeatType = repeatType,
                            notes = null,
                            createdAt = currentTimestamp()
                        )
                        habitRepository.createSchedule(schedule)

                        // Schedule notification if reminder is enabled
                        if (hasReminder && habitName.isNotBlank()) {
                            // Use a unique ID for each scheduled event notification
                            val notificationId = "${habitId}_${formatDate(date)}_${startTime}"
                            notificationManager.scheduleHabitReminder(
                                habitId = notificationId,
                                habitName = habitName,
                                reminderTime = startTime
                            )
                            // Save to notification history (important for iOS)
                            notificationHistoryRepository.saveNotification(
                                habitId = notificationId,
                                habitName = habitName,
                                title = "Time for $habitName!",
                                message = "Scheduled for ${formatDate(date)} at $startTime"
                            )
                        }
                    }

                    showEventDialog = false
                    selectedDate = null
                }
            }
        )
    }

    // Schedule Info Tooltip
    if (tooltipSchedule != null) {
        ScheduleInfoTooltip(
            schedule = tooltipSchedule!!,
            currentLanguage = currentLanguage,
            onDismiss = { tooltipSchedule = null },
            onDelete = {
                scope.launch {
                    val schedule = tooltipSchedule!!
                    // Cancel any notification for this schedule
                    val notificationId = "${schedule.habitId}_${schedule.date}_${schedule.startTime}"
                    notificationManager.cancelHabitReminder(notificationId)

                    habitRepository.deleteSchedule(schedule.id)
                    tooltipSchedule = null
                }
            },
            onMuteToggle = { isMuted ->
                scope.launch {
                    // Update the schedule's hasReminder field
                    val schedule = tooltipSchedule!!
                    val updatedSchedule = schedule.copy(hasReminder = !isMuted)
                    habitRepository.updateSchedule(updatedSchedule)

                    // Handle notification scheduling
                    val notificationId = "${schedule.habitId}_${schedule.date}_${schedule.startTime}"
                    if (isMuted) {
                        // Cancel notification when muting
                        notificationManager.cancelHabitReminder(notificationId)
                    } else {
                        // Schedule notification when unmuting
                        val habit = allHabits.find { it.id == schedule.habitId }
                        if (habit != null) {
                            notificationManager.scheduleHabitReminder(
                                habitId = notificationId,
                                habitName = habit.name,
                                reminderTime = schedule.startTime
                            )
                        }
                    }
                }
            }
        )
    }

    // Grid Settings Dialog
    if (showGridSettingsDialog) {
        GridSettingsDialog(
            weekViewColumnWidth = weekViewColumnWidth,
            weekViewRowHeight = weekViewRowHeight,
            monthViewColumnWidth = monthViewColumnWidth,
            monthViewRowHeight = monthViewRowHeight,
            dayViewColumnWidth = dayViewColumnWidth,
            dayViewRowHeight = dayViewRowHeight,
            day3ViewColumnWidth = day3ViewColumnWidth,
            day3ViewRowHeight = day3ViewRowHeight,
            onWeekViewColumnWidthChange = {
                weekViewColumnWidth = it
                onboardingPreferences.setWeekViewColumnWidth(it)
            },
            onWeekViewRowHeightChange = {
                weekViewRowHeight = it
                onboardingPreferences.setWeekViewRowHeight(it)
            },
            onMonthViewColumnWidthChange = {
                monthViewColumnWidth = it
                onboardingPreferences.setMonthViewColumnWidth(it)
            },
            onMonthViewRowHeightChange = {
                monthViewRowHeight = it
                onboardingPreferences.setMonthViewRowHeight(it)
            },
            onDayViewColumnWidthChange = {
                dayViewColumnWidth = it
                onboardingPreferences.setDayViewColumnWidth(it)
            },
            onDayViewRowHeightChange = {
                dayViewRowHeight = it
                onboardingPreferences.setDayViewRowHeight(it)
            },
            onDay3ViewColumnWidthChange = {
                day3ViewColumnWidth = it
                onboardingPreferences.setDay3ViewColumnWidth(it)
            },
            onDay3ViewRowHeightChange = {
                day3ViewRowHeight = it
                onboardingPreferences.setDay3ViewRowHeight(it)
            },
            onDismiss = { showGridSettingsDialog = false }
        )
    }
}

@Composable
private fun GridSettingsDialog(
    weekViewColumnWidth: Int,
    weekViewRowHeight: Int,
    monthViewColumnWidth: Int,
    monthViewRowHeight: Int,
    dayViewColumnWidth: Int,
    dayViewRowHeight: Int,
    day3ViewColumnWidth: Int,
    day3ViewRowHeight: Int,
    onWeekViewColumnWidthChange: (Int) -> Unit,
    onWeekViewRowHeightChange: (Int) -> Unit,
    onMonthViewColumnWidthChange: (Int) -> Unit,
    onMonthViewRowHeightChange: (Int) -> Unit,
    onDayViewColumnWidthChange: (Int) -> Unit,
    onDayViewRowHeightChange: (Int) -> Unit,
    onDay3ViewColumnWidthChange: (Int) -> Unit,
    onDay3ViewRowHeightChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    val accentColor = MaterialTheme.colorScheme.primary
    val dividerColor = textColor.copy(alpha = 0.08f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = StringResources.gridSettings.localized(),
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 16.sp
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = textColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1-Day View Settings
                GridSettingsSection(
                    title = StringResources.dayViewLabel.localized(),
                    columnWidth = dayViewColumnWidth,
                    rowHeight = dayViewRowHeight,
                    onColumnWidthChange = onDayViewColumnWidthChange,
                    onRowHeightChange = onDayViewRowHeightChange,
                    widthRange = 40..200,
                    heightRange = 50..150,
                    textColor = textColor,
                    accentColor = accentColor
                )

                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                // 3-Day View Settings
                GridSettingsSection(
                    title = StringResources.day3ViewLabel.localized(),
                    columnWidth = day3ViewColumnWidth,
                    rowHeight = day3ViewRowHeight,
                    onColumnWidthChange = onDay3ViewColumnWidthChange,
                    onRowHeightChange = onDay3ViewRowHeightChange,
                    widthRange = 40..180,
                    heightRange = 50..150,
                    textColor = textColor,
                    accentColor = accentColor
                )

                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                // Week View Settings
                GridSettingsSection(
                    title = StringResources.weekViewLabel.localized(),
                    columnWidth = weekViewColumnWidth,
                    rowHeight = weekViewRowHeight,
                    onColumnWidthChange = onWeekViewColumnWidthChange,
                    onRowHeightChange = onWeekViewRowHeightChange,
                    widthRange = 40..150,
                    heightRange = 50..150,
                    textColor = textColor,
                    accentColor = accentColor
                )

                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                // Month View Settings
                GridSettingsSection(
                    title = StringResources.monthViewLabel.localized(),
                    columnWidth = monthViewColumnWidth,
                    rowHeight = monthViewRowHeight,
                    onColumnWidthChange = onMonthViewColumnWidthChange,
                    onRowHeightChange = onMonthViewRowHeightChange,
                    widthRange = 40..150,
                    heightRange = 50..150,
                    textColor = textColor,
                    accentColor = accentColor
                )
            }
        },
        confirmButton = {},
        containerColor = cardColor
    )
}

@Composable
private fun GridSettingsSection(
    title: String,
    columnWidth: Int,
    rowHeight: Int,
    onColumnWidthChange: (Int) -> Unit,
    onRowHeightChange: (Int) -> Unit,
    widthRange: IntRange,
    heightRange: IntRange,
    textColor: Color,
    accentColor: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            fontSize = 13.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Width control
            CompactSliderControl(
                label = StringResources.widthLabel.localized(),
                value = columnWidth,
                onValueChange = onColumnWidthChange,
                valueRange = widthRange,
                textColor = textColor,
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            )
            // Height control
            CompactSliderControl(
                label = StringResources.heightLabel.localized(),
                value = rowHeight,
                onValueChange = onRowHeightChange,
                valueRange = heightRange,
                textColor = textColor,
                accentColor = accentColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompactSliderControl(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange,
    textColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.6f)
            )
            Text(
                text = "${value}dp",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = accentColor
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            modifier = Modifier.height(24.dp),
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = textColor.copy(alpha = 0.15f)
            )
        )
    }
}


@Composable
private fun ScheduleInfoTooltip(
    schedule: HabitSchedule,
    currentLanguage: Language,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onMuteToggle: (Boolean) -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    // Local mute state for this schedule
    var isMuted by remember { mutableStateOf(!schedule.hasReminder) }

    val habitColor = try {
        val colorString = schedule.habitColor.removePrefix("#")
        val colorInt = colorString.toLong(16)
        if (colorString.length == 6) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        Color(0xFF6C63FF)
    }

    // Get emoji from icon ID
    val iconEmoji = getIconEmoji(schedule.habitIcon)
    val localizedHabitName = LocalizationHelpers.getLocalizedHabitName(schedule.habitName, currentLanguage)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(habitColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconEmoji,
                        fontSize = 22.sp
                    )
                }
                Text(
                    text = localizedHabitName,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 18.sp,
                    maxLines = 2
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = habitColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${schedule.startTime} - ${schedule.endTime}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                }

                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = habitColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = schedule.date,
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }

                // Reminder with mute toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (isMuted) textColor.copy(alpha = 0.4f) else habitColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = StringResources.reminder.localized(),
                            fontSize = 14.sp,
                            color = if (isMuted) textColor.copy(alpha = 0.5f) else textColor.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = !isMuted,
                        onCheckedChange = { enabled ->
                            isMuted = !enabled
                            onMuteToggle(!enabled)
                        },
                        modifier = Modifier.height(24.dp),
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = habitColor,
                            checkedThumbColor = Color.White,
                            uncheckedTrackColor = textColor.copy(alpha = 0.2f),
                            uncheckedThumbColor = textColor.copy(alpha = 0.5f)
                        )
                    )
                }

                // Repeat
                if (schedule.repeatType != RepeatType.NEVER) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = habitColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = when (schedule.repeatType) {
                                RepeatType.DAILY -> StringResources.daily.localized()
                                RepeatType.WEEKLY -> StringResources.weekly.localized()
                                RepeatType.MONTHLY -> StringResources.monthly.localized()
                                else -> ""
                            },
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.8f)
                        )
                    }
                }

                // Notes
                val notes = schedule.notes
                if (!notes.isNullOrBlank()) {
                    HorizontalDivider(color = textColor.copy(alpha = 0.1f))
                    Text(
                        text = notes,
                        fontSize = 13.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = StringResources.close.localized(),
                    color = Color(0xFF4E7CFF)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Text(
                    text = StringResources.delete.localized(),
                    color = Color.Red.copy(alpha = 0.8f)
                )
            }
        }
    )
}

// Helper function to convert icon ID to emoji
private fun getIconEmoji(iconId: String): String {
    return when (iconId) {
        // Health & Fitness
        "water" -> "💧"
        "run" -> "🏃"
        "dumbbell" -> "🏋️"
        "heart" -> "❤️"
        "apple" -> "🍎"
        "salad" -> "🥗"
        "bike" -> "🚴"
        "swim" -> "🏊"
        // Mindfulness & Wellness
        "meditation" -> "🧘"
        "moon" -> "🌙"
        "sun" -> "☀️"
        "sparkles" -> "✨"
        "brain" -> "🧠"
        "lotus" -> "🪷"
        "pray" -> "🙏"
        "sleep" -> "😴"
        // Productivity & Work
        "code" -> "💻"
        "book" -> "📚"
        "briefcase" -> "💼"
        "pencil" -> "✏️"
        "target" -> "🎯"
        "clock" -> "⏰"
        "calendar" -> "📅"
        "chart" -> "📈"
        // Food & Drinks
        "coffee" -> "☕"
        "utensils" -> "🍴"
        "pizza" -> "🍕"
        "burger" -> "🍔"
        "candy" -> "🍬"
        "soda" -> "🥤"
        "beer" -> "🍺"
        "wine" -> "🍷"
        // Lifestyle & Home
        "home" -> "🏠"
        "bed" -> "🛏️"
        "clean" -> "🧹"
        "laundry" -> "🧺"
        "plant" -> "🌱"
        "leaf" -> "🍃"
        "flower" -> "🌸"
        "dog" -> "🐕"
        // Entertainment & Hobbies
        "music" -> "🎵"
        "palette" -> "🎨"
        "camera" -> "📷"
        "game" -> "🎮"
        "guitar" -> "🎸"
        "movie" -> "🎬"
        "headphones" -> "🎧"
        "mic" -> "🎤"
        // Quit & Reduce
        "cigarette" -> "🚬"
        "phone" -> "📱"
        "tv" -> "📺"
        "shopping" -> "🛍️"
        "cookie" -> "🍪"
        "chocolate" -> "🍫"
        "ice" -> "🍦"
        "donut" -> "🍩"
        // Tools & Misc
        "tools" -> "🛠"
        "umbrella" -> "☂️"
        "car" -> "🚗"
        "plane" -> "✈️"
        "star" -> "⭐"
        "fire" -> "🔥"
        "trophy" -> "🏆"
        "check" -> "✅"
        // Default
        else -> if (iconId.isNotEmpty() && iconId.length <= 2) iconId else "📅"
    }
}

@Composable
private fun ViewModeMenuItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground

    DropdownMenuItem(
        text = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = text,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = textColor
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        onClick = onClick,
        modifier = Modifier.background(
            if (isSelected) textColor.copy(alpha = 0.1f) else Color.Transparent
        )
    )
}

@Composable
private fun TimeRow(
    hour: Int,
    days: List<LocalDate>,
    today: LocalDate,
    schedules: List<HabitSchedule>,
    gridLineColor: Color,
    textColor: Color,
    backgroundColor: Color,
    weekendColor: Color,
    isMonthView: Boolean,
    viewMode: ScheduleViewMode,
    horizontalScrollState: ScrollState,
    isSelectionMode: Boolean,
    selectedScheduleIds: Set<String>,
    currentRowHeight: Dp,
    currentColumnWidth: Dp,
    onCellClick: (LocalDate, Int) -> Unit,
    onScheduleLongPress: (String) -> Unit,
    onScheduleClick: (HabitSchedule) -> Unit
) {
    val hourString = "${hour.toString().padStart(2, '0')}:00"
    val useWeight = viewMode == ScheduleViewMode.DAY_1
    val rowHeight = currentRowHeight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
    ) {
        // Time Label
        Box(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight()
                .border(
                    width = 0.5.dp,
                    color = gridLineColor
                ),
            contentAlignment = Alignment.TopEnd
        ) {
            Text(
                text = hourString,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.5f),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .offset(y = (-8).dp)
            )
        }

        // Day Cells - with synchronized horizontal scroll (all except DAY_1)
        Row(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (viewMode != ScheduleViewMode.DAY_1)
                        Modifier.horizontalScroll(horizontalScrollState)
                    else
                        Modifier
                )
        ) {
            days.forEach { date ->
                val isToday = date == today
                val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
                val dateStr = formatDate(date)

                // Find schedules that span this hour (not just start at this hour)
                val cellSchedules = schedules.filter { schedule ->
                    if (schedule.date != dateStr) return@filter false

                    val startHour = schedule.startTime.take(2).toIntOrNull() ?: 0
                    val endHour = schedule.endTime.take(2).toIntOrNull() ?: 24
                    val endMinute = schedule.endTime.takeLast(2).toIntOrNull() ?: 0

                    // Adjust end hour if it has minutes (e.g., 23:45 should include hour 23)
                    val effectiveEndHour = if (endMinute > 0) endHour else (endHour - 1).coerceAtLeast(startHour)

                    hour in startHour..effectiveEndHour
                }

                val eventCount = cellSchedules.size

                Box(
                    modifier = Modifier
                        .then(
                            if (useWeight) Modifier.weight(1f)
                            else Modifier.width(currentColumnWidth)
                        )
                        .fillMaxHeight()
                        .background(
                            when {
                                isToday -> Color(0xFF4E7CFF).copy(alpha = 0.08f)
                                isWeekend -> weekendColor
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            width = 0.5.dp,
                            color = gridLineColor
                        )
                        .clickable { onCellClick(date, hour) }
                ) {
                    // Show schedules that span this hour - split horizontally if multiple
                    if (eventCount > 0) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            cellSchedules.forEachIndexed { index, schedule ->
                                val scheduleStartHour = schedule.startTime.take(2).toIntOrNull() ?: 0
                                val scheduleStartMinute = schedule.startTime.takeLast(2).toIntOrNull() ?: 0
                                val scheduleEndHour = schedule.endTime.take(2).toIntOrNull() ?: 24
                                val scheduleEndMinute = schedule.endTime.takeLast(2).toIntOrNull() ?: 0
                                val effectiveEndHour = if (scheduleEndMinute > 0) scheduleEndHour else (scheduleEndHour - 1).coerceAtLeast(scheduleStartHour)

                                val isFirstRow = hour == scheduleStartHour
                                val isLastRow = hour == effectiveEndHour

                                // Calculate proportional offsets (0f to 1f)
                                val topOffset = if (isFirstRow) scheduleStartMinute / 60f else 0f
                                val bottomOffset = if (isLastRow && scheduleEndMinute > 0) (60 - scheduleEndMinute) / 60f else 0f

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    ScheduleItemSpanning(
                                        schedule = schedule,
                                        backgroundColor = backgroundColor,
                                        isMonthView = isMonthView,
                                        isSelected = schedule.id in selectedScheduleIds,
                                        isSelectionMode = isSelectionMode,
                                        isFirstRow = isFirstRow,
                                        isLastRow = isLastRow,
                                        topOffsetFraction = topOffset,
                                        bottomOffsetFraction = bottomOffset,
                                        onLongPress = { onScheduleLongPress(schedule.id) },
                                        onClick = { onScheduleClick(schedule) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleItemSpanning(
    schedule: HabitSchedule,
    backgroundColor: Color,
    isMonthView: Boolean = false,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    isFirstRow: Boolean = true,
    isLastRow: Boolean = false,
    topOffsetFraction: Float = 0f,
    bottomOffsetFraction: Float = 0f,
    onLongPress: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val habitColor = try {
        val colorString = schedule.habitColor.removePrefix("#")
        val colorInt = colorString.toLong(16)
        if (colorString.length == 6) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        Color(0xFF6C63FF)
    }

    // For continuous events, we want the background to fill proportionally
    // - First row: top rounded, offset from top based on start minute
    // - Middle rows: no rounding, fill completely
    // - Last row: bottom rounded, trim from bottom based on end minute
    val shape = when {
        isFirstRow && isLastRow -> RoundedCornerShape(4.dp) // Single row event
        isFirstRow -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
        isLastRow -> RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
        else -> RoundedCornerShape(0.dp)
    }

    // Left bar shape for Google Calendar style
    val leftBarShape = when {
        isFirstRow && isLastRow -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
        isFirstRow -> RoundedCornerShape(topStart = 4.dp)
        isLastRow -> RoundedCornerShape(bottomStart = 4.dp)
        else -> RoundedCornerShape(0.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(1f - topOffsetFraction - bottomOffsetFraction)
                .align(
                    when {
                        topOffsetFraction > 0f && bottomOffsetFraction > 0f -> Alignment.Center
                        topOffsetFraction > 0f -> Alignment.BottomCenter
                        else -> Alignment.TopCenter
                    }
                )
                .clip(shape)
                .background(habitColor.copy(alpha = if (isSelected) 0.3f else 0.15f))
                .then(
                    if (isSelected) Modifier.border(2.dp, habitColor, shape)
                    else Modifier
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongPress() },
                        onTap = { onClick() }
                    )
                }
        ) {
            // Left color bar (Google Calendar style)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(habitColor, shape = leftBarShape)
            )

            // Content area
            if (isFirstRow) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isSelectionMode) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = habitColor
                            )
                        }
                        Text(
                            text = schedule.habitName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = habitColor.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${schedule.startTime} - ${schedule.endTime}",
                        fontSize = 8.sp,
                        color = habitColor.copy(alpha = 0.7f)
                    )
                    if (schedule.hasReminder && !isSelectionMode) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = habitColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleItem(
    schedule: HabitSchedule,
    backgroundColor: Color,
    isMonthView: Boolean = false,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onLongPress: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val habitColor = try {
        val colorString = schedule.habitColor.removePrefix("#")
        val colorInt = colorString.toLong(16)
        if (colorString.length == 6) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        Color(0xFF6C63FF)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(habitColor.copy(alpha = if (isSelected) 1f else 0.8f))
            .then(
                if (isSelected) Modifier.border(2.dp, Color.White, RoundedCornerShape(4.dp))
                else Modifier
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = { onClick() }
                )
            }
            .padding(4.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isSelectionMode) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                }
                Text(
                    text = schedule.habitName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "${schedule.startTime} - ${schedule.endTime}",
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            if (schedule.hasReminder && !isSelectionMode) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventCreationDialog(
    selectedDate: LocalDate,
    selectedHour: Int,
    habits: List<Habit>,
    currentLanguage: Language,
    onDismiss: () -> Unit,
    onSave: (habitId: String, startTime: String, endTime: String, hasReminder: Boolean, repeatType: RepeatType) -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    var selectedHabitId by remember { mutableStateOf<String?>(null) }
    var showHabitDropdown by remember { mutableStateOf(false) }
    var hasReminder by remember { mutableStateOf(true) }
    var repeatType by remember { mutableStateOf(RepeatType.NEVER) }
    var showRepeatDropdown by remember { mutableStateOf(false) }

    // Editable time state
    var startHour by remember(selectedHour) { mutableStateOf(selectedHour) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember(selectedHour) { mutableStateOf((selectedHour + 1) % 24) }
    var endMinute by remember { mutableStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }
    var editingStartTime by remember { mutableStateOf(true) }

    val startTime = "${startHour.toString().padStart(2, '0')}:${startMinute.toString().padStart(2, '0')}"
    val endTime = "${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}"

    val selectedHabit = habits.find { it.id == selectedHabitId }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = textColor
                    )
                }
                Text(
                    text = StringResources.newEvent.localized(),
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                IconButton(
                    onClick = {
                        selectedHabitId?.let { habitId ->
                            onSave(habitId, startTime, endTime, hasReminder, repeatType)
                        }
                    },
                    enabled = selectedHabitId != null
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = if (selectedHabitId != null) Color(0xFF4ECDC4) else textColor.copy(alpha = 0.3f)
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Habit Selector
                Box {
                    OutlinedButton(
                        onClick = { showHabitDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = textColor
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedHabit?.let {
                                    LocalizationHelpers.getLocalizedHabitName(it.name, currentLanguage)
                                } ?: StringResources.selectHabit.localized(),
                                color = if (selectedHabit != null) textColor else textColor.copy(alpha = 0.5f)
                            )
                            Icon(
                                imageVector = if (showHabitDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = textColor.copy(alpha = 0.6f)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showHabitDropdown,
                        onDismissRequest = { showHabitDropdown = false },
                        containerColor = cardColor,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        if (habits.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = StringResources.noHabits.localized(),
                                        color = textColor.copy(alpha = 0.5f)
                                    )
                                },
                                onClick = { showHabitDropdown = false }
                            )
                        } else {
                            habits.forEach { habit ->
                                val habitColor = try {
                                    val colorString = habit.color.removePrefix("#")
                                    val colorInt = colorString.toLong(16)
                                    if (colorString.length == 6) {
                                        Color(0xFF000000 or colorInt)
                                    } else {
                                        Color(colorInt)
                                    }
                                } catch (e: Exception) {
                                    Color(0xFF6C63FF)
                                }

                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(habitColor)
                                            )
                                            Text(
                                                text = LocalizationHelpers.getLocalizedHabitName(habit.name, currentLanguage),
                                                color = textColor
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedHabitId = habit.id
                                        showHabitDropdown = false
                                    },
                                    modifier = Modifier.background(
                                        if (selectedHabitId == habit.id) habitColor.copy(alpha = 0.2f)
                                        else Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = textColor.copy(alpha = 0.1f))

                // Reminder Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = StringResources.reminder.localized(),
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                    }
                    Switch(
                        checked = hasReminder,
                        onCheckedChange = { hasReminder = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color(0xFF4E7CFF),
                            checkedThumbColor = Color.White
                        )
                    )
                }

                HorizontalDivider(color = textColor.copy(alpha = 0.1f))

                // Time Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayDate = formatDateForDisplay(selectedDate, currentLanguage)

                    // Start Column
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Start:",
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                        Text(
                            text = displayDate,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                        Text(
                            text = startTime,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4E7CFF)
                        )
                    }

                    // End Column
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "End:",
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                        Text(
                            text = displayDate,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                        Text(
                            text = endTime,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4E7CFF)
                        )
                    }

                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Time",
                            tint = textColor.copy(alpha = 0.6f)
                        )
                    }
                }

                // Time Picker Dialog
                if (showTimePicker) {
                    TimePickerDialog(
                        startHour = startHour,
                        startMinute = startMinute,
                        endHour = endHour,
                        endMinute = endMinute,
                        onStartTimeChanged = { hour, minute ->
                            startHour = hour
                            startMinute = minute
                            // Auto-adjust end time if it's before start
                            if (endHour < hour || (endHour == hour && endMinute <= minute)) {
                                endHour = (hour + 1) % 24
                                endMinute = minute
                            }
                        },
                        onEndTimeChanged = { hour, minute ->
                            endHour = hour
                            endMinute = minute
                        },
                        onDismiss = { showTimePicker = false }
                    )
                }

                HorizontalDivider(color = textColor.copy(alpha = 0.1f))

                // Repeat Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = StringResources.repeats.localized(),
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )

                    Box {
                        OutlinedButton(
                            onClick = { showRepeatDropdown = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = textColor
                            )
                        ) {
                            Text(
                                text = when (repeatType) {
                                    RepeatType.NEVER -> StringResources.never.localized()
                                    RepeatType.DAILY -> StringResources.daily.localized()
                                    RepeatType.WEEKLY -> StringResources.weekly.localized()
                                    RepeatType.MONTHLY -> StringResources.monthly.localized()
                                }
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showRepeatDropdown,
                            onDismissRequest = { showRepeatDropdown = false },
                            containerColor = cardColor
                        ) {
                            RepeatType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = when (type) {
                                                RepeatType.NEVER -> StringResources.never.localized()
                                                RepeatType.DAILY -> StringResources.daily.localized()
                                                RepeatType.WEEKLY -> StringResources.weekly.localized()
                                                RepeatType.MONTHLY -> StringResources.monthly.localized()
                                            },
                                            color = textColor
                                        )
                                    },
                                    onClick = {
                                        repeatType = type
                                        showRepeatDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

// Helper functions
private fun formatDate(date: LocalDate): String {
    return "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
}

private fun formatDateForDisplay(date: LocalDate, language: Language): String {
    val monthName = LocalizationHelpers.getMonthName(date.monthNumber, language)
    return "${date.dayOfMonth} $monthName"
}

private fun getMonthYearText(startDate: LocalDate, endDate: LocalDate, language: Language): String {
    val startMonth = LocalizationHelpers.getMonthName(startDate.monthNumber, language).take(3)
    val endMonth = LocalizationHelpers.getMonthName(endDate.monthNumber, language).take(3)

    return if (startDate.month == endDate.month) {
        "$startMonth ${startDate.year}"
    } else if (startDate.year == endDate.year) {
        "$startMonth - $endMonth '${startDate.year.toString().takeLast(2)}"
    } else {
        "$startMonth '${startDate.year.toString().takeLast(2)} - $endMonth '${endDate.year.toString().takeLast(2)}"
    }
}

private fun getDayAbbreviation(dayOfWeek: DayOfWeek, language: Language): String {
    // dayOfWeek.ordinal is 0-based (Monday=0), getDayNameShort expects 1-based (Monday=1)
    return LocalizationHelpers.getDayNameShort(dayOfWeek.ordinal + 1, language)
}

@Composable
private fun TimePickerDialog(
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    onStartTimeChanged: (hour: Int, minute: Int) -> Unit,
    onEndTimeChanged: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    var editingStart by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Text(
                text = if (editingStart) "Start Time" else "End Time",
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tab selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabButton(
                        text = "Start",
                        selected = editingStart,
                        onClick = { editingStart = true },
                        modifier = Modifier.weight(1f)
                    )
                    TabButton(
                        text = "End",
                        selected = !editingStart,
                        onClick = { editingStart = false },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Time display and picker
                val currentHour = if (editingStart) startHour else endHour
                val currentMinute = if (editingStart) startMinute else endMinute

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour picker
                    NumberPicker(
                        value = currentHour,
                        range = 0..23,
                        onValueChange = { hour ->
                            if (editingStart) {
                                onStartTimeChanged(hour, currentMinute)
                            } else {
                                onEndTimeChanged(hour, currentMinute)
                            }
                        }
                    )

                    Text(
                        text = ":",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Minute picker (1 minute increments)
                    NumberPicker(
                        value = currentMinute,
                        range = 0..59,
                        step = 1,
                        onValueChange = { minute ->
                            if (editingStart) {
                                onStartTimeChanged(currentHour, minute)
                            } else {
                                onEndTimeChanged(currentHour, minute)
                            }
                        }
                    )
                }

                // Preview
                Text(
                    text = "${startHour.toString().padStart(2, '0')}:${startMinute.toString().padStart(2, '0')} - ${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}",
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = Color(0xFF4ECDC4))
            }
        }
    )
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) Color(0xFF4E7CFF) else Color.Transparent
    val textColor = if (selected) Color.White else MaterialTheme.colorScheme.onBackground

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        onClick = onClick
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun NumberPicker(
    value: Int,
    range: IntRange,
    step: Int = 1,
    onValueChange: (Int) -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val validValues = range.filter { it % step == 0 || it == value }
    var showInputDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Up button
        IconButton(
            onClick = {
                val newValue = if (value + step > range.last) range.first else value + step
                onValueChange(newValue)
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase",
                tint = textColor.copy(alpha = 0.6f)
            )
        }

        // Value - clickable for manual entry
        Text(
            text = value.toString().padStart(2, '0'),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { showInputDialog = true }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        // Down button
        IconButton(
            onClick = {
                val newValue = if (value - step < range.first) range.last - (range.last % step) else value - step
                onValueChange(newValue)
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease",
                tint = textColor.copy(alpha = 0.6f)
            )
        }
    }

    // Manual input dialog
    if (showInputDialog) {
        NumberInputDialog(
            currentValue = value,
            range = range,
            onValueConfirmed = { newValue ->
                onValueChange(newValue)
                showInputDialog = false
            },
            onDismiss = { showInputDialog = false }
        )
    }
}

@Composable
private fun NumberInputDialog(
    currentValue: Int,
    range: IntRange,
    onValueConfirmed: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf(currentValue.toString()) }
    val cardColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Text(
                text = "Enter Value",
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { newValue ->
                        // Only allow digits
                        if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                            inputText = newValue
                        }
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = textColor
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4E7CFF),
                        unfocusedBorderColor = textColor.copy(alpha = 0.3f),
                        cursorColor = Color(0xFF4E7CFF)
                    )
                )
                Text(
                    text = "Range: ${range.first} - ${range.last}",
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.5f)
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = textColor.copy(alpha = 0.7f))
                }
                TextButton(
                    onClick = {
                        val parsedValue = inputText.toIntOrNull()
                        if (parsedValue != null && parsedValue in range) {
                            onValueConfirmed(parsedValue)
                        } else {
                            // If invalid, keep current value
                            onDismiss()
                        }
                    }
                ) {
                    Text("OK", color = Color(0xFF4E7CFF))
                }
            }
        }
    )
}
