package com.cil.shift.core.common.notification

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDateComponents
import platform.UserNotifications.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of NotificationManager using UNUserNotificationCenter.
 */
@OptIn(ExperimentalForeignApi::class)
actual class NotificationManager {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    actual fun scheduleHabitReminder(habitId: String, habitName: String, reminderTime: String) {
        // Parse reminder time (HH:mm format)
        val timeParts = reminderTime.split(":")
        if (timeParts.size != 2) return

        val hour = timeParts[0].toLongOrNull() ?: return
        val minute = timeParts[1].toLongOrNull() ?: return

        // Create notification content
        val content = UNMutableNotificationContent().apply {
            setTitle("Time for $habitName!")
            setBody("Don't forget to complete your habit today.")
            setSound(UNNotificationSound.defaultSound())
        }

        // Create date components for daily trigger at specific time
        val dateComponents = NSDateComponents().apply {
            setHour(hour)
            setMinute(minute)
        }

        // Create calendar trigger (repeats daily)
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = dateComponents,
            repeats = true
        )

        // Create notification request
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "habit_reminder_$habitId",
            content = content,
            trigger = trigger
        )

        // Schedule notification
        center.addNotificationRequest(request) { error ->
            error?.let {
                println("iOS: Failed to schedule notification - ${it.localizedDescription}")
            }
        }
    }

    actual fun cancelHabitReminder(habitId: String) {
        center.removePendingNotificationRequestsWithIdentifiers(
            listOf("habit_reminder_$habitId")
        )
    }

    actual fun cancelAllReminders() {
        center.removeAllPendingNotificationRequests()
    }

    actual suspend fun requestNotificationPermission(): Boolean = suspendCoroutine { continuation ->
        center.requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, error ->
            if (error != null) {
                println("iOS: Permission request error - ${error.localizedDescription}")
                continuation.resume(false)
            } else {
                continuation.resume(granted)
            }
        }
    }

    actual suspend fun hasNotificationPermission(): Boolean = suspendCoroutine { continuation ->
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val granted = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
            continuation.resume(granted)
        }
    }
}
