package com.cil.shift.di

import com.cil.shift.core.common.achievement.AchievementManager
import com.cil.shift.core.common.achievement.AchievementPreferences
import com.cil.shift.core.common.achievement.AndroidAchievementPreferences
import com.cil.shift.core.common.auth.AuthManager
import com.cil.shift.core.common.purchase.PurchaseManager
import com.cil.shift.core.common.localization.AndroidLanguagePreferences
import com.cil.shift.core.common.localization.LanguagePreferences
import com.cil.shift.core.common.notification.NotificationManager
import com.cil.shift.core.common.onboarding.AndroidOnboardingPreferences
import com.cil.shift.core.common.onboarding.OnboardingPreferences
import com.cil.shift.core.common.premium.AndroidPremiumPreferences
import com.cil.shift.core.common.premium.PremiumPreferences
import com.cil.shift.core.common.theme.AndroidThemePreferences
import com.cil.shift.core.common.theme.ThemePreferences
import com.cil.shift.core.database.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { DatabaseDriverFactory(androidContext()) }
    single<LanguagePreferences> { AndroidLanguagePreferences(androidContext()) }
    single<PremiumPreferences> { AndroidPremiumPreferences(androidContext()) }
    single<OnboardingPreferences> { AndroidOnboardingPreferences(androidContext()) }
    single<ThemePreferences> { AndroidThemePreferences(androidContext()) }
    single { NotificationManager(androidContext()) }
    single<AchievementPreferences> { AndroidAchievementPreferences(androidContext()) }
    single { AchievementManager(get()) }
    single { AuthManager() }
    single { PurchaseManager() }
}
