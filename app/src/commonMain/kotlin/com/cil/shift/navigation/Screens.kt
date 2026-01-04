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
            onNavigateToPrivacy = { navigator.push(PrivacyPolicyScreenNav) },
            onNavigateToTerms = { navigator.push(TermsOfServiceScreenNav) }
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

object PrivacyPolicyScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        DisposableEffect(Unit) {
            GlobalNavigationEvents.isFullScreenActive = true
            onDispose {
                GlobalNavigationEvents.isFullScreenActive = false
            }
        }

        PrivacyPolicyScreen(
            onNavigateBack = { navigator.pop() }
        )
    }
}

object TermsOfServiceScreenNav : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        DisposableEffect(Unit) {
            GlobalNavigationEvents.isFullScreenActive = true
            onDispose {
                GlobalNavigationEvents.isFullScreenActive = false
            }
        }

        TermsOfServiceScreen(
            onNavigateBack = { navigator.pop() }
        )
    }
}
