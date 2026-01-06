package com.cil.shift.feature.habits.domain.usecase

import com.cil.shift.core.common.Result
import com.cil.shift.core.common.currentDate
import com.cil.shift.core.common.currentTimestamp
import com.cil.shift.core.common.notification.NotificationManager
import com.cil.shift.feature.habits.domain.model.Habit
import com.cil.shift.feature.habits.domain.repository.HabitRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Use case for creating a new habit.
 *
 * @property repository The habit repository
 * @property notificationManager The notification manager for scheduling reminders
 */
class CreateHabitUseCase(
    private val repository: HabitRepository,
    private val notificationManager: NotificationManager
) {
    /**
     * Creates a new habit with validation.
     *
     * @param habit The habit to create (ID will be generated if empty)
     * @return Result indicating success or failure
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(habit: Habit): Result<Unit> {
        return try {
            // Validate habit name
            if (habit.name.isBlank()) {
                return Result.Error(Exception("Habit name cannot be empty"))
            }

            // Generate ID if not provided
            val habitWithId = if (habit.id.isEmpty()) {
                habit.copy(
                    id = Uuid.random().toString(),
                    createdAt = currentTimestamp()
                )
            } else {
                habit
            }

            repository.createHabit(habitWithId)

            // Create initial completion record for today so the habit appears on the home screen
            val today = currentDate().toString()
            repository.updateCurrentValue(habitWithId.id, today, 0)

            // Schedule notification reminder if reminder time is set
            habitWithId.reminderTime?.let { reminderTime ->
                // Request permission first (will be no-op if already granted)
                val hasPermission = notificationManager.hasNotificationPermission()
                if (!hasPermission) {
                    notificationManager.requestNotificationPermission()
                }

                notificationManager.scheduleHabitReminder(
                    habitId = habitWithId.id,
                    habitName = habitWithId.name,
                    reminderTime = reminderTime
                )
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
