package com.cil.shift.core.common.notification

/**
 * Platform-specific notification manager for scheduling and managing habit reminders.
 * Implementations handle platform-specific notification APIs (Android WorkManager, iOS UserNotifications).
 */
expect class NotificationManager {
    /**
     * Schedules a daily reminder notification for a habit.
     *
     * @param habitId Unique identifier for the habit
     * @param habitName Name of the habit to display in the notification
     * @param reminderTime Time in HH:mm format (24-hour) when notification should be shown
     */
    fun scheduleHabitReminder(habitId: String, habitName: String, reminderTime: String)

    /**
     * Cancels a scheduled reminder for a specific habit.
     *
     * @param habitId Unique identifier for the habit
     */
    fun cancelHabitReminder(habitId: String)

    /**
     * Cancels all scheduled reminders.
     */
    fun cancelAllReminders()

    /**
     * Requests notification permission from the user (Android 13+, iOS).
     * Implementations should handle platform-specific permission flows.
     */
    suspend fun requestNotificationPermission(): Boolean

    /**
     * Checks if notification permission has been granted.
     */
    suspend fun hasNotificationPermission(): Boolean
}
