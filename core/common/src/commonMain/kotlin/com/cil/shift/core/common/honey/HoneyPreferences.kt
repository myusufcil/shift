package com.cil.shift.core.common.honey

/**
 * Interface for storing and retrieving honey-related preferences.
 * Platform-specific implementations handle the actual storage.
 */
interface HoneyPreferences {
    fun getBalance(): Int
    fun setBalance(balance: Int)

    fun getTotalEarned(): Int
    fun setTotalEarned(total: Int)

    fun getTotalSpent(): Int
    fun setTotalSpent(total: Int)

    fun getLastDailyReward(): Long?
    fun setLastDailyReward(timestamp: Long)

    fun getFirstOpenDate(): Long
    fun setFirstOpenDate(timestamp: Long)

    fun getLowHoneyWarningShown(): Boolean
    fun setLowHoneyWarningShown(shown: Boolean)

    // Track unlocked features (one-time purchases)
    fun getUnlockedFeatures(): Set<String>
    fun addUnlockedFeature(featureId: String)
}
