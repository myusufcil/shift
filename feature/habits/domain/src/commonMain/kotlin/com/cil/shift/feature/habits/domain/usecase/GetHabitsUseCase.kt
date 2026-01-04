package com.cil.shift.feature.habits.domain.usecase

import com.cil.shift.feature.habits.domain.model.Habit
import com.cil.shift.feature.habits.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing all active habits.
 *
 * @property repository The habit repository
 */
class GetHabitsUseCase(
    private val repository: HabitRepository
) {
    /**
     * Observes all active habits.
     *
     * @return Flow of habit list
     */
    operator fun invoke(): Flow<List<Habit>> {
        return repository.getHabits()
    }
}
