package com.cil.shift.core.common.premium

import kotlinx.datetime.Instant

/**
 * Premium subscription status
 */
data class PremiumStatus(
    val isPremium: Boolean = false,
    val subscriptionType: SubscriptionType = SubscriptionType.FREE,
    val expirationDate: Instant? = null,
    val trialUsed: Boolean = false,
    val trialExpirationDate: Instant? = null
) {
    val isActive: Boolean
        get() = isPremium && (expirationDate == null || expirationDate > kotlinx.datetime.Clock.System.now())

    val isTrial: Boolean
        get() = subscriptionType == SubscriptionType.TRIAL && isActive

    val isLifetime: Boolean
        get() = subscriptionType == SubscriptionType.LIFETIME
}

enum class SubscriptionType {
    FREE,
    TRIAL,           // 7-day free trial
    MONTHLY,         // Monthly subscription
    YEARLY,          // Yearly subscription (discounted)
    LIFETIME         // One-time purchase
}

/**
 * Premium features enumeration
 */
enum class PremiumFeature {
    UNLIMITED_HABITS,           // Free: 10 habits max, Premium: unlimited
    ADVANCED_STATISTICS,        // Premium: charts, insights, trends
    CUSTOM_THEMES,              // Premium: color themes and appearance
    CLOUD_SYNC,                 // Premium: sync across devices
    EXPORT_DATA,                // Premium: export to CSV/PDF
    HABIT_TEMPLATES,            // Premium: pre-made habit templates
    CUSTOM_REMINDERS,           // Premium: multiple reminders per habit
    WIDGETS,                    // Premium: home screen widgets
    AD_FREE;                    // Premium: no advertisements

    fun isAvailableFor(status: PremiumStatus): Boolean {
        return when (this) {
            // All features require premium
            else -> status.isActive
        }
    }
}

/**
 * Premium plan details
 */
sealed class PremiumPlan(
    val type: SubscriptionType,
    val price: String,
    val duration: String,
    val features: List<PremiumFeature>
) {
    object Trial : PremiumPlan(
        type = SubscriptionType.TRIAL,
        price = "Free",
        duration = "7 days",
        features = PremiumFeature.entries
    )

    object Monthly : PremiumPlan(
        type = SubscriptionType.MONTHLY,
        price = "$4.99",
        duration = "per month",
        features = PremiumFeature.entries
    )

    object Yearly : PremiumPlan(
        type = SubscriptionType.YEARLY,
        price = "$39.99",
        duration = "per year",
        features = PremiumFeature.entries
    ) {
        val savings = "Save 33%"
    }

    object Lifetime : PremiumPlan(
        type = SubscriptionType.LIFETIME,
        price = "$99.99",
        duration = "one-time",
        features = PremiumFeature.entries
    ) {
        val tag = "Best Value"
    }
}
