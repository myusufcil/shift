package com.cil.shift.core.common.notification

interface NotificationHistoryRepository {
    suspend fun saveNotification(
        habitId: String,
        habitName: String,
        title: String,
        message: String
    )

    suspend fun isHabitCompletedToday(habitId: String): Boolean
}
