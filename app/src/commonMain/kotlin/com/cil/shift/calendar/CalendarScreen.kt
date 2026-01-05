package com.cil.shift.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.currentDate
import com.cil.shift.core.common.localization.LocalizationHelpers
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.localization.StringResources
import com.cil.shift.core.common.localization.localized
import com.cil.shift.feature.habits.domain.model.Habit
import com.cil.shift.feature.habits.domain.repository.HabitRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToCreateHabit: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()
    val habitRepository = koinInject<HabitRepository>()

    var currentMonth by remember {
        mutableStateOf(
            currentDate().let { YearMonth(it.year, it.month) }
        )
    }
    var selectedDate by remember {
        mutableStateOf<LocalDate?>(null)
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    var allHabits by remember { mutableStateOf<List<Habit>>(emptyList()) }
    var selectedHabitIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val scope = rememberCoroutineScope()
    val today = currentDate()

    // Load all habits when bottom sheet opens
    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet) {
            scope.launch {
                habitRepository.getHabits().collect { habits ->
                    allHabits = habits
                }
            }
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = StringResources.calendar.localized(),
                        color = textColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Month Navigation
            MonthNavigationHeader(
                currentMonth = currentMonth,
                currentLanguage = currentLanguage,
                onPreviousMonth = {
                    currentMonth = currentMonth.previous()
                },
                onNextMonth = {
                    currentMonth = currentMonth.next()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Day of week headers
            DayOfWeekHeaders(currentLanguage = currentLanguage)

            Spacer(modifier = Modifier.height(12.dp))

            // Calendar grid
            CalendarGrid(
                month = currentMonth,
                selectedDate = selectedDate,
                today = today,
                onDateSelected = { date ->
                    // Only allow selection of today or future dates
                    if (date >= today) {
                        selectedDate = date
                        showBottomSheet = true
                        selectedHabitIds = emptySet() // Reset selection
                    }
                }
            )
        }
    }

    // Bottom Sheet for habit selection
    if (showBottomSheet && selectedDate != null) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        LaunchedEffect(Unit) {
            sheetState.expand()
        }

        ModalBottomSheet(
            onDismissRequest = {
                // Allow swipe-down dismissal - same behavior as Cancel button
                showBottomSheet = false
                selectedDate = null
            },
            containerColor = backgroundColor,
            sheetState = sheetState
        ) {
            HabitSelectionBottomSheet(
                selectedDate = selectedDate!!,
                allHabits = allHabits,
                selectedHabitIds = selectedHabitIds,
                currentLanguage = currentLanguage,
                onHabitToggle = { habitId ->
                    selectedHabitIds = if (selectedHabitIds.contains(habitId)) {
                        selectedHabitIds - habitId
                    } else {
                        selectedHabitIds + habitId
                    }
                },
                onConfirm = {
                    scope.launch {
                        val dateString = "${selectedDate!!.year}-${selectedDate!!.monthNumber.toString().padStart(2, '0')}-${selectedDate!!.dayOfMonth.toString().padStart(2, '0')}"
                        // Assign selected habits to the date
                        selectedHabitIds.forEach { habitId ->
                            habitRepository.updateCurrentValue(habitId, dateString, 0)
                        }
                        showBottomSheet = false
                        selectedDate = null
                    }
                },
                onDismiss = {
                    showBottomSheet = false
                    selectedDate = null
                }
            )
        }
    }
}

@Composable
private fun MonthNavigationHeader(
    currentMonth: YearMonth,
    currentLanguage: com.cil.shift.core.common.localization.Language,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous month",
                tint = textColor,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "${LocalizationHelpers.getMonthName(currentMonth.month.number, currentLanguage)} ${currentMonth.year}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next month",
                tint = textColor,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun DayOfWeekHeaders(currentLanguage: com.cil.shift.core.common.localization.Language) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        daysOfWeek.forEach { dayOfWeek ->
            Text(
                text = LocalizationHelpers.getDayNameShort(dayOfWeek, currentLanguage),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    selectedDate: LocalDate?,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val dates = remember(month) {
        buildCalendarDates(month)
    }

    // Calculate cell size based on available width
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val cellSize = ((maxWidth - 48.dp) / 7) // Account for spacing
        val spacing = 6.dp

        Column(
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            val rows = dates.chunked(7)
            rows.forEach { weekDates ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    weekDates.forEach { dateOrNull ->
                        if (dateOrNull != null) {
                            DateCell(
                                date = dateOrNull,
                                isSelected = dateOrNull == selectedDate,
                                isToday = dateOrNull == today,
                                isDisabled = dateOrNull < today,
                                cellSize = cellSize,
                                onClick = { onDateSelected(dateOrNull) }
                            )
                        } else {
                            // Empty cell for alignment
                            Spacer(modifier = Modifier.size(cellSize))
                        }
                    }
                    // Fill remaining cells if week is incomplete
                    repeat(7 - weekDates.size) {
                        Spacer(modifier = Modifier.size(cellSize))
                    }
                }
            }
        }
    }
}

@Composable
private fun DateCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    isDisabled: Boolean,
    cellSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .size(cellSize)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> Color(0xFF4E7CFF)
                    isToday -> Color(0xFF00D9FF).copy(alpha = 0.2f)
                    else -> cardColor.copy(alpha = 0.5f)
                }
            )
            .border(
                width = 1.dp,
                color = when {
                    isSelected -> Color.Transparent
                    isToday -> Color(0xFF00D9FF)
                    else -> textColor.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .then(
                if (!isDisabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 16.sp,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
            color = when {
                isDisabled -> textColor.copy(alpha = 0.3f)
                isSelected -> Color.White // Keep white for selected state
                isToday -> Color(0xFF00D9FF)
                else -> textColor.copy(alpha = 0.8f)
            }
        )
    }
}

@Composable
private fun HabitSelectionBottomSheet(
    selectedDate: LocalDate,
    allHabits: List<Habit>,
    selectedHabitIds: Set<String>,
    currentLanguage: com.cil.shift.core.common.localization.Language,
    onHabitToggle: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp)
    ) {
        // Date Display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Color(0xFF4E7CFF).copy(alpha = 0.2f)
                )
                .border(
                    width = 1.dp,
                    color = textColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${selectedDate.dayOfMonth} ${LocalizationHelpers.getMonthName(selectedDate.monthNumber, currentLanguage).take(3)}",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = selectedDate.year.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Habit count message
        if (allHabits.isNotEmpty()) {
            Text(
                text = StringResources.youHaveHabits.get(currentLanguage).replace("%d", allHabits.size.toString()),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = StringResources.letsSeeThem.get(currentLanguage),
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Habits List
        if (allHabits.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = StringResources.noHabitsAvailable.get(currentLanguage),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = StringResources.createYourFirstHabit.get(currentLanguage),
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Text(
                text = StringResources.timeSlot.get(currentLanguage),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allHabits) { habit ->
                    HabitSelectionCard(
                        habit = habit,
                        isSelected = selectedHabitIds.contains(habit.id),
                        currentLanguage = currentLanguage,
                        onClick = { onHabitToggle(habit.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = StringResources.endOfList.get(currentLanguage),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = textColor
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = textColor.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = StringResources.cancel.localized(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onConfirm,
                enabled = selectedHabitIds.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4E7CFF),
                    disabledContainerColor = Color(0xFF4E7CFF).copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = StringResources.assign.localized(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun HabitSelectionCard(
    habit: Habit,
    isSelected: Boolean,
    currentLanguage: com.cil.shift.core.common.localization.Language,
    onClick: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

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

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF4E7CFF).copy(alpha = 0.3f) else textColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                cardColor
            } else {
                cardColor.copy(alpha = 0.6f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Habit Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(habitColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getIconEmoji(habit.icon),
                        fontSize = 20.sp
                    )
                }

                // Habit Details
                Column {
                    Text(
                        text = LocalizationHelpers.getLocalizedHabitName(habit.name, currentLanguage),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (habit.frequency) {
                                is com.cil.shift.feature.habits.domain.model.Frequency.Daily -> StringResources.daily.get(currentLanguage)
                                is com.cil.shift.feature.habits.domain.model.Frequency.Weekly -> StringResources.weekly.get(currentLanguage)
                                is com.cil.shift.feature.habits.domain.model.Frequency.Custom -> StringResources.custom.get(currentLanguage)
                            },
                            fontSize = 12.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                        if (habit.reminderTime != null) {
                            Text(
                                text = "• ${habit.reminderTime}",
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Selection Checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) Color(0xFF4E7CFF) else textColor.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .background(
                        if (isSelected) Color(0xFF4E7CFF) else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun getIconEmoji(icon: String): String {
    return when (icon.lowercase()) {
        // Health
        "water", "wat", "hydration" -> "💧"
        "vegetables", "veg" -> "🥦"
        "fruit", "fru" -> "🍉"
        "cooking", "coo" -> "🍳"
        "sunrise", "sun" -> "🌅"
        "sunset" -> "🌇"
        "pill", "med" -> "💊"
        "nutrition", "food" -> "🥗"
        // Mindfulness
        "journal", "jou" -> "✍️"
        "pray", "pra" -> "🙏"
        "meditation", "me", "mindfulness" -> "🧘"
        "relaxed", "rel" -> "😌"
        "detox", "det" -> "🚫"
        // Learning
        "books", "book", "boo", "read" -> "📚"
        "course", "cou" -> "📝"
        "instrument", "ins" -> "🎷"
        "study", "stu" -> "🧑‍🎓"
        "flute", "flu", "ute" -> "🎺"
        // Active
        "running", "run" -> "🏃"
        "walking", "wal" -> "🚶"
        "dance", "dan" -> "💃"
        "pilates", "pil" -> "🤸"
        "gym", "dumbbell", "dum", "fitness", "workout" -> "🏋️"
        "sports", "spo" -> "⚽"
        "stretching", "str" -> "🤾"
        "yoga", "yog" -> "🧘"
        // Self-care
        "shower", "sho" -> "🚿"
        "skincare", "ski" -> "🧴"
        "haircare", "hai" -> "💆"
        // Social
        "couple", "heart", "hea" -> "💕"
        "party", "par" -> "🥳"
        "family", "fam" -> "👨‍👩‍👧"
        // Financial
        "budget", "bud" -> "💰"
        "invest", "inv" -> "📊"
        "expenses", "exp" -> "💸"
        // Home
        "clean", "cle" -> "🧹"
        "bed" -> "🛏️"
        "laundry", "lau" -> "🧺"
        "dishes", "dis" -> "🪣"
        "bills", "bil" -> "🧾"
        // Additional
        "leaf", "lea" -> "🍃"
        "brain", "bra" -> "🧠"
        "fire", "fir" -> "🔥"
        "moon", "mo" -> "🌙"
        "bulb", "bul" -> "💡"
        "smile", "smi" -> "😊"
        "check", "che" -> "✅"
        "coffee", "cof" -> "☕"
        "sleep", "sle" -> "😴"
        "music", "mus" -> "🎵"
        "art", "palette", "pale", "pal" -> "🎨"
        "briefcase", "bri", "work" -> "💼"
        else -> {
            if (icon.any { it.code >= 0x1F300 }) icon else "✓"
        }
    }
}

// Helper data class for YearMonth
data class YearMonth(val year: Int, val month: Month) {
    fun next(): YearMonth {
        return if (month == Month.DECEMBER) {
            YearMonth(year + 1, Month.JANUARY)
        } else {
            YearMonth(year, Month(month.number + 1))
        }
    }

    fun previous(): YearMonth {
        return if (month == Month.JANUARY) {
            YearMonth(year - 1, Month.DECEMBER)
        } else {
            YearMonth(year, Month(month.number - 1))
        }
    }
}

// Helper function to build calendar dates
private fun buildCalendarDates(yearMonth: YearMonth): List<LocalDate?> {
    val firstDayOfMonth = LocalDate(yearMonth.year, yearMonth.month, 1)

    // Calculate days in month
    val daysInMonth = when (yearMonth.month) {
        Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY, Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        Month.FEBRUARY -> if (isLeapYear(yearMonth.year)) 29 else 28
        else -> 30
    }

    // Get the day of week for the first day (Monday = 1, Sunday = 7)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.isoDayNumber

    // Create the list with nulls for days before the first day
    val dates = mutableListOf<LocalDate?>()

    // Add null for empty cells before the first day
    repeat(firstDayOfWeek - 1) {
        dates.add(null)
    }

    // Add all days of the month
    for (day in 1..daysInMonth) {
        dates.add(LocalDate(yearMonth.year, yearMonth.month, day))
    }

    return dates
}

private fun isLeapYear(year: Int): Boolean {
    return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
}
