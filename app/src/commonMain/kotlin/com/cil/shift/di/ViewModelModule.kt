package com.cil.shift.di

import com.cil.shift.feature.habits.presentation.create.CreateEditHabitViewModel
import com.cil.shift.feature.habits.presentation.detail.HabitDetailViewModel
import com.cil.shift.feature.habits.presentation.home.HomeViewModel
import com.cil.shift.feature.onboarding.presentation.walkthrough.WalkthroughViewModel
import com.cil.shift.feature.onboarding.presentation.suggestions.HabitSuggestionsViewModel
import com.cil.shift.feature.settings.presentation.SettingsViewModel
import com.cil.shift.feature.settings.presentation.NotificationsViewModel
import com.cil.shift.feature.statistics.presentation.StatisticsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val viewModelModule = module {
    // Onboarding
    factoryOf(::WalkthroughViewModel)
    factory { HabitSuggestionsViewModel(get()) }

    // Habits
    factory { HomeViewModel(get(), get(), get(), get(), get(), get()) }
    factory { (habitId: String?) -> CreateEditHabitViewModel(habitId, get(), get(), get()) }
    factory { (habitId: String, selectedDate: String?) -> HabitDetailViewModel(habitId, selectedDate, get(), get(), get(), get()) }

    // Statistics
    factory { StatisticsViewModel(get(), get(), get()) }

    // Settings
    factory { SettingsViewModel(get(), get(), get()) }
    factory { NotificationsViewModel(get()) }
}
