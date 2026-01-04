package com.cil.shift.feature.habits.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.cil.shift.core.common.currentTimestamp
import com.cil.shift.feature.habits.data.database.HabitsDatabase
import com.cil.shift.feature.habits.data.mapper.toDomain
import com.cil.shift.feature.habits.data.mapper.toEntity
import com.cil.shift.feature.habits.domain.model.Habit
import com.cil.shift.feature.habits.domain.model.HabitCompletion
import com.cil.shift.feature.habits.domain.repository.HabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * SQLDelight implementation of HabitRepository.
 *
 * @property database The habits database instance
 */
class HabitRepositoryImpl(
    private val database: HabitsDatabase
) : HabitRepository {

    private val habitQueries = database.habitQueries

    override fun getHabits(): Flow<List<Habit>> {
        return habitQueries.getAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getHabitById(id: String): Habit? {
        return withContext(Dispatchers.IO) {
            habitQueries.getById(id).executeAsOneOrNull()?.toDomain()
        }
    }

    override suspend fun createHabit(habit: Habit) {
        withContext(Dispatchers.IO) {
            val params = habit.toEntity()
            habitQueries.insert(
                id = params.id,
                name = params.name,
                icon = params.icon,
                color = params.color,
                frequency_type = params.frequencyType,
                frequency_data = params.frequencyData,
                habit_type = params.habitType,
                target_value = params.targetValue?.toLong(),
                target_unit = params.targetUnit,
                reminder_time = params.reminderTime,
                notes = params.notes,
                sort_order = params.sortOrder,
                created_at = params.createdAt,
                is_archived = params.isArchived
            )
        }
    }

    override suspend fun updateHabit(habit: Habit) {
        // Update is same as create with SQLDelight's INSERT OR REPLACE
        createHabit(habit)
    }

    override suspend fun deleteHabit(id: String) {
        withContext(Dispatchers.IO) {
            habitQueries.deleteById(id)
        }
    }

    override suspend fun toggleCompletion(habitId: String, date: String) {
        withContext(Dispatchers.IO) {
            habitQueries.toggleCompletion(
                habitId, date,
                habitId, date,
                habitId, date,
                habitId, date,
                habitId, date
            )
        }
    }

    override suspend fun getCompletion(habitId: String, date: String): HabitCompletion? {
        return withContext(Dispatchers.IO) {
            habitQueries.getCompletion(habitId, date).executeAsOneOrNull()?.let {
                HabitCompletion(
                    habitId = it.habit_id,
                    date = it.date,
                    isCompleted = it.is_completed > 0,
                    currentValue = it.current_value?.toInt() ?: 0,
                    note = it.note
                )
            }
        }
    }

    override suspend fun getCompletionsForDate(date: String): List<HabitCompletion> {
        return withContext(Dispatchers.IO) {
            habitQueries.getCompletionsForDate(date).executeAsList().map {
                HabitCompletion(
                    habitId = it.habit_id,
                    date = it.date,
                    isCompleted = it.is_completed > 0,
                    currentValue = it.current_value?.toInt() ?: 0,
                    note = it.note
                )
            }
        }
    }

    override suspend fun updateCurrentValue(habitId: String, date: String, value: Int) {
        withContext(Dispatchers.IO) {
            // Get the habit to check target value
            val habit = habitQueries.getById(habitId).executeAsOneOrNull()
            val targetValue = habit?.target_value?.toInt() ?: Int.MAX_VALUE

            // Check if should be marked as completed
            val isCompleted = if (targetValue > 0) {
                value >= targetValue
            } else {
                false
            }

            // Get existing note
            val existingNote = habitQueries.getCompletion(habitId, date).executeAsOneOrNull()?.note

            // Upsert with completion status
            habitQueries.upsertCompletion(
                habit_id = habitId,
                date = date,
                is_completed = if (isCompleted) 1 else 0,
                current_value = value.toLong(),
                note = existingNote
            )
        }
    }

    override fun getCompletions(habitId: String): Flow<List<HabitCompletion>> {
        return habitQueries.getCompletionsForHabit(habitId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    HabitCompletion(
                        habitId = entity.habit_id,
                        date = entity.date,
                        isCompleted = entity.is_completed > 0,
                        currentValue = entity.current_value?.toInt() ?: 0,
                        note = entity.note
                    )
                }
            }
    }

    override suspend fun isCompletedOnDate(habitId: String, date: String): Boolean {
        return withContext(Dispatchers.IO) {
            habitQueries.getCompletion(habitId, date).executeAsOneOrNull()?.is_completed?.let { it > 0 } ?: false
        }
    }

    override suspend fun updateHabitOrders(habitOrders: Map<String, Int>) {
        withContext(Dispatchers.IO) {
            habitOrders.forEach { (habitId, sortOrder) ->
                habitQueries.updateSortOrder(
                    sort_order = sortOrder.toLong(),
                    id = habitId
                )
            }
        }
    }

    override suspend fun updateCompletionNote(habitId: String, date: String, note: String?) {
        withContext(Dispatchers.IO) {
            habitQueries.updateNote(
                note = note,
                habit_id = habitId,
                date = date
            )
        }
    }
}
