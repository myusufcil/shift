package com.cil.shift.core.common.honey

import android.content.Context
import android.content.SharedPreferences

/**
 * Android implementation of HoneyPreferences using SharedPreferences.
 */
class AndroidHoneyPreferences(context: Context) : HoneyPreferences {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "shift_honey_prefs"
        private const val KEY_BALANCE = "honey_balance"
        private const val KEY_TOTAL_EARNED = "honey_total_earned"
        private const val KEY_TOTAL_SPENT = "honey_total_spent"
        private const val KEY_LAST_DAILY_REWARD = "honey_last_daily"
        private const val KEY_FIRST_OPEN_DATE = "honey_first_open"
        private const val KEY_LOW_WARNING_SHOWN = "honey_low_warning_shown"
        private const val KEY_UNLOCKED_FEATURES = "honey_unlocked_features"
        private const val KEY_TOTAL_HABITS_CREATED = "honey_total_habits_created"

        private const val DEFAULT_BALANCE = 50
    }

    override fun getBalance(): Int {
        return prefs.getInt(KEY_BALANCE, DEFAULT_BALANCE)
    }

    override fun setBalance(balance: Int) {
        prefs.edit().putInt(KEY_BALANCE, balance).apply()
    }

    override fun getTotalEarned(): Int {
        return prefs.getInt(KEY_TOTAL_EARNED, DEFAULT_BALANCE)
    }

    override fun setTotalEarned(total: Int) {
        prefs.edit().putInt(KEY_TOTAL_EARNED, total).apply()
    }

    override fun getTotalSpent(): Int {
        return prefs.getInt(KEY_TOTAL_SPENT, 0)
    }

    override fun setTotalSpent(total: Int) {
        prefs.edit().putInt(KEY_TOTAL_SPENT, total).apply()
    }

    override fun getLastDailyReward(): Long? {
        val value = prefs.getLong(KEY_LAST_DAILY_REWARD, -1L)
        return if (value == -1L) null else value
    }

    override fun setLastDailyReward(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_DAILY_REWARD, timestamp).apply()
    }

    override fun getFirstOpenDate(): Long {
        return prefs.getLong(KEY_FIRST_OPEN_DATE, 0L)
    }

    override fun setFirstOpenDate(timestamp: Long) {
        prefs.edit().putLong(KEY_FIRST_OPEN_DATE, timestamp).apply()
    }

    override fun getLowHoneyWarningShown(): Boolean {
        return prefs.getBoolean(KEY_LOW_WARNING_SHOWN, false)
    }

    override fun setLowHoneyWarningShown(shown: Boolean) {
        prefs.edit().putBoolean(KEY_LOW_WARNING_SHOWN, shown).apply()
    }

    override fun getUnlockedFeatures(): Set<String> {
        return prefs.getStringSet(KEY_UNLOCKED_FEATURES, emptySet()) ?: emptySet()
    }

    override fun addUnlockedFeature(featureId: String) {
        val current = getUnlockedFeatures().toMutableSet()
        current.add(featureId)
        prefs.edit().putStringSet(KEY_UNLOCKED_FEATURES, current).apply()
    }

    override fun getTotalHabitsCreated(): Int {
        return prefs.getInt(KEY_TOTAL_HABITS_CREATED, 0)
    }

    override fun incrementTotalHabitsCreated() {
        val current = getTotalHabitsCreated()
        prefs.edit().putInt(KEY_TOTAL_HABITS_CREATED, current + 1).apply()
    }
}
