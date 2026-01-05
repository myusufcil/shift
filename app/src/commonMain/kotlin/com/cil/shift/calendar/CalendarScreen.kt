package com.cil.shift.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
    modifier: Modifier = Modifier
) {
    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()
    val habitRepository = koinInject<HabitRepository>()
    val scope = rememberCoroutineScope()

    val today = currentDate()
    var selectedWeekStart by remember {
        mutableStateOf(today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY))
    }
    var viewMode by remember { mutableStateOf(ScheduleViewMode.WEEK) }
    var showViewModeMenu by remember { mutableStateOf(false) }
    var showEventDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedHour by remember { mutableStateOf(12) }

    var allHabits by remember { mutableStateOf<List<Habit>>(emptyList()) }
    var schedules by remember { mutableStateOf<List<HabitSchedule>>(emptyList()) }

    // Selection mode for deleting
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedScheduleIds by remember { mutableStateOf(setOf<String>()) }

    // Load habits
    LaunchedEffect(Unit) {
        habitRepository.getHabits().collect { habits ->
            allHabits = habits
        }
    }

    // Load schedules for current week
    LaunchedEffect(selectedWeekStart) {
        val endDate = selectedWeekStart.plus(6, DateTimeUnit.DAY)
        val startStr = formatDate(selectedWeekStart)
        val endStr = formatDate(endDate)
        habitRepository.getSchedulesForDateRange(startStr, endStr).collect {
            schedules = it
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    val gridLineColor = textColor.copy(alpha = 0.1f)
    val accentColor = Color(0xFF4E7CFF) // App theme blue

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
                    // View Mode Toggle
                    Box {
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
                        ViewModeMenuItem("1-Day", Icons.Default.ViewDay, viewMode == ScheduleViewMode.DAY_1) {
                            viewMode = ScheduleViewMode.DAY_1
                            showViewModeMenu = false
                        }
                        ViewModeMenuItem("3-Day", Icons.Default.ViewWeek, viewMode == ScheduleViewMode.DAY_3) {
                            viewMode = ScheduleViewMode.DAY_3
                            showViewModeMenu = false
                        }
                        ViewModeMenuItem("Week", Icons.Default.ViewWeek, viewMode == ScheduleViewMode.WEEK) {
                            viewMode = ScheduleViewMode.WEEK
                            showViewModeMenu = false
                        }
                        ViewModeMenuItem("Month", Icons.Default.CalendarMonth, viewMode == ScheduleViewMode.MONTH) {
                            viewMode = ScheduleViewMode.MONTH
                            showViewModeMenu = false
                        }
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
                    IconButton(onClick = { /* Toggle list view */ }) {
                        Icon(
                            imageVector = Icons.Default.ViewList,
                            contentDescription = "List View",
                            tint = accentColor
                        )
                    }
                }
            }

            // Date Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            selectedWeekStart = selectedWeekStart.minus(visibleDays, DateTimeUnit.DAY)
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous",
                            tint = accentColor
                        )
                    }

                    Button(
                        onClick = {
                            selectedWeekStart = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = StringResources.today.localized(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = {
                            selectedWeekStart = selectedWeekStart.plus(visibleDays, DateTimeUnit.DAY)
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next",
                            tint = accentColor
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
                        if (isMonthView || viewMode == ScheduleViewMode.WEEK)
                            Modifier.horizontalScroll(horizontalScrollState)
                        else
                            Modifier
                    )
            ) {
                // Time column spacer
                Spacer(modifier = Modifier.width(50.dp))

                // For DAY_1 and DAY_3, use weight to fill available space
                val useWeight = viewMode == ScheduleViewMode.DAY_1 || viewMode == ScheduleViewMode.DAY_3

                daysToShow.forEachIndexed { index, date ->
                    val isToday = date == today
                    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

                    Box(
                        modifier = Modifier
                            .then(
                                if (useWeight) Modifier.weight(1f)
                                else Modifier.width(if (isMonthView) 48.dp else 60.dp)
                            )
                            .background(if (isWeekend) textColor.copy(alpha = 0.04f) else Color.Transparent)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = getDayAbbreviation(date.dayOfWeek, currentLanguage),
                                fontSize = if (viewMode == ScheduleViewMode.DAY_1) 16.sp
                                          else if (isMonthView) 10.sp
                                          else 14.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) textColor else textColor.copy(alpha = 0.6f)
                            )
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(
                                        if (viewMode == ScheduleViewMode.DAY_1) 48.dp
                                        else if (isMonthView) 28.dp
                                        else 36.dp
                                    )
                                    .clip(RoundedCornerShape(if (viewMode == ScheduleViewMode.DAY_1) 12.dp else 8.dp))
                                    .background(
                                        if (isToday) accentColor
                                        else Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    fontSize = if (viewMode == ScheduleViewMode.DAY_1) 22.sp
                                              else if (isMonthView) 12.sp
                                              else 16.sp,
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

            // Scroll to current time on first load
            LaunchedEffect(Unit) {
                val currentHour = kotlinx.datetime.Clock.System.now()
                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).hour
                scrollState.scrollToItem((currentHour - 2).coerceAtLeast(0))
            }

            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(24) { hour ->
                    TimeRow(
                        hour = hour,
                        days = daysToShow,
                        today = today,
                        schedules = schedules,
                        gridLineColor = gridLineColor,
                        textColor = textColor,
                        backgroundColor = backgroundColor,
                        isMonthView = isMonthView,
                        viewMode = viewMode,
                        horizontalScrollState = horizontalScrollState,
                        isSelectionMode = isSelectionMode,
                        selectedScheduleIds = selectedScheduleIds,
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
                        onScheduleClick = { scheduleId ->
                            if (isSelectionMode) {
                                selectedScheduleIds = if (scheduleId in selectedScheduleIds) {
                                    selectedScheduleIds - scheduleId
                                } else {
                                    selectedScheduleIds + scheduleId
                                }
                                if (selectedScheduleIds.isEmpty()) {
                                    isSelectionMode = false
                                }
                            }
                        }
                    )
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
                    }

                    showEventDialog = false
                    selectedDate = null
                }
            }
        )
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
    isMonthView: Boolean,
    viewMode: ScheduleViewMode,
    horizontalScrollState: ScrollState,
    isSelectionMode: Boolean,
    selectedScheduleIds: Set<String>,
    onCellClick: (LocalDate, Int) -> Unit,
    onScheduleLongPress: (String) -> Unit,
    onScheduleClick: (String) -> Unit
) {
    val hourString = String.format("%02d:00", hour)
    val useWeight = viewMode == ScheduleViewMode.DAY_1 || viewMode == ScheduleViewMode.DAY_3
    val rowHeight = if (viewMode == ScheduleViewMode.DAY_1) 80.dp else 60.dp

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

        // Day Cells - with synchronized horizontal scroll (only for week/month)
        Row(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (isMonthView || viewMode == ScheduleViewMode.WEEK)
                        Modifier.horizontalScroll(horizontalScrollState)
                    else
                        Modifier
                )
        ) {
            days.forEach { date ->
                val isToday = date == today
                val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
                val dateStr = formatDate(date)
                val cellSchedules = schedules.filter {
                    it.date == dateStr && it.startTime.startsWith(String.format("%02d", hour))
                }

                Box(
                    modifier = Modifier
                        .then(
                            if (useWeight) Modifier.weight(1f)
                            else Modifier.width(if (isMonthView) 48.dp else 60.dp)
                        )
                        .fillMaxHeight()
                        .background(
                            when {
                                isToday -> textColor.copy(alpha = 0.05f)
                                isWeekend -> textColor.copy(alpha = 0.03f)
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            width = 0.5.dp,
                            color = gridLineColor
                        )
                        .clickable { onCellClick(date, hour) }
                ) {
                    // Show schedules in this cell
                    cellSchedules.forEach { schedule ->
                        ScheduleItem(
                            schedule = schedule,
                            backgroundColor = backgroundColor,
                            isMonthView = isMonthView,
                            isSelected = schedule.id in selectedScheduleIds,
                            isSelectionMode = isSelectionMode,
                            onLongPress = { onScheduleLongPress(schedule.id) },
                            onClick = { onScheduleClick(schedule.id) }
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

    val startTime = String.format("%02d:00", selectedHour)
    val endTime = String.format("%02d:00", (selectedHour + 1) % 24)

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
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        tint = if (selectedHabitId != null) textColor else textColor.copy(alpha = 0.3f)
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayDate = formatDateForDisplay(selectedDate, currentLanguage)
                    Column {
                        Text(
                            text = "Start:",
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "$displayDate $startTime",
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                    }
                    Column {
                        Text(
                            text = "End:",
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "$displayDate $endTime",
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                    }
                    IconButton(onClick = { /* Open time picker */ }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Time",
                            tint = textColor.copy(alpha = 0.6f)
                        )
                    }
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
    return when (language) {
        Language.TURKISH -> when (dayOfWeek) {
            DayOfWeek.MONDAY -> "Pzt"
            DayOfWeek.TUESDAY -> "Sal"
            DayOfWeek.WEDNESDAY -> "Çar"
            DayOfWeek.THURSDAY -> "Per"
            DayOfWeek.FRIDAY -> "Cum"
            DayOfWeek.SATURDAY -> "Cmt"
            DayOfWeek.SUNDAY -> "Paz"
            else -> "?"
        }
        Language.SPANISH -> when (dayOfWeek) {
            DayOfWeek.MONDAY -> "L"
            DayOfWeek.TUESDAY -> "M"
            DayOfWeek.WEDNESDAY -> "X"
            DayOfWeek.THURSDAY -> "J"
            DayOfWeek.FRIDAY -> "V"
            DayOfWeek.SATURDAY -> "S"
            DayOfWeek.SUNDAY -> "D"
            else -> "?"
        }
        else -> when (dayOfWeek) {
            DayOfWeek.MONDAY -> "M"
            DayOfWeek.TUESDAY -> "T"
            DayOfWeek.WEDNESDAY -> "W"
            DayOfWeek.THURSDAY -> "Th"
            DayOfWeek.FRIDAY -> "F"
            DayOfWeek.SATURDAY -> "S"
            DayOfWeek.SUNDAY -> "Su"
            else -> "?"
        }
    }
}
