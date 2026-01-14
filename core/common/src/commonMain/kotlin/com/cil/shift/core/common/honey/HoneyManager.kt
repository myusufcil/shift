package com.cil.shift.core.common.honey

import com.cil.shift.core.common.purchase.PurchaseManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock

/**
 * Represents the current honey status of the user.
 */
data class HoneyStatus(
    val balance: Int = 50,
    val totalEarned: Int = 50,
    val totalSpent: Int = 0,
    val lastDailyReward: Long? = null,
    val firstOpenDate: Long = 0,
    val unlockedFeatures: Set<String> = emptySet()
) {
    val isLowBalance: Boolean get() = balance < 20
    val isCriticalBalance: Boolean get() = balance < 10
    val isEmpty: Boolean get() = balance <= 0
}

/**
 * Reasons for earning honey.
 */
enum class HoneyReason(val amount: Int, val descriptionKey: String) {
    INITIAL_BONUS(50, "honey_initial_bonus"),
    DAILY_LOGIN(2, "honey_daily_login"),
    HABIT_COMPLETED(1, "honey_habit_completed"),
    STREAK_7_DAYS(10, "honey_streak_7"),
    STREAK_30_DAYS(30, "honey_streak_30"),
    STREAK_100_DAYS(100, "honey_streak_100"),
    REFERRAL(20, "honey_referral"),
    WATCH_AD(5, "honey_watch_ad")
}

/**
 * Features that cost honey to use.
 */
enum class HoneyFeature(val cost: Int, val isOneTime: Boolean = false) {
    CREATE_HABIT_4TH(10, false),
    CREATE_HABIT_5TH(15, false),
    CREATE_HABIT_6TH_PLUS(20, false),
    CUSTOM_ICON(5, false),
    DETAILED_STATS(3, false),
    WIDGET_UNLOCK(15, true),
    THEME_DARK(0, true),      // Default theme - free
    THEME_LIGHT(10, true),
    THEME_MIDNIGHT(10, true),
    EXPORT_DATA(20, false)
}

/**
 * Result of checking if a feature can be used.
 */
sealed class HoneyCheckResult {
    data object Free : HoneyCheckResult()
    data object PremiumUser : HoneyCheckResult()
    data object AlreadyUnlocked : HoneyCheckResult()
    data class CanAfford(val feature: HoneyFeature) : HoneyCheckResult()
    data class NotEnoughHoney(val feature: HoneyFeature, val balance: Int) : HoneyCheckResult()
}

/**
 * Manages the honey credit system for the app.
 * Honey is earned through engagement and spent on premium features.
 */
class HoneyManager(
    private val preferences: HoneyPreferences,
    private val purchaseManager: PurchaseManager
) {
    companion object {
        private const val INITIAL_HONEY = 50
        private const val HONEYMOON_DAYS = 7
        private const val DAILY_REWARD_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 hours
    }

    private val _honeyStatus = MutableStateFlow(HoneyStatus())
    val honeyStatus: StateFlow<HoneyStatus> = _honeyStatus.asStateFlow()

    init {
        loadHoneyStatus()
    }

    /**
     * Load honey status from preferences.
     */
    private fun loadHoneyStatus() {
        val firstOpen = preferences.getFirstOpenDate()
        val now = Clock.System.now().toEpochMilliseconds()

        // First time user - initialize with bonus
        if (firstOpen == 0L) {
            preferences.setFirstOpenDate(now)
            preferences.setBalance(INITIAL_HONEY)
            preferences.setTotalEarned(INITIAL_HONEY)
        }

        _honeyStatus.value = HoneyStatus(
            balance = preferences.getBalance(),
            totalEarned = preferences.getTotalEarned(),
            totalSpent = preferences.getTotalSpent(),
            lastDailyReward = preferences.getLastDailyReward(),
            firstOpenDate = preferences.getFirstOpenDate(),
            unlockedFeatures = preferences.getUnlockedFeatures()
        )
    }

    /**
     * Check if user is in honeymoon period (first 7 days).
     * During honeymoon, no prompts are shown.
     */
    fun isInHoneymoonPeriod(): Boolean {
        val firstOpen = _honeyStatus.value.firstOpenDate
        if (firstOpen == 0L) return true

        val now = Clock.System.now().toEpochMilliseconds()
        val daysSinceFirstOpen = (now - firstOpen) / (24 * 60 * 60 * 1000L)
        return daysSinceFirstOpen < HONEYMOON_DAYS
    }

    /**
     * Check if user is premium (bypasses all honey requirements).
     */
    fun isPremium(): Boolean {
        return purchaseManager.isPremium
    }

    /**
     * Add honey to the user's balance.
     */
    fun addHoney(reason: HoneyReason): Int {
        if (isPremium()) return 0 // Premium users don't need honey

        val amount = reason.amount
        val newBalance = _honeyStatus.value.balance + amount
        val newTotalEarned = _honeyStatus.value.totalEarned + amount

        preferences.setBalance(newBalance)
        preferences.setTotalEarned(newTotalEarned)

        _honeyStatus.update {
            it.copy(
                balance = newBalance,
                totalEarned = newTotalEarned
            )
        }

        return amount
    }

    /**
     * Spend honey on a feature.
     * Returns true if successful, false if not enough honey.
     */
    fun spendHoney(feature: HoneyFeature): Boolean {
        if (isPremium()) return true

        // Check if already unlocked (for one-time purchases)
        if (feature.isOneTime && isFeatureUnlocked(feature)) {
            return true
        }

        val cost = feature.cost
        if (_honeyStatus.value.balance < cost) {
            return false
        }

        val newBalance = _honeyStatus.value.balance - cost
        val newTotalSpent = _honeyStatus.value.totalSpent + cost

        preferences.setBalance(newBalance)
        preferences.setTotalSpent(newTotalSpent)

        // Mark as unlocked if one-time
        if (feature.isOneTime) {
            preferences.addUnlockedFeature(feature.name)
        }

        _honeyStatus.update {
            it.copy(
                balance = newBalance,
                totalSpent = newTotalSpent,
                unlockedFeatures = if (feature.isOneTime) {
                    it.unlockedFeatures + feature.name
                } else it.unlockedFeatures
            )
        }

        return true
    }

    /**
     * Check if user can afford a feature.
     */
    fun checkFeature(feature: HoneyFeature): HoneyCheckResult {
        if (isPremium()) return HoneyCheckResult.PremiumUser

        if (feature.isOneTime && isFeatureUnlocked(feature)) {
            return HoneyCheckResult.AlreadyUnlocked
        }

        if (feature.cost == 0) return HoneyCheckResult.Free

        return if (_honeyStatus.value.balance >= feature.cost) {
            HoneyCheckResult.CanAfford(feature)
        } else {
            HoneyCheckResult.NotEnoughHoney(feature, _honeyStatus.value.balance)
        }
    }

    /**
     * Check if a one-time feature has been unlocked.
     */
    fun isFeatureUnlocked(feature: HoneyFeature): Boolean {
        if (isPremium()) return true
        if (!feature.isOneTime) return false
        return _honeyStatus.value.unlockedFeatures.contains(feature.name)
    }

    /**
     * Check and award daily login reward.
     * Returns the amount awarded, or null if not eligible.
     */
    fun checkDailyReward(): Int? {
        if (isPremium()) return null

        val lastReward = _honeyStatus.value.lastDailyReward
        val now = Clock.System.now().toEpochMilliseconds()

        // Check if 24 hours have passed since last reward
        if (lastReward != null && (now - lastReward) < DAILY_REWARD_INTERVAL_MS) {
            return null
        }

        // Award daily reward
        preferences.setLastDailyReward(now)
        _honeyStatus.update { it.copy(lastDailyReward = now) }

        return addHoney(HoneyReason.DAILY_LOGIN)
    }

    /**
     * Get total habits ever created (doesn't decrease when deleted).
     */
    fun getTotalHabitsCreated(): Int {
        return preferences.getTotalHabitsCreated()
    }

    /**
     * Increment total habits created counter.
     * Call this after successfully creating a habit.
     */
    fun incrementTotalHabitsCreated() {
        preferences.incrementTotalHabitsCreated()
    }

    /**
     * Check habit creation eligibility based on TOTAL habits ever created.
     * Uses total created count, not current active count, to prevent
     * users from gaming the system by deleting and recreating habits.
     */
    fun checkHabitCreation(currentHabitCount: Int): HoneyCheckResult {
        if (isPremium()) return HoneyCheckResult.PremiumUser

        // Use total habits ever created, not current count
        val totalCreated = getTotalHabitsCreated()

        return when {
            totalCreated < 3 -> HoneyCheckResult.Free
            totalCreated == 3 -> checkFeature(HoneyFeature.CREATE_HABIT_4TH)
            totalCreated == 4 -> checkFeature(HoneyFeature.CREATE_HABIT_5TH)
            else -> checkFeature(HoneyFeature.CREATE_HABIT_6TH_PLUS)
        }
    }

    /**
     * Get the honey cost for creating a habit based on TOTAL habits ever created.
     */
    fun getHabitCreationCost(currentHabitCount: Int): Int? {
        if (isPremium()) return null

        // Use total habits ever created, not current count
        val totalCreated = getTotalHabitsCreated()

        return when {
            totalCreated < 3 -> null // Free
            totalCreated == 3 -> HoneyFeature.CREATE_HABIT_4TH.cost
            totalCreated == 4 -> HoneyFeature.CREATE_HABIT_5TH.cost
            else -> HoneyFeature.CREATE_HABIT_6TH_PLUS.cost
        }
    }

    /**
     * Check if low honey warning should be shown (only once).
     */
    fun shouldShowLowHoneyWarning(): Boolean {
        if (isPremium()) return false
        if (isInHoneymoonPeriod()) return false
        if (preferences.getLowHoneyWarningShown()) return false
        if (!_honeyStatus.value.isCriticalBalance) return false

        return true
    }

    /**
     * Mark low honey warning as shown.
     */
    fun markLowHoneyWarningShown() {
        preferences.setLowHoneyWarningShown(true)
    }

    /**
     * Award streak bonus.
     */
    fun awardStreakBonus(streakDays: Int): Int? {
        if (isPremium()) return null

        val reason = when (streakDays) {
            7 -> HoneyReason.STREAK_7_DAYS
            30 -> HoneyReason.STREAK_30_DAYS
            100 -> HoneyReason.STREAK_100_DAYS
            else -> return null
        }

        return addHoney(reason)
    }
}
