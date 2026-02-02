package com.cil.shift.feature.habits.presentation.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cil.shift.core.common.achievement.Achievement
import com.cil.shift.core.common.achievement.AchievementManager
import com.cil.shift.core.common.auth.AuthManager
import com.cil.shift.core.common.auth.AuthState
import com.cil.shift.core.common.haptic.HapticType
import com.cil.shift.core.common.haptic.getHapticFeedbackManager
import com.cil.shift.core.common.honey.HoneyManager
import com.cil.shift.core.common.honey.HoneyReason
import com.cil.shift.core.common.purchase.PurchaseManager
import com.cil.shift.core.common.purchase.PremiumState
import com.cil.shift.core.common.localization.LocalizationHelpers
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.localization.StringResources
import com.cil.shift.core.common.localization.localized
import com.cil.shift.core.designsystem.components.CoachMarkController
import com.cil.shift.core.designsystem.components.coachMarkTarget
import com.cil.shift.core.designsystem.components.HoneyCounter
import com.cil.shift.core.designsystem.components.HoneyEarnedPopup
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
    onNavigateToPomodoro: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    coachMarkController: CoachMarkController? = null,
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

    // Auth manager for login check
    val authManager = koinInject<AuthManager>()
    val authState by authManager.authState.collectAsState()

    // Honey system
    val honeyManager = koinInject<HoneyManager>()
    val honeyStatus by honeyManager.honeyStatus.collectAsState()

    // Premium state - observe reactively for proper recomposition
    val purchaseManager = koinInject<PurchaseManager>()
    val premiumState by purchaseManager.premiumState.collectAsState()
    val isPremiumUser = premiumState is PremiumState.Premium
    var showHoneyEarned by remember { mutableStateOf(false) }
    var honeyEarnedAmount by remember { mutableStateOf(0) }
    var honeyEarnedReason by remember { mutableStateOf("") }

    // Check for daily reward on screen load - delay to avoid collision with coach marks
    val onboardingPreferences = koinInject<com.cil.shift.core.common.onboarding.OnboardingPreferences>()
    LaunchedEffect(Unit) {
        // Wait longer to let coach marks fully initialize
        kotlinx.coroutines.delay(1000)
        // Skip honey effect entirely if product walkthrough hasn't been seen (first launch)
        // or if coach mark is currently active
        if (onboardingPreferences.hasSeenProductWalkthrough() && coachMarkController?.isActive != true) {
            val dailyReward = honeyManager.checkDailyReward()
            if (dailyReward != null) {
                honeyEarnedAmount = dailyReward
                honeyEarnedReason = "Daily check-in bonus!"
                showHoneyEarned = true
            }
        }
    }

    // Observe honey earned from habit completions - skip if coach mark is active
    LaunchedEffect(state.lastHoneyEarned, state.lastHoneyReason) {
        if (state.lastHoneyEarned > 0 && state.lastHoneyReason.isNotEmpty() &&
            coachMarkController?.isActive != true) {
            honeyEarnedAmount = state.lastHoneyEarned
            honeyEarnedReason = state.lastHoneyReason
            showHoneyEarned = true
        }
    }

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
            // Header with user info, honey counter, and notifications
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .let { mod ->
                                coachMarkController?.let { mod.coachMarkTarget(it, HomeTutorialTargets.GREETING_HEADER) } ?: mod
                            }
                    ) {
                        Text(
                            text = state.currentDate.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4E7CFF),
                            letterSpacing = 1.sp
                        )
                        val displayName = when {
                            authState is AuthState.Authenticated -> {
                                val user = (authState as AuthState.Authenticated).user
                                user.displayName
                                    ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                                    ?: state.userName
                            }
                            else -> StringResources.user.localized()
                        }
                        Text(
                            text = "${StringResources.hello.localized()}, $displayName",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Honey counter (show ∞ for premium users)
                        HoneyCounter(
                            balance = honeyStatus.balance,
                            onClick = {
                                // Premium users don't need to see premium screen
                                if (isPremiumUser) return@HoneyCounter
                                // Check if user is logged in before navigating to premium
                                if (authState is AuthState.Authenticated) {
                                    onNavigateToPremium()
                                } else {
                                    onNavigateToLogin()
                                }
                            },
                            isPremium = isPremiumUser,
                            isLow = honeyStatus.isLowBalance,
                            isCritical = honeyStatus.isCriticalBalance
                        )

                        IconButton(
                            onClick = onNavigateToPomodoro,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(cardColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = StringResources.pomodoroFocusTimer.localized(),
                                tint = Color(0xFF4ECDC4),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onNavigateToNotifications,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(cardColor)
                                .let { mod ->
                                    coachMarkController?.let { mod.coachMarkTarget(it, HomeTutorialTargets.NOTIFICATION_BUTTON) } ?: mod
                                }
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
            }

            // Weekly Calendar
            item {
                Box(
                    modifier = Modifier.let { mod ->
                        coachMarkController?.let { mod.coachMarkTarget(it, HomeTutorialTargets.WEEKLY_CALENDAR) } ?: mod
                    }
                ) {
                    WeeklyCalendar(
                        currentDayOfWeek = 3, // Wednesday = 3
                        selectedDate = state.selectedDate,
                        onDaySelected = { date ->
                            viewModel.onEvent(HomeEvent.DaySelected(date))
                        },
                        currentLanguage = currentLanguage
                    )
                }
            }

            // Daily Goal Progress
            item {
                val completedHabits = state.habits.count { it.isCompletedToday }
                val totalHabits = state.habits.size
                val progress = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f

                Box(
                    modifier = Modifier.let { mod ->
                        coachMarkController?.let { mod.coachMarkTarget(it, HomeTutorialTargets.DAILY_PROGRESS) } ?: mod
                    }
                ) {
                    DailyGoalProgress(progress = progress)
                }
            }

            // Weekly Progress Chart
            item {
                // Calculate selected day index for chart highlighting
                // Chart shows Monday to Sunday (index 0=Mon, 6=Sun)
                val selectedDayIndex = remember(state.selectedDate, state.selectedDayIndex) {
                    state.selectedDate?.let { selectedDate ->
                        // Use the day of week (Monday=0, Sunday=6)
                        val today = com.cil.shift.core.common.currentDate()
                        val daysFromMonday = today.dayOfWeek.ordinal
                        val mondayOfWeek = today.toEpochDays() - daysFromMonday
                        val sundayOfWeek = mondayOfWeek + 6

                        // Check if selected date is within current week
                        val selectedEpochDays = selectedDate.toEpochDays()
                        if (selectedEpochDays in mondayOfWeek..sundayOfWeek) {
                            selectedDate.dayOfWeek.ordinal
                        } else {
                            null
                        }
                    } ?: state.selectedDayIndex // Default to today's index from state
                }

                WeeklyProgressChart(
                    weeklyData = state.weeklyChartData,
                    chartType = state.weeklyChartType,
                    selectedDayIndex = selectedDayIndex,
                    onChartTypeChange = { chartType ->
                        viewModel.onEvent(HomeEvent.ChangeWeeklyChartType(chartType))
                    },
                    currentLanguage = currentLanguage
                )
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
                    Box(
                        modifier = Modifier.let { mod ->
                            coachMarkController?.let { mod.coachMarkTarget(it, HomeTutorialTargets.FIRST_HABIT) } ?: mod
                        }
                    ) {
                        EmptyStateComponent()
                    }
                }
            } else {
                val firstHabitId = habitsToShow.firstOrNull()?.habit?.id
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
                        val isFirstHabit = habit.id == firstHabitId

                        // Apply visual feedback when dragging
                        val elevation by animateDpAsState(
                            targetValue = if (isDragging) 8.dp else 0.dp,
                            animationSpec = tween(200),
                            label = "dragElevation"
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (isDragging) 1.02f else 1f,
                            animationSpec = tween(200),
                            label = "dragScale"
                        )

                        val habitModifier = if (isFirstHabit && coachMarkController != null) {
                            Modifier.coachMarkTarget(coachMarkController, HomeTutorialTargets.FIRST_HABIT)
                        } else {
                            Modifier
                        }

                        val dragModifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                shadowElevation = elevation.toPx()
                            }
                            .zIndex(if (isDragging) 1f else 0f)

                        when (habit.habitType) {
                            HabitType.TIMER -> {
                                HabitItemTimer(
                                    name = LocalizationHelpers.getLocalizedHabitName(habit.name, currentLanguage),
                                    currentMinutes = habitWithCompletion.currentValue,
                                    targetMinutes = habit.targetValue ?: 30,
                                    icon = habit.icon,
                                    color = habitColor,
                                    statusLabel = StringResources.focus.localized(),
                                    isCompleted = habitWithCompletion.isCompleted,
                                    streak = habitWithCompletion.currentStreak,
                                    currentLanguage = currentLanguage,
                                    isTimerRunning = state.runningTimers.contains(habit.id),
                                    onTimerToggle = {
                                        hapticManager.performHaptic(HapticType.LIGHT)
                                        viewModel.onEvent(HomeEvent.ToggleTimer(habit.id))
                                    },
                                    onTimerTick = { viewModel.onEvent(HomeEvent.TimerTick(habit.id)) },
                                    onTimerReset = {
                                        hapticManager.performHaptic(HapticType.LIGHT)
                                        viewModel.onEvent(HomeEvent.ResetTimer(habit.id))
                                    },
                                    onClick = { onNavigateToHabitDetail(habit.id, state.selectedDate?.toString()) },
                                    modifier = dragModifier.then(habitModifier).then(Modifier.longPressDraggableHandle())
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
                                    currentLanguage = currentLanguage,
                                    onIncrement = {
                                        hapticManager.performHaptic(HapticType.LIGHT)
                                        viewModel.onEvent(HomeEvent.IncrementHabit(habit.id, 250))
                                    },
                                    onDecrement = {
                                        hapticManager.performHaptic(HapticType.LIGHT)
                                        viewModel.onEvent(HomeEvent.DecrementHabit(habit.id, 250))
                                    },
                                    onClick = { onNavigateToHabitDetail(habit.id, state.selectedDate?.toString()) },
                                    modifier = dragModifier.then(habitModifier).then(Modifier.longPressDraggableHandle())
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
                                    modifier = dragModifier.then(habitModifier).then(Modifier.longPressDraggableHandle())
                                )
                            }

                            HabitType.QUIT -> {
                                HabitItemQuit(
                                    name = LocalizationHelpers.getLocalizedHabitName(habit.name, currentLanguage),
                                    quitStartDate = habit.quitStartDate,
                                    icon = habit.icon,
                                    color = habitColor,
                                    onClick = { onNavigateToHabitDetail(habit.id, state.selectedDate?.toString()) },
                                    modifier = dragModifier.then(habitModifier).then(Modifier.longPressDraggableHandle())
                                )
                            }

                            HabitType.NEGATIVE -> {
                                HabitItemNegative(
                                    name = LocalizationHelpers.getLocalizedHabitName(habit.name, currentLanguage),
                                    currentValue = habitWithCompletion.currentValue,
                                    limitValue = habit.targetValue ?: 2,
                                    unit = habit.targetUnit ?: "times",
                                    icon = habit.icon,
                                    color = habitColor,
                                    onIncrement = { amount ->
                                        hapticManager.performHaptic(HapticType.LIGHT)
                                        viewModel.onEvent(HomeEvent.IncrementHabit(habit.id, amount))
                                    },
                                    onClick = { onNavigateToHabitDetail(habit.id, state.selectedDate?.toString()) },
                                    modifier = dragModifier.then(habitModifier).then(Modifier.longPressDraggableHandle())
                                )
                            }
                        }
                    }
                }
            }

            // Today's Schedule section (if there are scheduled events) - shown after habits
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

        // Honey earned popup
        HoneyEarnedPopup(
            visible = showHoneyEarned,
            amount = honeyEarnedAmount,
            reason = honeyEarnedReason,
            onDismiss = {
                showHoneyEarned = false
                viewModel.onEvent(HomeEvent.ClearHoneyEarned)
            }
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
