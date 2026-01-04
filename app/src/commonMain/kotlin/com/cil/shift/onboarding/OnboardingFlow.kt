package com.cil.shift.onboarding

import androidx.compose.runtime.*
import com.cil.shift.feature.onboarding.presentation.permission.NotificationPermissionScreen
import com.cil.shift.feature.onboarding.presentation.walkthrough.WalkthroughScreen
import com.cil.shift.feature.onboarding.presentation.suggestions.HabitSuggestionsScreen
import org.koin.compose.getKoin

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

    when (currentStep) {
        OnboardingStep.WALKTHROUGH -> {
            WalkthroughScreen(
                onComplete = { currentStep = OnboardingStep.SUGGESTIONS }
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
