package com.cil.shift.core.common.premium

import kotlinx.datetime.Instant
import platform.Foundation.NSUserDefaults

class IOSPremiumPreferences : PremiumPreferences {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun getPremiumStatus(): PremiumStatus {
        val isPremium = userDefaults.boolForKey(KEY_IS_PREMIUM)
        val subscriptionTypeStr = userDefaults.stringForKey(KEY_SUBSCRIPTION_TYPE)
        val subscriptionType = try {
            SubscriptionType.valueOf(subscriptionTypeStr ?: SubscriptionType.FREE.name)
        } catch (e: Exception) {
            SubscriptionType.FREE
        }

        val expirationTimestamp = userDefaults.doubleForKey(KEY_EXPIRATION_DATE)
        val expirationDate = if (expirationTimestamp > 0) {
            Instant.fromEpochMilliseconds(expirationTimestamp.toLong())
        } else null

        val trialUsed = userDefaults.boolForKey(KEY_TRIAL_USED)
        val trialExpirationTimestamp = userDefaults.doubleForKey(KEY_TRIAL_EXPIRATION_DATE)
        val trialExpirationDate = if (trialExpirationTimestamp > 0) {
            Instant.fromEpochMilliseconds(trialExpirationTimestamp.toLong())
        } else null

        return PremiumStatus(
            isPremium = isPremium,
            subscriptionType = subscriptionType,
            expirationDate = expirationDate,
            trialUsed = trialUsed,
            trialExpirationDate = trialExpirationDate
        )
    }

    override fun savePremiumStatus(status: PremiumStatus) {
        userDefaults.setBool(status.isPremium, KEY_IS_PREMIUM)
        userDefaults.setObject(status.subscriptionType.name, KEY_SUBSCRIPTION_TYPE)

        if (status.expirationDate != null) {
            userDefaults.setDouble(status.expirationDate.toEpochMilliseconds().toDouble(), KEY_EXPIRATION_DATE)
        } else {
            userDefaults.removeObjectForKey(KEY_EXPIRATION_DATE)
        }

        userDefaults.setBool(status.trialUsed, KEY_TRIAL_USED)

        if (status.trialExpirationDate != null) {
            userDefaults.setDouble(status.trialExpirationDate.toEpochMilliseconds().toDouble(), KEY_TRIAL_EXPIRATION_DATE)
        } else {
            userDefaults.removeObjectForKey(KEY_TRIAL_EXPIRATION_DATE)
        }

        userDefaults.synchronize()
    }

    companion object {
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_SUBSCRIPTION_TYPE = "subscription_type"
        private const val KEY_EXPIRATION_DATE = "expiration_date"
        private const val KEY_TRIAL_USED = "trial_used"
        private const val KEY_TRIAL_EXPIRATION_DATE = "trial_expiration_date"
    }
}
