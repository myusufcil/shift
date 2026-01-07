package com.cil.shift.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.cil.shift.navigation.GlobalNavigationEvents
import com.cil.shift.feature.habits.presentation.create.MultiStepCreateHabitScreen
import com.cil.shift.feature.habits.presentation.create.CreateEditHabitViewModel
import com.cil.shift.feature.habits.presentation.detail.HabitDetailScreen
import com.cil.shift.feature.habits.presentation.detail.HabitDetailViewModel
import com.cil.shift.feature.settings.presentation.*
import com.cil.shift.feature.settings.presentation.achievements.AchievementsScreen
import com.cil.shift.feature.settings.presentation.auth.LoginScreen
import com.cil.shift.feature.settings.presentation.auth.SignUpScreen
import com.cil.shift.feature.settings.presentation.auth.ForgotPasswordScreen
import com.cil.shift.feature.settings.presentation.premium.PremiumScreen
import com.cil.shift.feature.onboarding.presentation.walkthrough.ProductWalkthroughScreen
import com.cil.shift.core.common.onboarding.OnboardingPreferences
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

data class CreateHabitScreen(val habitId: String? = null) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: CreateEditHabitViewModel = koinInject { parametersOf(habitId) }

        DisposableEffect(Unit) {
            GlobalNavigationEvents.isFullScreenActive = true
            onDispose {
                GlobalNavigationEvents.isFullScreenActive = false
            }
        }

        MultiStepCreateHabitScreen(
            onNavigateBack = { navigator.pop() },
            viewModel = viewModel
        )
    }
}

data class HabitDetailScreen(val habitId: String, val selectedDate: String? = null) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: HabitDetailViewModel = koinInject { parametersOf(habitId, selectedDate) }

        DisposableEffect(Unit) {
            GlobalNavigationEvents.isFullScreenActive = true
            onDispose {
                GlobalNavigationEvents.isFullScreenActive = false
            }
        }

        HabitDetailScreen(
            onNavigateBack = { navigator.pop() },
            onNavigateToEdit = { id -> navigator.push(CreateHabitScreen(id)) },
            viewModel = viewModel
        )
    }
}

object NotificationsScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        DisposableEffect(Unit) {
            GlobalNavigationEvents.isFullScreenActive = true
            onDispose {
                GlobalNavigationEvents.isFullScreenActive = false
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            NotificationsScreen(
                onNavigateBack = { navigator.pop() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

object ProfileScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        ProfileScreen(
            onNavigateBack = { navigator.pop() },
            onNavigateToAbout = { navigator.push(AboutScreenNav) },
            onNavigateToPrivacy = { navigator.push(PrivacyPolicyWebViewNav) },
            onNavigateToTerms = { navigator.push(TermsOfServiceWebViewNav) },
            onNavigateToAchievements = { navigator.push(AchievementsScreenNav) },
            onNavigateToLogin = { navigator.push(LoginScreenNav) },
            onNavigateToPremium = { navigator.push(PremiumScreenNav) }
        )
    }
}

object AboutScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        DisposableEffect(Unit) {
            GlobalNavigationEvents.isFullScreenActive = true
            onDispose {
                GlobalNavigationEvents.isFullScreenActive = false
            }
        }

        AboutScreen(
            onNavigateBack = { navigator.pop() }
        )
    }
}

object PrivacyPolicyWebViewNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        DisposableEffect(Unit) {
            GlobalNavigationEvents.isFullScreenActive = true
            onDispose {
                GlobalNavigationEvents.isFullScreenActive = false
            }
        }

        WebViewScreen(
            title = "Privacy Policy",
            url = "https://myusufcil.github.io/shift/privacy.html",
            onNavigateBack = { navigator.pop() }
        )
    }
}

object TermsOfServiceWebViewNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        DisposableEffect(Unit) {
            GlobalNavigationEvents.isFullScreenActive = true
            onDispose {
                GlobalNavigationEvents.isFullScreenActive = false
            }
        }

        WebViewScreen(
            title = "Terms of Service",
            url = "https://myusufcil.github.io/shift/terms.html",
            onNavigateBack = { navigator.pop() }
        )
    }
}

object AchievementsScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        DisposableEffect(Unit) {
            GlobalNavigationEvents.isFullScreenActive = true
            onDispose {
                GlobalNavigationEvents.isFullScreenActive = false
            }
        }

        AchievementsScreen(
            onNavigateBack = { navigator.pop() }
        )
    }
}

object LoginScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        DisposableEffect(Unit) {
            GlobalNavigationEvents.isFullScreenActive = true
            onDispose {
                GlobalNavigationEvents.isFullScreenActive = false
            }
        }

        LoginScreen(
            onNavigateBack = { navigator.pop() },
            onNavigateToSignUp = { navigator.push(SignUpScreenNav) },
            onNavigateToForgotPassword = { navigator.push(ForgotPasswordScreenNav) },
            onLoginSuccess = { navigator.pop() }
        )
    }
}

object SignUpScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        DisposableEffect(Unit) {
            GlobalNavigationEvents.isFullScreenActive = true
            onDispose {
                GlobalNavigationEvents.isFullScreenActive = false
            }
        }

        SignUpScreen(
            onNavigateBack = { navigator.pop() },
            onSignUpSuccess = {
                // Pop back to profile screen
                navigator.popUntil { it is ProfileScreenNav }
            }
        )
    }
}

object ForgotPasswordScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        DisposableEffect(Unit) {
            GlobalNavigationEvents.isFullScreenActive = true
            onDispose {
                GlobalNavigationEvents.isFullScreenActive = false
            }
        }

        ForgotPasswordScreen(
            onNavigateBack = { navigator.pop() }
        )
    }
}

object PremiumScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        DisposableEffect(Unit) {
            GlobalNavigationEvents.isFullScreenActive = true
            onDispose {
                GlobalNavigationEvents.isFullScreenActive = false
            }
        }

        PremiumScreen(
            onNavigateBack = { navigator.pop() },
            onNavigateToLogin = { navigator.push(LoginScreenNav) }
        )
    }
}

object ProductWalkthroughScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val onboardingPreferences = koinInject<OnboardingPreferences>()

        DisposableEffect(Unit) {
            GlobalNavigationEvents.isFullScreenActive = true
            onDispose {
                GlobalNavigationEvents.isFullScreenActive = false
            }
        }

        ProductWalkthroughScreen(
            onComplete = {
                onboardingPreferences.setProductWalkthroughSeen(true)
                navigator.pop()
            }
        )
    }
}
