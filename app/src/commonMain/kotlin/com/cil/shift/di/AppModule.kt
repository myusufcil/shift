package com.cil.shift.di

import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.notification.NotificationHistoryRepository
import com.cil.shift.core.common.premium.PremiumManager
import com.cil.shift.core.common.theme.ThemeManager
import com.cil.shift.feature.habits.data.repository.HabitRepositoryImpl
import com.cil.shift.feature.habits.data.repository.NotificationHistoryRepositoryImpl
import com.cil.shift.feature.habits.domain.repository.HabitRepository
import com.cil.shift.feature.habits.domain.usecase.CreateHabitUseCase
import com.cil.shift.feature.habits.domain.usecase.DeleteHabitUseCase
import com.cil.shift.feature.habits.domain.usecase.GetHabitsUseCase
import com.cil.shift.feature.habits.domain.usecase.ToggleHabitCompletionUseCase
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect fun platformModule(): Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            platformModule(),
            dataModule,
            domainModule,
            viewModelModule
        )
    }
}

val dataModule = module {
    // Database
    single {
        val driverFactory = get<com.cil.shift.core.database.DatabaseDriverFactory>()
        val driver = driverFactory.createDriverWithSchema(
            com.cil.shift.feature.habits.data.database.HabitsDatabase.Schema,
            "shift_v4.db" // Updated with NotificationHistory table
        )
        com.cil.shift.feature.habits.data.database.HabitsDatabase(driver)
    }

    // Repositories
    single<HabitRepository> { HabitRepositoryImpl(get()) }
    single<NotificationHistoryRepository> { NotificationHistoryRepositoryImpl(get()) }
}

val domainModule = module {
    // Habit Use Cases
    singleOf(::GetHabitsUseCase)
    singleOf(::CreateHabitUseCase)
    singleOf(::DeleteHabitUseCase)
    singleOf(::ToggleHabitCompletionUseCase)

    // Localization
    single { LocalizationManager(get()) }

    // Theme
    single { ThemeManager(get()) }

    // Premium
    single { PremiumManager(get()) }
}
