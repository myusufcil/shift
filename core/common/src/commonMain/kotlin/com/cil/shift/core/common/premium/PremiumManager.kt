package com.cil.shift.core.common.premium

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

class PremiumManager(
    private val preferences: PremiumPreferences
) {
    private val _premiumStatus = MutableStateFlow(PremiumStatus())
    val premiumStatus: StateFlow<PremiumStatus> = _premiumStatus.asStateFlow()

    init {
        loadPremiumStatus()
    }

    private fun loadPremiumStatus() {
        val savedStatus = preferences.getPremiumStatus()
        _premiumStatus.value = savedStatus

        // Check if subscription expired
        if (savedStatus.isActive && savedStatus.expirationDate != null) {
            val now = Clock.System.now()
            if (savedStatus.expirationDate < now) {
                // Subscription expired, revert to free
                updatePremiumStatus(PremiumStatus())
            }
        }
    }

    fun updatePremiumStatus(status: PremiumStatus) {
        _premiumStatus.value = status
        preferences.savePremiumStatus(status)
    }

    fun startTrial() {
        if (_premiumStatus.value.trialUsed) {
            // Trial already used
            return
        }

        val now = Clock.System.now()
        val expirationDate = now.plus(kotlin.time.Duration.parse("P7D"))

        val trialStatus = PremiumStatus(
            isPremium = true,
            subscriptionType = SubscriptionType.TRIAL,
            expirationDate = expirationDate,
            trialUsed = true,
            trialExpirationDate = expirationDate
        )

        updatePremiumStatus(trialStatus)
    }

    fun subscribe(plan: PremiumPlan) {
        val now = Clock.System.now()
        val expirationDate = when (plan.type) {
            SubscriptionType.MONTHLY -> now.plus(kotlin.time.Duration.parse("P30D"))
            SubscriptionType.YEARLY -> now.plus(kotlin.time.Duration.parse("P365D"))
            SubscriptionType.LIFETIME -> null // Never expires
            else -> null
        }

        val premiumStatus = PremiumStatus(
            isPremium = true,
            subscriptionType = plan.type,
            expirationDate = expirationDate,
            trialUsed = _premiumStatus.value.trialUsed,
            trialExpirationDate = _premiumStatus.value.trialExpirationDate
        )

        updatePremiumStatus(premiumStatus)
    }

    fun cancelSubscription() {
        updatePremiumStatus(
            PremiumStatus(
                trialUsed = _premiumStatus.value.trialUsed,
                trialExpirationDate = _premiumStatus.value.trialExpirationDate
            )
        )
    }

    fun hasFeature(feature: PremiumFeature): Boolean {
        return feature.isAvailableFor(_premiumStatus.value)
    }

    fun restorePurchases() {
        // Platform-specific implementation will handle this
        // For now, just reload from preferences
        loadPremiumStatus()
    }
}

/**
 * Interface for platform-specific premium preferences storage
 */
interface PremiumPreferences {
    fun getPremiumStatus(): PremiumStatus
    fun savePremiumStatus(status: PremiumStatus)
}
