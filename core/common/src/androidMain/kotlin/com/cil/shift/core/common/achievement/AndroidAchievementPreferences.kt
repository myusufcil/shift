package com.cil.shift.core.common.achievement

import android.content.Context
import android.content.SharedPreferences

class AndroidAchievementPreferences(context: Context) : AchievementPreferences {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "shift_achievements",
        Context.MODE_PRIVATE
    )

    override fun getUnlockedAchievements(): Set<String> {
        return prefs.getStringSet(KEY_UNLOCKED, emptySet()) ?: emptySet()
    }

    override fun unlockAchievement(achievementId: String, unlockedAt: Long) {
        val current = getUnlockedAchievements().toMutableSet()
        current.add(achievementId)
        prefs.edit()
            .putStringSet(KEY_UNLOCKED, current)
            .putLong("${KEY_UNLOCKED_AT}_$achievementId", unlockedAt)
            .apply()
    }

    override fun getProgress(achievementId: String): Int {
        return prefs.getInt("${KEY_PROGRESS}_$achievementId", 0)
    }

    override fun setProgress(achievementId: String, progress: Int) {
        prefs.edit().putInt("${KEY_PROGRESS}_$achievementId", progress).apply()
    }

    override fun getTotalCompletions(): Int {
        return prefs.getInt(KEY_TOTAL_COMPLETIONS, 0)
    }

    override fun setTotalCompletions(count: Int) {
        prefs.edit().putInt(KEY_TOTAL_COMPLETIONS, count).apply()
    }

    override fun getHabitsCreated(): Int {
        return prefs.getInt(KEY_HABITS_CREATED, 0)
    }

    override fun setHabitsCreated(count: Int) {
        prefs.edit().putInt(KEY_HABITS_CREATED, count).apply()
    }

    override fun getAppUsageDays(): Int {
        return prefs.getInt(KEY_APP_USAGE_DAYS, 0)
    }

    override fun setAppUsageDays(days: Int) {
        prefs.edit().putInt(KEY_APP_USAGE_DAYS, days).apply()
    }

    override fun getLastUsageDate(): String? {
        return prefs.getString(KEY_LAST_USAGE_DATE, null)
    }

    override fun setLastUsageDate(date: String) {
        prefs.edit().putString(KEY_LAST_USAGE_DATE, date).apply()
    }

    override fun getEarlyBirdCount(): Int {
        return prefs.getInt(KEY_EARLY_BIRD_COUNT, 0)
    }

    override fun setEarlyBirdCount(count: Int) {
        prefs.edit().putInt(KEY_EARLY_BIRD_COUNT, count).apply()
    }

    override fun getNightOwlCount(): Int {
        return prefs.getInt(KEY_NIGHT_OWL_COUNT, 0)
    }

    override fun setNightOwlCount(count: Int) {
        prefs.edit().putInt(KEY_NIGHT_OWL_COUNT, count).apply()
    }

    companion object {
        private const val KEY_UNLOCKED = "unlocked_achievements"
        private const val KEY_UNLOCKED_AT = "unlocked_at"
        private const val KEY_PROGRESS = "progress"
        private const val KEY_TOTAL_COMPLETIONS = "total_completions"
        private const val KEY_HABITS_CREATED = "habits_created"
        private const val KEY_APP_USAGE_DAYS = "app_usage_days"
        private const val KEY_LAST_USAGE_DATE = "last_usage_date"
        private const val KEY_EARLY_BIRD_COUNT = "early_bird_count"
        private const val KEY_NIGHT_OWL_COUNT = "night_owl_count"
    }
}
