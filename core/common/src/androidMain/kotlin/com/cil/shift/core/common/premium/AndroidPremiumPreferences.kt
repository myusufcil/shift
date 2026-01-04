package com.cil.shift.core.common.premium

import android.content.Context
import android.content.SharedPreferences
import kotlinx.datetime.Instant

class AndroidPremiumPreferences(context: Context) : PremiumPreferences {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "shift_premium_prefs",
        Context.MODE_PRIVATE
    )

    override fun getPremiumStatus(): PremiumStatus {
        val isPremium = prefs.getBoolean(KEY_IS_PREMIUM, false)
        val subscriptionTypeStr = prefs.getString(KEY_SUBSCRIPTION_TYPE, SubscriptionType.FREE.name)
        val subscriptionType = try {
            SubscriptionType.valueOf(subscriptionTypeStr ?: SubscriptionType.FREE.name)
        } catch (e: Exception) {
            SubscriptionType.FREE
        }

        val expirationTimestamp = prefs.getLong(KEY_EXPIRATION_DATE, -1L)
        val expirationDate = if (expirationTimestamp > 0) {
            Instant.fromEpochMilliseconds(expirationTimestamp)
        } else null

        val trialUsed = prefs.getBoolean(KEY_TRIAL_USED, false)
        val trialExpirationTimestamp = prefs.getLong(KEY_TRIAL_EXPIRATION_DATE, -1L)
        val trialExpirationDate = if (trialExpirationTimestamp > 0) {
            Instant.fromEpochMilliseconds(trialExpirationTimestamp)
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
        prefs.edit().apply {
            putBoolean(KEY_IS_PREMIUM, status.isPremium)
            putString(KEY_SUBSCRIPTION_TYPE, status.subscriptionType.name)

            if (status.expirationDate != null) {
                putLong(KEY_EXPIRATION_DATE, status.expirationDate.toEpochMilliseconds())
            } else {
                remove(KEY_EXPIRATION_DATE)
            }

            putBoolean(KEY_TRIAL_USED, status.trialUsed)

            if (status.trialExpirationDate != null) {
                putLong(KEY_TRIAL_EXPIRATION_DATE, status.trialExpirationDate.toEpochMilliseconds())
            } else {
                remove(KEY_TRIAL_EXPIRATION_DATE)
            }

            apply()
        }
    }

    companion object {
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_SUBSCRIPTION_TYPE = "subscription_type"
        private const val KEY_EXPIRATION_DATE = "expiration_date"
        private const val KEY_TRIAL_USED = "trial_used"
        private const val KEY_TRIAL_EXPIRATION_DATE = "trial_expiration_date"
    }
}
