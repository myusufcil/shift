package com.cil.shift

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.localization.ProvideLocalization
import com.cil.shift.core.common.onboarding.OnboardingPreferences
import com.cil.shift.core.designsystem.theme.ShiftTheme
import com.cil.shift.navigation.BottomNavigationBar
import com.cil.shift.navigation.GlobalNavigationEvents
import com.cil.shift.navigation.HomeTab
import com.cil.shift.onboarding.OnboardingFlow
import com.cil.shift.ui.ConfigureSystemBars
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
fun App() {
    // Initialize Koin
    KoinContext {
        val localizationManager = koinInject<LocalizationManager>()
        val onboardingPreferences = koinInject<OnboardingPreferences>()

        ProvideLocalization(localizationManager = localizationManager) {
            ShiftTheme {
                // Configure system UI (status bar, navigation bar)
                ConfigureSystemBars()

                var showOnboarding by remember { mutableStateOf(!onboardingPreferences.isOnboardingCompleted()) }

                if (showOnboarding) {
                    OnboardingFlow(
                        onComplete = {
                            onboardingPreferences.setOnboardingCompleted(true)
                            showOnboarding = false
                        }
                    )
                } else {
                    MainApp()
                }
            }
        }
    }
}

@Composable
private fun MainApp() {
    val isFullScreen = GlobalNavigationEvents.isFullScreenActive

    TabNavigator(HomeTab) { tabNavigator ->
        Scaffold(
            bottomBar = {
                if (!isFullScreen) {
                    BottomNavigationBar(
                        tabNavigator = tabNavigator,
                        onAddClick = {
                            // Switch to HomeTab and trigger navigation
                            tabNavigator.current = HomeTab
                            GlobalNavigationEvents.navigateToCreateHabit()
                        }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CurrentTab()
            }
        }
    }
}
