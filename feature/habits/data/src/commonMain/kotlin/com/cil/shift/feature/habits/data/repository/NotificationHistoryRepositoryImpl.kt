package com.cil.shift.feature.habits.data.repository

import com.cil.shift.core.common.currentDateTime
import com.cil.shift.core.common.currentTimestamp
import com.cil.shift.core.common.notification.NotificationHistoryRepository
import com.cil.shift.feature.habits.data.database.HabitsDatabase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class NotificationHistoryRepositoryImpl(
    private val database: HabitsDatabase
) : NotificationHistoryRepository {

    override suspend fun saveNotification(
        habitId: String,
        habitName: String,
        title: String,
        message: String
    ) {
        val notificationId = Uuid.random().toString()
        val timestamp = currentTimestamp()

        database.habitQueries.insertNotification(
            id = notificationId,
            habit_id = habitId,
            habit_name = habitName,
            title = title,
            message = message,
            timestamp = timestamp,
            is_read = 0L,
            action_taken = null
        )
    }

    override suspend fun isHabitCompletedToday(habitId: String): Boolean {
        val today = currentDateTime()
        val dateString = "${today.year}-${today.monthNumber.toString().padStart(2, '0')}-${today.dayOfMonth.toString().padStart(2, '0')}"

        val completion = database.habitQueries.getCompletion(habitId, dateString).executeAsOneOrNull()
        return completion?.is_completed == 1L
    }
}
