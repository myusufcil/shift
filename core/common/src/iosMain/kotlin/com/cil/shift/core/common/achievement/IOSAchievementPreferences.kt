package com.cil.shift.core.common.achievement

import platform.Foundation.NSUserDefaults

class IOSAchievementPreferences : AchievementPreferences {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getUnlockedAchievements(): Set<String> {
        val array = defaults.arrayForKey(KEY_UNLOCKED) ?: return emptySet()
        return array.mapNotNull { it as? String }.toSet()
    }

    override fun unlockAchievement(achievementId: String, unlockedAt: Long) {
        val current = getUnlockedAchievements().toMutableSet()
        current.add(achievementId)
        defaults.setObject(current.toList(), KEY_UNLOCKED)
        defaults.setDouble(unlockedAt.toDouble(), "${KEY_UNLOCKED_AT}_$achievementId")
    }

    override fun getProgress(achievementId: String): Int {
        return defaults.integerForKey("${KEY_PROGRESS}_$achievementId").toInt()
    }

    override fun setProgress(achievementId: String, progress: Int) {
        defaults.setInteger(progress.toLong(), "${KEY_PROGRESS}_$achievementId")
    }

    override fun getTotalCompletions(): Int {
        return defaults.integerForKey(KEY_TOTAL_COMPLETIONS).toInt()
    }

    override fun setTotalCompletions(count: Int) {
        defaults.setInteger(count.toLong(), KEY_TOTAL_COMPLETIONS)
    }

    override fun getHabitsCreated(): Int {
        return defaults.integerForKey(KEY_HABITS_CREATED).toInt()
    }

    override fun setHabitsCreated(count: Int) {
        defaults.setInteger(count.toLong(), KEY_HABITS_CREATED)
    }

    override fun getAppUsageDays(): Int {
        return defaults.integerForKey(KEY_APP_USAGE_DAYS).toInt()
    }

    override fun setAppUsageDays(days: Int) {
        defaults.setInteger(days.toLong(), KEY_APP_USAGE_DAYS)
    }

    override fun getLastUsageDate(): String? {
        return defaults.stringForKey(KEY_LAST_USAGE_DATE)
    }

    override fun setLastUsageDate(date: String) {
        defaults.setObject(date, KEY_LAST_USAGE_DATE)
    }

    override fun getEarlyBirdCount(): Int {
        return defaults.integerForKey(KEY_EARLY_BIRD_COUNT).toInt()
    }

    override fun setEarlyBirdCount(count: Int) {
        defaults.setInteger(count.toLong(), KEY_EARLY_BIRD_COUNT)
    }

    override fun getNightOwlCount(): Int {
        return defaults.integerForKey(KEY_NIGHT_OWL_COUNT).toInt()
    }

    override fun setNightOwlCount(count: Int) {
        defaults.setInteger(count.toLong(), KEY_NIGHT_OWL_COUNT)
    }

    companion object {
        private const val KEY_UNLOCKED = "shift_unlocked_achievements"
        private const val KEY_UNLOCKED_AT = "shift_unlocked_at"
        private const val KEY_PROGRESS = "shift_progress"
        private const val KEY_TOTAL_COMPLETIONS = "shift_total_completions"
        private const val KEY_HABITS_CREATED = "shift_habits_created"
        private const val KEY_APP_USAGE_DAYS = "shift_app_usage_days"
        private const val KEY_LAST_USAGE_DATE = "shift_last_usage_date"
        private const val KEY_EARLY_BIRD_COUNT = "shift_early_bird_count"
        private const val KEY_NIGHT_OWL_COUNT = "shift_night_owl_count"
    }
}
