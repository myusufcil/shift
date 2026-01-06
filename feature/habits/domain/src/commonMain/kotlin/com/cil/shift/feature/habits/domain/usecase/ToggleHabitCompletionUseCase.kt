package com.cil.shift.feature.habits.domain.usecase

import com.cil.shift.core.common.Result
import com.cil.shift.core.common.widget.WidgetNotifier
import com.cil.shift.feature.habits.domain.repository.HabitRepository

/**
 * Use case for toggling habit completion on a specific date.
 *
 * @property repository The habit repository
 */
class ToggleHabitCompletionUseCase(
    private val repository: HabitRepository
) {
    /**
     * Toggles completion status for a habit on a specific date.
     *
     * @param habitId The habit ID
     * @param date The date in YYYY-MM-DD format
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(habitId: String, date: String): Result<Unit> {
        return try {
            repository.toggleCompletion(habitId, date)
            // Notify widgets to update with new data
            WidgetNotifier.notifyWidgetsToUpdate()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
