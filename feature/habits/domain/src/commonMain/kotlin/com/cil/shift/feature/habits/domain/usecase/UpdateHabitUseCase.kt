package com.cil.shift.feature.habits.domain.usecase

import com.cil.shift.core.common.Result
import com.cil.shift.core.common.notification.NotificationManager
import com.cil.shift.feature.habits.domain.model.Habit
import com.cil.shift.feature.habits.domain.repository.HabitRepository

/**
 * Use case for updating an existing habit.
 *
 * @property repository The habit repository
 * @property notificationManager The notification manager for updating reminders
 */
class UpdateHabitUseCase(
    private val repository: HabitRepository,
    private val notificationManager: NotificationManager
) {
    /**
     * Updates a habit and reschedules notification if reminder time changed.
     *
     * @param habit The updated habit
     * @param previousReminderTime The previous reminder time (to detect changes)
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(habit: Habit, previousReminderTime: String? = null): Result<Unit> {
        return try {
            // Validate habit name
            if (habit.name.isBlank()) {
                return Result.Error(Exception("Habit name cannot be empty"))
            }

            repository.updateHabit(habit)

            // Handle notification scheduling
            val newReminderTime = habit.reminderTime

            when {
                // Reminder was removed
                previousReminderTime != null && newReminderTime == null -> {
                    notificationManager.cancelHabitReminder(habit.id)
                }
                // Reminder was added or changed
                newReminderTime != null && newReminderTime != previousReminderTime -> {
                    // Cancel old reminder first (if exists)
                    notificationManager.cancelHabitReminder(habit.id)
                    // Schedule new reminder
                    notificationManager.scheduleHabitReminder(
                        habitId = habit.id,
                        habitName = habit.name,
                        reminderTime = newReminderTime
                    )
                }
                // Habit name changed but reminder time is same - need to update notification content
                newReminderTime != null && newReminderTime == previousReminderTime -> {
                    // Reschedule with updated habit name
                    notificationManager.cancelHabitReminder(habit.id)
                    notificationManager.scheduleHabitReminder(
                        habitId = habit.id,
                        habitName = habit.name,
                        reminderTime = newReminderTime
                    )
                }
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
