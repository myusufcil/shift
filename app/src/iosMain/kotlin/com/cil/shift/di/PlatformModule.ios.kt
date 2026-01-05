package com.cil.shift.di

import com.cil.shift.core.common.achievement.AchievementManager
import com.cil.shift.core.common.achievement.AchievementPreferences
import com.cil.shift.core.common.achievement.IOSAchievementPreferences
import com.cil.shift.core.common.auth.AuthManager
import com.cil.shift.core.common.auth.SocialSignInProvider
import com.cil.shift.core.common.purchase.PurchaseManager
import com.cil.shift.core.common.localization.IOSLanguagePreferences
import com.cil.shift.core.common.localization.LanguagePreferences
import com.cil.shift.core.common.notification.NotificationManager
import com.cil.shift.core.common.onboarding.IOSOnboardingPreferences
import com.cil.shift.core.common.onboarding.OnboardingPreferences
import com.cil.shift.core.common.premium.IOSPremiumPreferences
import com.cil.shift.core.common.premium.PremiumPreferences
import com.cil.shift.core.common.theme.IOSThemePreferences
import com.cil.shift.core.common.theme.ThemePreferences
import com.cil.shift.core.database.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { DatabaseDriverFactory() }
    single<LanguagePreferences> { IOSLanguagePreferences() }
    single<PremiumPreferences> { IOSPremiumPreferences() }
    single<OnboardingPreferences> { IOSOnboardingPreferences() }
    single<ThemePreferences> { IOSThemePreferences() }
    single { NotificationManager() }
    single<AchievementPreferences> { IOSAchievementPreferences() }
    single { AchievementManager(get()) }
    single { AuthManager() }
    single { PurchaseManager() }
    single { SocialSignInProvider() }
}
