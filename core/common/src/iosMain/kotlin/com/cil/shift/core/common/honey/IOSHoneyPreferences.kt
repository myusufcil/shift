package com.cil.shift.core.common.honey

import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of HoneyPreferences using NSUserDefaults.
 */
class IOSHoneyPreferences : HoneyPreferences {

    private val defaults = NSUserDefaults.standardUserDefaults

    companion object {
        private const val KEY_BALANCE = "honey_balance"
        private const val KEY_TOTAL_EARNED = "honey_total_earned"
        private const val KEY_TOTAL_SPENT = "honey_total_spent"
        private const val KEY_LAST_DAILY_REWARD = "honey_last_daily"
        private const val KEY_FIRST_OPEN_DATE = "honey_first_open"
        private const val KEY_LOW_WARNING_SHOWN = "honey_low_warning_shown"
        private const val KEY_UNLOCKED_FEATURES = "honey_unlocked_features"
        private const val KEY_INITIALIZED = "honey_initialized"

        private const val DEFAULT_BALANCE = 50
    }

    init {
        // Initialize balance on first run
        if (!defaults.boolForKey(KEY_INITIALIZED)) {
            defaults.setInteger(DEFAULT_BALANCE.toLong(), KEY_BALANCE)
            defaults.setInteger(DEFAULT_BALANCE.toLong(), KEY_TOTAL_EARNED)
            defaults.setBool(true, KEY_INITIALIZED)
        }
    }

    override fun getBalance(): Int {
        val value = defaults.integerForKey(KEY_BALANCE)
        return if (value == 0L && !defaults.boolForKey(KEY_INITIALIZED)) DEFAULT_BALANCE else value.toInt()
    }

    override fun setBalance(balance: Int) {
        defaults.setInteger(balance.toLong(), KEY_BALANCE)
    }

    override fun getTotalEarned(): Int {
        val value = defaults.integerForKey(KEY_TOTAL_EARNED)
        return if (value == 0L) DEFAULT_BALANCE else value.toInt()
    }

    override fun setTotalEarned(total: Int) {
        defaults.setInteger(total.toLong(), KEY_TOTAL_EARNED)
    }

    override fun getTotalSpent(): Int {
        return defaults.integerForKey(KEY_TOTAL_SPENT).toInt()
    }

    override fun setTotalSpent(total: Int) {
        defaults.setInteger(total.toLong(), KEY_TOTAL_SPENT)
    }

    override fun getLastDailyReward(): Long? {
        val value = defaults.doubleForKey(KEY_LAST_DAILY_REWARD)
        return if (value == 0.0) null else value.toLong()
    }

    override fun setLastDailyReward(timestamp: Long) {
        defaults.setDouble(timestamp.toDouble(), KEY_LAST_DAILY_REWARD)
    }

    override fun getFirstOpenDate(): Long {
        return defaults.doubleForKey(KEY_FIRST_OPEN_DATE).toLong()
    }

    override fun setFirstOpenDate(timestamp: Long) {
        defaults.setDouble(timestamp.toDouble(), KEY_FIRST_OPEN_DATE)
    }

    override fun getLowHoneyWarningShown(): Boolean {
        return defaults.boolForKey(KEY_LOW_WARNING_SHOWN)
    }

    override fun setLowHoneyWarningShown(shown: Boolean) {
        defaults.setBool(shown, KEY_LOW_WARNING_SHOWN)
    }

    override fun getUnlockedFeatures(): Set<String> {
        val array = defaults.stringArrayForKey(KEY_UNLOCKED_FEATURES)
        return array?.filterIsInstance<String>()?.toSet() ?: emptySet()
    }

    override fun addUnlockedFeature(featureId: String) {
        val current = getUnlockedFeatures().toMutableSet()
        current.add(featureId)
        defaults.setObject(current.toList(), KEY_UNLOCKED_FEATURES)
    }
}
