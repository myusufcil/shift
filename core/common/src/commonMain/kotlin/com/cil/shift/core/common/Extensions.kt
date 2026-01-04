package com.cil.shift.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.*

// Flow extensions

/**
 * Wraps a Flow<T> into a Flow<Result<T>> with loading and error handling.
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> { Result.Success(it) }
        .onStart { emit(Result.Loading) }
        .catch { emit(Result.Error(it as? Exception ?: Exception(it.message))) }
}

// String extensions

/**
 * Parses ISO-8601 string to LocalDate.
 */
fun String.toLocalDate(): LocalDate = LocalDate.parse(this)

/**
 * Parses ISO-8601 string to LocalDateTime.
 */
fun String.toLocalDateTime(): LocalDateTime = LocalDateTime.parse(this)

// LocalDate extensions

/**
 * Converts LocalDate to ISO-8601 string format (YYYY-MM-DD).
 */
fun LocalDate.toIsoString(): String = this.toString()

/**
 * Checks if this date is today.
 */
fun LocalDate.isToday(): Boolean {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return this == today
}

/**
 * Formats date as "DD/MM/YYYY".
 */
fun LocalDate.formatShort(): String {
    return "${dayOfMonth.toString().padStart(2, '0')}/${monthNumber.toString().padStart(2, '0')}/$year"
}

/**
 * Formats date as "DD Month YYYY" (e.g., "15 January 2024").
 */
fun LocalDate.formatLong(): String {
    val monthName = month.name.lowercase().replaceFirstChar { it.uppercase() }
    return "$dayOfMonth $monthName $year"
}

// LocalDateTime extensions

/**
 * Converts LocalDateTime to ISO-8601 string format.
 */
fun LocalDateTime.toIsoString(): String = this.toString()

// Current date/time helpers

/**
 * Returns current date in the system's default timezone.
 */
fun currentDate(): LocalDate {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}

/**
 * Returns current date and time in the system's default timezone.
 */
fun currentDateTime(): LocalDateTime {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}

/**
 * Returns current timestamp in milliseconds since epoch.
 */
fun currentTimestamp(): Long {
    return Clock.System.now().toEpochMilliseconds()
}
