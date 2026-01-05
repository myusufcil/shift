package com.cil.shift.feature.habits.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cil.shift.core.common.achievement.Achievement
import com.cil.shift.core.common.achievement.AchievementManager
import com.cil.shift.core.common.haptic.HapticType
import com.cil.shift.core.common.haptic.getHapticFeedbackManager
import com.cil.shift.core.common.localization.LocalizationHelpers
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.localization.StringResources
import com.cil.shift.core.common.localization.localized
import com.cil.shift.feature.habits.domain.model.HabitSchedule
import com.cil.shift.feature.habits.domain.model.HabitType
import com.cil.shift.feature.habits.presentation.home.components.*
import com.cil.shift.feature.settings.presentation.achievements.AchievementUnlockPopup
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import sh.calvin.reorderable.ReorderableLazyListState

@Composable
fun NewHomeScreen(
    onNavigateToCreateHabit: () -> Unit,
    onNavigateToHabitDetail: (String, String?) -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel {
        throw IllegalStateException("ViewModel should be provided via DI")
    }
) {
    val state by viewModel.state.collectAsState()
    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()

    // Haptic feedback manager
    val hapticManager = remember { getHapticFeedbackManager() }

    // Achievement manager
    val achievementManager = koinInject<AchievementManager>()
    val newlyUnlockedAchievement by achievementManager.newlyUnlocked.collectAsState()
    var displayedAchievement by remember { mutableStateOf<Achievement?>(null) }

    // When a new achievement is unlocked, show it
    LaunchedEffect(newlyUnlockedAchievement) {
        if (newlyUnlockedAchievement != null) {
            displayedAchievement = newlyUnlockedAchievement
            hapticManager.performHaptic(HapticType.SUCCESS)
        }
    }

    // Confetti animation state
    var showConfetti by remember { mutableStateOf(false) }

    // Track all habits completion
    val completedHabits = state.habits.count { it.isCompletedToday }
    val totalHabits = state.habits.size
    val allHabitsCompleted = totalHabits > 0 && completedHabits == totalHabits

    // Get today's date string for confetti tracking
    val todayDateString = remember {
        val today = com.cil.shift.core.common.currentDate()
        "${today.year}-${today.monthNumber.toString().padStart(2, '0')}-${today.dayOfMonth.toString().padStart(2, '0')}"
    }

    // Trigger confetti when all habits are completed (only once per day)
    LaunchedEffect(allHabitsCompleted, state.confettiShownForDate) {
        if (allHabitsCompleted && state.confettiShownForDate != todayDateString) {
            showConfetti = true
            viewModel.onEvent(HomeEvent.ConfettiShown(todayDateString))
        }
    }

    // Refresh data when screen becomes visible, preserving selected date
    androidx.compose.runtime.LaunchedEffect(Unit) {
        // Only refresh if no date is selected, otherwise keep the selected date
        if (state.selectedDate == null) {
            viewModel.refresh()
        } else {
            // Just reload chart data without changing habits list
            viewModel.onEvent(HomeEvent.RefreshChartOnly)
        }
    }

    // Keep track of habits list for reordering
    val habitsToShow = remember(state.habits, state.showAllHabits) {
        if (state.showAllHabits) state.habits else state.habits.take(5)
    }

    // Setup reorderable state
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // Use keys to find the actual habit indices, not LazyColumn indices
        val fromHabitId = from.key as? String ?: return@rememberReorderableLazyListState
        val toHabitId = to.key as? String ?: return@rememberReorderableLazyListState

        val allHabits = state.habits.toMutableList()
        val fromIndex = allHabits.indexOfFirst { it.habit.id == fromHabitId }
        val toIndex = allHabits.indexOfFirst { it.habit.id == toHabitId }

        if (fromIndex != -1 && toIndex != -1) {
            val item = allHabits.removeAt(fromIndex)
            allHabits.add(toIndex, item)
            val reorderedIds = allHabits.map { it.habit.id }
            viewModel.onEvent(HomeEvent.ReorderHabits(reorderedIds))
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with user info and notifications
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = state.currentDate.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4E7CFF),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${StringResources.hello.localized()}, ${state.userName}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    IconButton(
                        onClick = onNavigateToNotifications,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(cardColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = StringResources.notifications.localized(),
                            tint = textColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Weekly Calendar
            item {
                WeeklyCalendar(
                    currentDayOfWeek = 3, // Wednesday = 3
                    selectedDate = state.selectedDate,
                    onDaySelected = { date ->
                        viewModel.onEvent(HomeEvent.DaySelected(date))
                    },
                    currentLanguage = currentLanguage
                )
            }

            // Daily Goal Progress
            item {
                val completedHabits = state.habits.count { it.isCompletedToday }
                val totalHabits = state.habits.size
                val progress = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f

                DailyGoalProgress(progress = progress)
            }

            // Weekly Progress Chart
            item {
                // Calculate selected day index for chart highlighting
                val selectedDayIndex = remember(state.selectedDate) {
                    state.selectedDate?.let { selectedDate ->
                        val today = com.cil.shift.core.common.currentDate()
                        val daysDifference = selectedDate.toEpochDays() - today.toEpochDays()
                        // Chart shows days from 6 days ago to today (index 0-6)
                        // Today = index 6, yesterday = index 5, etc.
                        (6 + daysDifference).toInt().takeIf { it in 0..6 }
                    }
                }

                WeeklyProgressChart(
                    weeklyData = state.weeklyChartData,
                    chartType = state.weeklyChartType,
                    selectedDayIndex = selectedDayIndex,
                    onChartTypeChange = { chartType ->
                        viewModel.onEvent(HomeEvent.ChangeWeeklyChartType(chartType))
                    }
                )
            }

            // Today's Schedule section (if there are scheduled events)
            if (state.scheduledEvents.isNotEmpty()) {
                item {
                    Text(
                        text = StringResources.schedule.localized(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                items(
                    items = state.scheduledEvents,
                    key = { it.id }
                ) { schedule ->
                    ScheduledEventCard(
                        schedule = schedule,
                        currentLanguage = currentLanguage
                    )
                }
            }

            // Today's Habits header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = StringResources.todaysHabits.localized(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    TextButton(onClick = { viewModel.onEvent(HomeEvent.ToggleShowAll) }) {
                        Text(
                            text = if (state.showAllHabits) StringResources.showLess.localized() else StringResources.viewAll.localized(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Habits list - different UI based on habit type
            if (state.habits.isEmpty()) {
                item {
                    EmptyStateComponent()
                }
            } else {
                items(
                    items = habitsToShow,
                    key = { it.habit.id }
                ) { habitWithCompletion ->
                    ReorderableItem(
                        state = reorderableLazyListState,
                        key = habitWithCompletion.habit.id
                    ) { isDragging ->
                        val habit = habitWithCompletion.habit
                        val habitColor = habit.color.toComposeColor()

                        when (habit.habitType) {
                            HabitType.TIMER -> {
                                HabitItemTimer(
                                    name = LocalizationHelpers.getLocalizedHabitName(habit.name, currentLanguage),
                                    currentMinutes = 0, // TODO: Get from completion data
                                    targetMinutes = habit.targetValue ?: 120,
                                    icon = habit.icon,
                                    color = habitColor,
                                    statusLabel = StringResources.focus.localized(),
                                    isCompleted = habitWithCompletion.isCompleted,
                                    streak = habitWithCompletion.currentStreak,
                                    onClick = { onNavigateToHabitDetail(habit.id, state.selectedDate?.toString()) },
                                    modifier = Modifier.longPressDraggableHandle()
                                )
                            }

                            HabitType.MEASURABLE -> {
                                HabitItemMeasurable(
                                    name = LocalizationHelpers.getLocalizedHabitName(habit.name, currentLanguage),
                                    currentValue = habitWithCompletion.currentValue,
                                    targetValue = habit.targetValue ?: 2000,
                                    unit = habit.targetUnit ?: "",
                                    icon = habit.icon,
                                    color = habitColor,
                                    streak = habitWithCompletion.currentStreak,
                                    onIncrement = {
                                        hapticManager.performHaptic(HapticType.LIGHT)
                                        viewModel.onEvent(HomeEvent.IncrementHabit(habit.id, 250))
                                    },
                                    onDecrement = {
                                        hapticManager.performHaptic(HapticType.LIGHT)
                                        viewModel.onEvent(HomeEvent.DecrementHabit(habit.id, 250))
                                    },
                                    onClick = { onNavigateToHabitDetail(habit.id, state.selectedDate?.toString()) },
                                    modifier = Modifier.longPressDraggableHandle()
                                )
                            }

                            HabitType.SESSION -> {
                                HabitItemSession(
                                    name = LocalizationHelpers.getLocalizedHabitName(habit.name, currentLanguage),
                                    subtitle = "${habit.targetValue ?: 15} ${StringResources.minsGoal.localized()} • ${habit.targetUnit ?: StringResources.session.localized()}",
                                    icon = habit.icon,
                                    color = habitColor,
                                    isCompleted = habitWithCompletion.isCompleted,
                                    streak = habitWithCompletion.currentStreak,
                                    onClick = { onNavigateToHabitDetail(habit.id, state.selectedDate?.toString()) },
                                    modifier = Modifier.longPressDraggableHandle()
                                )
                            }

                            HabitType.SIMPLE -> {
                                HabitItemSimple(
                                    name = LocalizationHelpers.getLocalizedHabitName(habit.name, currentLanguage),
                                    subtitle = if (habitWithCompletion.isCompleted) StringResources.done.localized() else habit.targetUnit,
                                    icon = habit.icon,
                                    color = habitColor,
                                    isCompleted = habitWithCompletion.isCompleted,
                                    streak = habitWithCompletion.currentStreak,
                                    onToggle = {
                                        // Haptic feedback - success when completing, light when uncompleting
                                        val hapticType = if (habitWithCompletion.isCompleted) HapticType.LIGHT else HapticType.SUCCESS
                                        hapticManager.performHaptic(hapticType)
                                        viewModel.onEvent(HomeEvent.ToggleHabit(habit.id))
                                    },
                                    onClick = { onNavigateToHabitDetail(habit.id, state.selectedDate?.toString()) },
                                    modifier = Modifier.longPressDraggableHandle()
                                )
                            }
                        }
                    }
                }
            }

        }

        // Confetti animation overlay
        ConfettiAnimation(
            isPlaying = showConfetti,
            onAnimationEnd = { showConfetti = false }
        )

        // Achievement unlock popup
        AchievementUnlockPopup(
            achievement = displayedAchievement,
            currentLanguage = currentLanguage,
            onDismiss = {
                displayedAchievement = null
                achievementManager.clearNewlyUnlocked()
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )
    }
}

// Helper to convert hex color to Compose Color
private fun String.toComposeColor(): Color {
    return try {
        val colorString = this.removePrefix("#")
        val colorInt = colorString.toLong(16)
        if (colorString.length == 6) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        Color(0xFF6C63FF)
    }
}
