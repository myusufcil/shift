package com.cil.shift.onboarding

import androidx.compose.runtime.*
import com.cil.shift.core.common.onboarding.OnboardingPreferences
import com.cil.shift.feature.onboarding.presentation.permission.NotificationPermissionScreen
import com.cil.shift.feature.onboarding.presentation.walkthrough.WalkthroughScreen
import com.cil.shift.feature.onboarding.presentation.walkthrough.WalkthroughState
import com.cil.shift.feature.onboarding.presentation.suggestions.HabitSuggestionsScreen
import org.koin.compose.getKoin
import org.koin.compose.koinInject

enum class OnboardingStep {
    WALKTHROUGH,
    SUGGESTIONS,
    PERMISSION
}

@Composable
fun OnboardingFlow(
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.WALKTHROUGH) }
    val onboardingPreferences = koinInject<OnboardingPreferences>()

    when (currentStep) {
        OnboardingStep.WALKTHROUGH -> {
            WalkthroughScreen(
                onComplete = { state ->
                    // Save onboarding data
                    saveOnboardingData(onboardingPreferences, state)
                    currentStep = OnboardingStep.SUGGESTIONS
                }
            )
        }

        OnboardingStep.SUGGESTIONS -> {
            val koin = getKoin()
            val viewModel = koin.get<com.cil.shift.feature.onboarding.presentation.suggestions.HabitSuggestionsViewModel>()
            HabitSuggestionsScreen(
                onComplete = { currentStep = OnboardingStep.PERMISSION },
                viewModel = viewModel
            )
        }

        OnboardingStep.PERMISSION -> {
            NotificationPermissionScreen(
                onComplete = onComplete
            )
        }
    }
}

private fun saveOnboardingData(prefs: OnboardingPreferences, state: WalkthroughState) {
    prefs.setUserName(state.userName.trim())
    prefs.setAgeRange(state.selectedAge?.name)
    prefs.setFocusAreas(state.selectedFocusAreas.map { it.name }.toSet())
    prefs.setDailyRhythm(state.dailyRhythm?.name)
    prefs.setWeeklyGoal(state.weeklyGoal?.name)
    prefs.setStartingHabitCount(state.startingHabitCount?.name)
}
