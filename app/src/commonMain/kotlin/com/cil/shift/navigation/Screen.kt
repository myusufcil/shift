package com.cil.shift.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.localization.StringResources
import com.cil.shift.core.common.onboarding.OnboardingPreferences
import com.cil.shift.feature.habits.presentation.home.getHomeTutorialSteps
import org.koin.compose.koinInject
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.cil.shift.feature.habits.presentation.home.NewHomeScreen
import com.cil.shift.feature.habits.presentation.home.HomeViewModel
import com.cil.shift.feature.settings.presentation.SettingsScreen
import com.cil.shift.feature.settings.presentation.SettingsViewModel
import com.cil.shift.feature.statistics.presentation.StatisticsScreen
import com.cil.shift.feature.statistics.presentation.StatisticsViewModel
import com.cil.shift.calendar.CalendarScreen

// Tab Screens
object HomeTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Filled.Home)
            val localizationManager = koinInject<LocalizationManager>()
            val currentLanguage by localizationManager.currentLanguage.collectAsState()

            return remember(currentLanguage) {
                TabOptions(
                    index = 0u,
                    title = "Shift",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        cafe.adriel.voyager.navigator.Navigator(screen = HomeScreenWrapper)
    }
}

// Wrapper screen for navigation within Home tab
private object HomeScreenWrapper : cafe.adriel.voyager.core.screen.Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: HomeViewModel = koinInject()
        val onboardingPreferences = koinInject<OnboardingPreferences>()
        val localizationManager = koinInject<LocalizationManager>()
        val currentLanguage by localizationManager.currentLanguage.collectAsState()

        // Observe habits state to determine if user has habits
        val homeState by viewModel.state.collectAsState()
        val hasHabits = homeState.habits.isNotEmpty()

        // Use global coach mark controller
        val coachMarkController = GlobalNavigationEvents.coachMarkController

        // Show tutorial on first launch - only trigger once
        var tutorialStarted by remember { mutableStateOf(false) }
        LaunchedEffect(coachMarkController) {
            if (coachMarkController != null && !onboardingPreferences.hasSeenProductWalkthrough() && !tutorialStarted) {
                tutorialStarted = true
                // Short delay to let the UI settle and habits to load before showing coach marks
                kotlinx.coroutines.delay(300)
                // Get steps with current habit state at this moment
                val steps = getHomeTutorialSteps(currentLanguage, hasHabits)
                coachMarkController.start(steps) {
                    onboardingPreferences.setProductWalkthroughSeen(true)
                }
            }
        }

        // Track navigator stack size to detect returns from child screens
        val navigatorSize = navigator.size
        val previousSizeState = remember { androidx.compose.runtime.mutableStateOf(navigatorSize) }

        // Detect when returning from a screen (stack size decreased)
        LaunchedEffect(navigatorSize) {
            if (navigatorSize < previousSizeState.value) {
                // Returned from a child screen - refresh with today's data
                viewModel.onEvent(com.cil.shift.feature.habits.presentation.home.HomeEvent.ResetToToday)
            }
            previousSizeState.value = navigatorSize
        }

        // Observe global navigation event
        LaunchedEffect(GlobalNavigationEvents.shouldNavigateToCreateHabit) {
            if (GlobalNavigationEvents.shouldNavigateToCreateHabit) {
                navigator.push(CreateHabitScreen())
                GlobalNavigationEvents.resetNavigateToCreateHabit()
            }
        }

        NewHomeScreen(
            onNavigateToCreateHabit = { navigator.push(CreateHabitScreen()) },
            onNavigateToHabitDetail = { habitId, selectedDate -> navigator.push(HabitDetailScreen(habitId, selectedDate)) },
            onNavigateToNotifications = { navigator.push(NotificationsScreenNav) },
            onNavigateToPomodoro = { navigator.push(PomodoroScreenNav) },
            onNavigateToProfile = { navigator.push(ProfileScreenNav) },
            onNavigateToPremium = { navigator.push(PremiumScreenNav) },
            onNavigateToLogin = { navigator.push(LoginScreenNav) },
            coachMarkController = coachMarkController,
            viewModel = viewModel
        )
    }
}

object StatisticsTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Filled.Star)
            val localizationManager = koinInject<LocalizationManager>()
            val currentLanguage by localizationManager.currentLanguage.collectAsState()

            return remember(currentLanguage) {
                TabOptions(
                    index = 1u,
                    title = StringResources.navStatistics.get(currentLanguage),
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        val viewModel: StatisticsViewModel = koinInject()
        StatisticsScreen(viewModel = viewModel)
    }
}

object CalendarTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Filled.CalendarToday)
            val localizationManager = koinInject<LocalizationManager>()
            val currentLanguage by localizationManager.currentLanguage.collectAsState()

            return remember(currentLanguage) {
                TabOptions(
                    index = 3u,
                    title = StringResources.navCalendar.get(currentLanguage),
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        cafe.adriel.voyager.navigator.Navigator(screen = CalendarScreenWrapper)
    }
}

private object CalendarScreenWrapper : cafe.adriel.voyager.core.screen.Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val onboardingPreferences = koinInject<OnboardingPreferences>()
        val localizationManager = koinInject<LocalizationManager>()
        val currentLanguage by localizationManager.currentLanguage.collectAsState()

        // Use global coach mark controller for calendar tutorial
        val coachMarkController = GlobalNavigationEvents.coachMarkController

        // Show calendar tutorial on first visit - only when Calendar tab is active
        var calendarTutorialStarted by remember { mutableStateOf(false) }
        val isCalendarTabActive = GlobalNavigationEvents.currentTabIndex == 3

        LaunchedEffect(coachMarkController, isCalendarTabActive) {
            if (coachMarkController != null &&
                isCalendarTabActive &&
                !onboardingPreferences.hasSeenCalendarWalkthrough() &&
                !calendarTutorialStarted) {
                calendarTutorialStarted = true
                // Short delay to let the UI settle
                kotlinx.coroutines.delay(200)
                val steps = com.cil.shift.calendar.getCalendarTutorialSteps(currentLanguage)
                coachMarkController.start(steps) {
                    onboardingPreferences.setCalendarWalkthroughSeen(true)
                }
            }
        }

        CalendarScreen(
            onNavigateToCreateHabit = { selectedDate ->
                // Navigate to create habit screen
                // TODO: Pass selected date to CreateHabitScreen when implemented
                navigator.push(CreateHabitScreen())
            },
            coachMarkController = coachMarkController
        )
    }
}

object ProfileTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Filled.Person)
            val localizationManager = koinInject<LocalizationManager>()
            val currentLanguage by localizationManager.currentLanguage.collectAsState()

            return remember(currentLanguage) {
                TabOptions(
                    index = 4u,
                    title = StringResources.navProfile.get(currentLanguage),
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        cafe.adriel.voyager.navigator.Navigator(screen = ProfileScreenNav)
    }
}
