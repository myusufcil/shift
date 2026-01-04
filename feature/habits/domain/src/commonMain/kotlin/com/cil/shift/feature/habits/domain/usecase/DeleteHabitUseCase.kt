package com.cil.shift.feature.habits.domain.usecase

import com.cil.shift.core.common.Result
import com.cil.shift.core.common.notification.NotificationManager
import com.cil.shift.feature.habits.domain.repository.HabitRepository

/**
 * Use case for deleting a habit.
 *
 * @property repository The habit repository
 * @property notificationManager The notification manager for canceling reminders
 */
class DeleteHabitUseCase(
    private val repository: HabitRepository,
    private val notificationManager: NotificationManager
) {
    /**
     * Deletes a habit (soft delete) and cancels its notification reminder.
     *
     * @param habitId The habit ID to delete
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(habitId: String): Result<Unit> {
        return try {
            repository.deleteHabit(habitId)

            // Cancel notification reminder for this habit
            notificationManager.cancelHabitReminder(habitId)

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
