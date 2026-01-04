package com.cil.shift.feature.habits.domain.repository

import com.cil.shift.feature.habits.domain.model.Habit
import com.cil.shift.feature.habits.domain.model.HabitCompletion
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for habit-related data operations.
 * Implementations are provided in the data layer.
 */
interface HabitRepository {
    /**
     * Observes all active (non-archived) habits.
     *
     * @return Flow of habit list that updates when data changes
     */
    fun getHabits(): Flow<List<Habit>>

    /**
     * Gets a specific habit by ID.
     *
     * @param id The habit ID
     * @return The habit if found, null otherwise
     */
    suspend fun getHabitById(id: String): Habit?

    /**
     * Creates a new habit.
     *
     * @param habit The habit to create
     */
    suspend fun createHabit(habit: Habit)

    /**
     * Updates an existing habit.
     *
     * @param habit The habit with updated values
     */
    suspend fun updateHabit(habit: Habit)

    /**
     * Deletes a habit (soft delete - marks as archived).
     *
     * @param id The habit ID to delete
     */
    suspend fun deleteHabit(id: String)

    /**
     * Toggles habit completion for a specific date.
     * If already completed, removes the completion. If not completed, adds completion.
     *
     * @param habitId The habit ID
     * @param date The date in YYYY-MM-DD format
     */
    suspend fun toggleCompletion(habitId: String, date: String)

    /**
     * Observes all completions for a specific habit.
     *
     * @param habitId The habit ID
     * @return Flow of completion records
     */
    fun getCompletions(habitId: String): Flow<List<HabitCompletion>>

    /**
     * Checks if a habit is completed on a specific date.
     *
     * @param habitId The habit ID
     * @param date The date in YYYY-MM-DD format
     * @return True if completed, false otherwise
     */
    suspend fun isCompletedOnDate(habitId: String, date: String): Boolean

    /**
     * Gets completion data for a specific habit on a specific date.
     *
     * @param habitId The habit ID
     * @param date The date in YYYY-MM-DD format
     * @return HabitCompletion if exists, null otherwise
     */
    suspend fun getCompletion(habitId: String, date: String): HabitCompletion?

    /**
     * Gets all completions for a specific date.
     *
     * @param date The date in YYYY-MM-DD format
     * @return List of completions for that date
     */
    suspend fun getCompletionsForDate(date: String): List<HabitCompletion>

    /**
     * Updates the current value for measurable/timer habits.
     *
     * @param habitId The habit ID
     * @param date The date in YYYY-MM-DD format
     * @param value The new value
     */
    suspend fun updateCurrentValue(habitId: String, date: String, value: Int)

    /**
     * Updates the sort order of multiple habits.
     *
     * @param habitOrders Map of habit ID to sort order
     */
    suspend fun updateHabitOrders(habitOrders: Map<String, Int>)

    /**
     * Updates the note for a habit completion on a specific date.
     *
     * @param habitId The habit ID
     * @param date The date in YYYY-MM-DD format
     * @param note The note text
     */
    suspend fun updateCompletionNote(habitId: String, date: String, note: String?)
}
