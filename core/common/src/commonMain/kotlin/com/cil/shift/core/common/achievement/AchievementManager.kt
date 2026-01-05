package com.cil.shift.core.common.achievement

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Interface for achievement preferences storage
 */
interface AchievementPreferences {
    fun getUnlockedAchievements(): Set<String>
    fun unlockAchievement(achievementId: String, unlockedAt: Long)
    fun getProgress(achievementId: String): Int
    fun setProgress(achievementId: String, progress: Int)
    fun getTotalCompletions(): Int
    fun setTotalCompletions(count: Int)
    fun getHabitsCreated(): Int
    fun setHabitsCreated(count: Int)
    fun getAppUsageDays(): Int
    fun setAppUsageDays(days: Int)
    fun getLastUsageDate(): String?
    fun setLastUsageDate(date: String)
    fun getEarlyBirdCount(): Int
    fun setEarlyBirdCount(count: Int)
    fun getNightOwlCount(): Int
    fun setNightOwlCount(count: Int)
}

/**
 * Manages achievement checking, unlocking, and progress tracking
 */
class AchievementManager(
    private val preferences: AchievementPreferences
) {
    private val _unlockedAchievements = MutableStateFlow<Set<String>>(emptySet())
    val unlockedAchievements: StateFlow<Set<String>> = _unlockedAchievements.asStateFlow()

    private val _newlyUnlocked = MutableStateFlow<Achievement?>(null)
    val newlyUnlocked: StateFlow<Achievement?> = _newlyUnlocked.asStateFlow()

    init {
        _unlockedAchievements.value = preferences.getUnlockedAchievements()
    }

    /**
     * Check and unlock achievements based on a new streak value
     */
    fun checkStreakAchievements(streak: Int): List<Achievement> {
        val newUnlocks = mutableListOf<Achievement>()

        Achievements.getByType(AchievementType.STREAK).forEach { achievement ->
            if (streak >= achievement.requiredValue && !isUnlocked(achievement.id)) {
                unlock(achievement)
                newUnlocks.add(achievement)
            }
        }

        return newUnlocks
    }

    /**
     * Record a habit completion and check related achievements
     */
    fun recordCompletion(hour: Int = -1): List<Achievement> {
        val newUnlocks = mutableListOf<Achievement>()

        // Increment total completions
        val newTotal = preferences.getTotalCompletions() + 1
        preferences.setTotalCompletions(newTotal)

        // Check first step achievement
        if (newTotal == 1) {
            Achievements.getByType(AchievementType.FIRST_STEP).forEach { achievement ->
                if (!isUnlocked(achievement.id)) {
                    unlock(achievement)
                    newUnlocks.add(achievement)
                }
            }
        }

        // Check total completion achievements
        Achievements.getByType(AchievementType.TOTAL_COMPLETIONS).forEach { achievement ->
            if (newTotal >= achievement.requiredValue && !isUnlocked(achievement.id)) {
                unlock(achievement)
                newUnlocks.add(achievement)
            }
        }

        // Check early bird (before 8 AM)
        if (hour in 0..7) {
            val earlyBirdCount = preferences.getEarlyBirdCount() + 1
            preferences.setEarlyBirdCount(earlyBirdCount)

            Achievements.getByType(AchievementType.EARLY_BIRD).forEach { achievement ->
                if (earlyBirdCount >= achievement.requiredValue && !isUnlocked(achievement.id)) {
                    unlock(achievement)
                    newUnlocks.add(achievement)
                }
            }
        }

        // Check night owl (after 10 PM)
        if (hour in 22..23) {
            val nightOwlCount = preferences.getNightOwlCount() + 1
            preferences.setNightOwlCount(nightOwlCount)

            Achievements.getByType(AchievementType.NIGHT_OWL).forEach { achievement ->
                if (nightOwlCount >= achievement.requiredValue && !isUnlocked(achievement.id)) {
                    unlock(achievement)
                    newUnlocks.add(achievement)
                }
            }
        }

        return newUnlocks
    }

    /**
     * Record habit creation and check related achievements
     */
    fun recordHabitCreated(): List<Achievement> {
        val newUnlocks = mutableListOf<Achievement>()

        val newCount = preferences.getHabitsCreated() + 1
        preferences.setHabitsCreated(newCount)

        Achievements.getByType(AchievementType.HABITS_CREATED).forEach { achievement ->
            if (newCount >= achievement.requiredValue && !isUnlocked(achievement.id)) {
                unlock(achievement)
                newUnlocks.add(achievement)
            }
        }

        return newUnlocks
    }

    /**
     * Record app usage day and check dedicated user achievements
     */
    fun recordAppUsage(currentDate: String): List<Achievement> {
        val newUnlocks = mutableListOf<Achievement>()

        val lastDate = preferences.getLastUsageDate()
        if (lastDate != currentDate) {
            preferences.setLastUsageDate(currentDate)
            val newDays = preferences.getAppUsageDays() + 1
            preferences.setAppUsageDays(newDays)

            Achievements.getByType(AchievementType.DEDICATED).forEach { achievement ->
                if (newDays >= achievement.requiredValue && !isUnlocked(achievement.id)) {
                    unlock(achievement)
                    newUnlocks.add(achievement)
                }
            }
        }

        return newUnlocks
    }

    /**
     * Check perfect week achievement (7 consecutive days with all habits completed)
     */
    fun checkPerfectWeek(consecutivePerfectDays: Int): List<Achievement> {
        val newUnlocks = mutableListOf<Achievement>()

        if (consecutivePerfectDays >= 7) {
            Achievements.getByType(AchievementType.PERFECT_WEEK).forEach { achievement ->
                if (!isUnlocked(achievement.id)) {
                    unlock(achievement)
                    newUnlocks.add(achievement)
                }
            }
        }

        return newUnlocks
    }

    /**
     * Check perfect month achievement (30 consecutive days with all habits completed)
     */
    fun checkPerfectMonth(consecutivePerfectDays: Int): List<Achievement> {
        val newUnlocks = mutableListOf<Achievement>()

        if (consecutivePerfectDays >= 30) {
            Achievements.getByType(AchievementType.PERFECT_MONTH).forEach { achievement ->
                if (!isUnlocked(achievement.id)) {
                    unlock(achievement)
                    newUnlocks.add(achievement)
                }
            }
        }

        return newUnlocks
    }

    fun isUnlocked(achievementId: String): Boolean {
        return _unlockedAchievements.value.contains(achievementId)
    }

    private fun unlock(achievement: Achievement) {
        val currentTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        preferences.unlockAchievement(achievement.id, currentTime)
        _unlockedAchievements.value = preferences.getUnlockedAchievements()
        _newlyUnlocked.value = achievement
    }

    fun clearNewlyUnlocked() {
        _newlyUnlocked.value = null
    }

    fun getProgress(achievementId: String): Int {
        val achievement = Achievements.getById(achievementId) ?: return 0

        return when (achievement.type) {
            AchievementType.TOTAL_COMPLETIONS, AchievementType.FIRST_STEP -> preferences.getTotalCompletions()
            AchievementType.HABITS_CREATED -> preferences.getHabitsCreated()
            AchievementType.DEDICATED -> preferences.getAppUsageDays()
            AchievementType.EARLY_BIRD -> preferences.getEarlyBirdCount()
            AchievementType.NIGHT_OWL -> preferences.getNightOwlCount()
            else -> preferences.getProgress(achievementId)
        }
    }

    fun getUnlockedCount(): Int = _unlockedAchievements.value.size

    fun getTotalCount(): Int = Achievements.all.size

    fun getAllWithStatus(): List<Pair<Achievement, Boolean>> {
        return Achievements.all.map { achievement ->
            achievement to isUnlocked(achievement.id)
        }
    }
}
