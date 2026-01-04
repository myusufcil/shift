package com.cil.shift.core.common.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.Calendar
import java.util.concurrent.TimeUnit

actual class NotificationManager(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "habit_reminders"
        private const val CHANNEL_NAME = "Habit Reminders"
        private const val CHANNEL_DESCRIPTION = "Daily reminders for your habits"
        private const val NOTIFICATION_ICON = android.R.drawable.ic_dialog_info
    }

    init {
        createNotificationChannel()
    }

    /**
     * Creates notification channel for Android 8.0+ (API 26+).
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = AndroidNotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    actual fun scheduleHabitReminder(habitId: String, habitName: String, reminderTime: String) {
        // Parse reminder time (HH:mm format)
        val timeParts = reminderTime.split(":")
        if (timeParts.size != 2) return

        val hour = timeParts[0].toIntOrNull() ?: return
        val minute = timeParts[1].toIntOrNull() ?: return

        // Calculate initial delay until the reminder time
        val currentTime = Calendar.getInstance()
        val reminderCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If time has passed today, schedule for tomorrow
            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val initialDelay = reminderCalendar.timeInMillis - currentTime.timeInMillis

        // Create input data for the worker
        val inputData = workDataOf(
            "habitId" to habitId,
            "habitName" to habitName
        )

        // Create periodic work request (runs daily)
        val workRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("habit_reminder_$habitId")
            .build()

        // Schedule the work with unique name to prevent duplicates
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "habit_reminder_$habitId",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    actual fun cancelHabitReminder(habitId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("habit_reminder_$habitId")
    }

    actual fun cancelAllReminders() {
        WorkManager.getInstance(context).cancelAllWorkByTag("habit_reminder")
    }

    actual suspend fun requestNotificationPermission(): Boolean {
        // On Android 13+ (API 33+), runtime permission is required
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Permission should be requested from the Activity, not here
            // This just checks if permission is granted
            hasNotificationPermission()
        } else {
            // Pre-Android 13, notifications are allowed by default
            true
        }
    }

    actual suspend fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Saves notification to history database.
     */
    fun saveNotificationToHistory(habitId: String, habitName: String, title: String, message: String) {
        // TODO: Save to database using HabitRepository
        // This will be called after showing notification
    }

    /**
     * Shows a notification immediately (used by the Worker).
     */
    fun showNotification(habitId: String, habitName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        // Create intent to open the app when notification is tapped
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("habitId", habitId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(NOTIFICATION_ICON)
            .setContentTitle("Time for $habitName!")
            .setContentText("Don't forget to complete your habit today.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(habitId.hashCode(), notification)
    }
}

/**
 * Worker that displays the habit reminder notification.
 */
class HabitReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params), KoinComponent {

    override fun doWork(): Result {
        val habitId = inputData.getString("habitId") ?: return Result.failure()
        val habitName = inputData.getString("habitName") ?: return Result.failure()

        // Smart notifications: Check if habit is already completed
        try {
            val repository = get<NotificationHistoryRepository>()

            val isCompleted = kotlinx.coroutines.runBlocking {
                repository.isHabitCompletedToday(habitId)
            }

            // Skip notification if habit is already completed
            if (isCompleted) {
                return Result.success()
            }

            // Show notification
            val notificationManager = NotificationManager(applicationContext)
            notificationManager.showNotification(habitId, habitName)

            // Save notification to history
            val title = "Time for $habitName!"
            val message = "Don't forget to complete your habit today."

            kotlinx.coroutines.runBlocking {
                repository.saveNotification(habitId, habitName, title, message)
            }
        } catch (e: Exception) {
            // Log error but still show notification on error
            e.printStackTrace()
            val notificationManager = NotificationManager(applicationContext)
            notificationManager.showNotification(habitId, habitName)
        }

        return Result.success()
    }
}
