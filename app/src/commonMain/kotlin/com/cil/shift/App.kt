package com.cil.shift

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.cil.shift.core.common.localization.Language
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.localization.ProvideLocalization
import com.cil.shift.core.common.notification.NotificationHistoryRepository
import com.cil.shift.core.common.notification.NotificationManager
import com.cil.shift.core.common.onboarding.OnboardingPreferences
import com.cil.shift.core.common.purchase.PurchaseManager
import com.cil.shift.core.common.theme.ProvideTheme
import com.cil.shift.core.common.theme.ThemeManager
import kotlinx.coroutines.launch
import com.cil.shift.core.designsystem.components.CoachMarkOverlay
import com.cil.shift.core.designsystem.components.CoachMarkStrings
import com.cil.shift.core.designsystem.components.rememberCoachMarkController
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
        val themeManager = koinInject<ThemeManager>()
        val notificationManager = koinInject<NotificationManager>()
        val notificationHistoryRepository = koinInject<NotificationHistoryRepository>()
        val purchaseManager = koinInject<PurchaseManager>()

        // Initialize RevenueCat and sync notifications
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            // Initialize RevenueCat purchases
            scope.launch {
                try {
                    purchaseManager.initialize()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Sync delivered notifications to history (primarily for iOS)
            scope.launch {
                try {
                    val deliveredNotifications = notificationManager.getDeliveredNotifications()
                    deliveredNotifications.forEach { notification ->
                        notificationHistoryRepository.saveNotification(
                            habitId = notification.habitId,
                            habitName = notification.habitName,
                            title = notification.title,
                            message = notification.message
                        )
                    }
                    if (deliveredNotifications.isNotEmpty()) {
                        notificationManager.clearDeliveredNotifications()
                    }
                } catch (e: Exception) {
                    // Silently handle errors during notification sync
                    e.printStackTrace()
                }
            }
        }

        val currentTheme by themeManager.currentTheme.collectAsState()
        val isSystemInDarkTheme = isSystemInDarkTheme()
        // Directly derive isDarkTheme from currentTheme state to ensure reactivity
        val isDarkTheme = when (currentTheme) {
            com.cil.shift.core.common.theme.AppTheme.DARK -> true
            com.cil.shift.core.common.theme.AppTheme.LIGHT -> false
            com.cil.shift.core.common.theme.AppTheme.SYSTEM -> isSystemInDarkTheme
        }

        ProvideLocalization(localizationManager = localizationManager) {
            ProvideTheme(themeManager = themeManager) {
                ShiftTheme(darkTheme = isDarkTheme) {
                    // Configure system UI (status bar, navigation bar)
                    ConfigureSystemBars(isDarkTheme = isDarkTheme)

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
}

@Composable
private fun MainApp() {
    val isFullScreen = GlobalNavigationEvents.isFullScreenActive
    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()

    // Create and store global coach mark controller
    val coachMarkController = rememberCoachMarkController()
    LaunchedEffect(coachMarkController) {
        GlobalNavigationEvents.coachMarkController = coachMarkController
    }

    val coachMarkStrings = remember(currentLanguage) {
        getCoachMarkStrings(currentLanguage)
    }

    Box(modifier = Modifier.fillMaxSize()) {
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

        // Coach Mark Overlay - rendered above everything including bottom nav
        CoachMarkOverlay(
            controller = coachMarkController,
            strings = coachMarkStrings,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// Coach Mark Strings helper
private fun getCoachMarkStrings(language: Language): CoachMarkStrings {
    return when (language) {
        Language.TURKISH -> CoachMarkStrings(
            skip = "Atla",
            next = "Sonraki",
            back = "Geri",
            finish = "Bitir"
        )
        Language.GERMAN -> CoachMarkStrings(
            skip = "Überspringen",
            next = "Weiter",
            back = "Zurück",
            finish = "Fertig"
        )
        Language.FRENCH -> CoachMarkStrings(
            skip = "Passer",
            next = "Suivant",
            back = "Retour",
            finish = "Terminer"
        )
        Language.SPANISH -> CoachMarkStrings(
            skip = "Saltar",
            next = "Siguiente",
            back = "Atrás",
            finish = "Terminar"
        )
        Language.PORTUGUESE -> CoachMarkStrings(
            skip = "Pular",
            next = "Próximo",
            back = "Voltar",
            finish = "Concluir"
        )
        Language.ARABIC -> CoachMarkStrings(
            skip = "تخطي",
            next = "التالي",
            back = "رجوع",
            finish = "إنهاء"
        )
        Language.RUSSIAN -> CoachMarkStrings(
            skip = "Пропустить",
            next = "Далее",
            back = "Назад",
            finish = "Готово"
        )
        Language.HINDI -> CoachMarkStrings(
            skip = "छोड़ें",
            next = "अगला",
            back = "पीछे",
            finish = "समाप्त"
        )
        Language.JAPANESE -> CoachMarkStrings(
            skip = "スキップ",
            next = "次へ",
            back = "戻る",
            finish = "完了"
        )
        Language.CHINESE -> CoachMarkStrings(
            skip = "跳过",
            next = "下一步",
            back = "返回",
            finish = "完成"
        )
        Language.ENGLISH -> CoachMarkStrings(
            skip = "Skip",
            next = "Next",
            back = "Back",
            finish = "Finish"
        )
    }
}
