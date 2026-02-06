package com.cil.shift.core.common.purchase

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.CustomerInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Premium subscription state
 */
sealed class PremiumState {
    data object Loading : PremiumState()
    data object NotPremium : PremiumState()
    data object Premium : PremiumState()
    data class Error(val message: String) : PremiumState()
}

/**
 * Purchase result
 */
sealed class PurchaseResult {
    data class Success(val customerInfo: CustomerInfo) : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
    data object Cancelled : PurchaseResult()
}

/**
 * Available premium packages
 */
data class PremiumPackages(
    val monthly: Package? = null,
    val yearly: Package? = null,
    val lifetime: Package? = null
)

/**
 * Manages RevenueCat purchases and subscriptions
 */
class PurchaseManager {

    private val _premiumState = MutableStateFlow<PremiumState>(PremiumState.Loading)
    val premiumState: StateFlow<PremiumState> = _premiumState.asStateFlow()

    private val _packages = MutableStateFlow<PremiumPackages?>(null)
    val packages: StateFlow<PremiumPackages?> = _packages.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val isPremium: Boolean
        get() = _premiumState.value is PremiumState.Premium

    fun clearError() {
        _errorMessage.value = null
    }

    companion object {
        // RevenueCat entitlement identifier (must match RevenueCat dashboard)
        const val PREMIUM_ENTITLEMENT = "Shift Pro"

        // Package identifiers
        const val PACKAGE_MONTHLY = "\$rc_monthly"
        const val PACKAGE_YEARLY = "\$rc_annual"
        const val PACKAGE_LIFETIME = "\$rc_lifetime"
    }

    /**
     * Initialize RevenueCat and check current subscription status
     */
    suspend fun initialize() {
        try {
            checkPremiumStatus()
            loadOfferings()
        } catch (e: Exception) {
            _premiumState.value = PremiumState.Error(e.message ?: "Failed to initialize")
        }
    }

    /**
     * Set the user ID for cross-platform sync (call after Firebase Auth login)
     */
    suspend fun login(userId: String) {
        try {
            val result = suspendCancellableCoroutine { continuation ->
                Purchases.sharedInstance.logIn(
                    newAppUserID = userId,
                    onError = { error ->
                        continuation.resumeWithException(Exception(error.message))
                    },
                    onSuccess = { customerInfo, created ->
                        continuation.resume(customerInfo)
                    }
                )
            }
            updatePremiumState(result)
        } catch (e: Exception) {
            _premiumState.value = PremiumState.Error(e.message ?: "Login failed")
        }
    }

    /**
     * Logout from RevenueCat (call when Firebase Auth signs out)
     * Always resets premium state to NotPremium after logout,
     * regardless of device-level subscriptions. User must log back in
     * to restore their premium status.
     */
    suspend fun logout() {
        try {
            suspendCancellableCoroutine<Unit> { continuation ->
                Purchases.sharedInstance.logOut(
                    onError = { error ->
                        continuation.resumeWithException(Exception(error.message))
                    },
                    onSuccess = { _ ->
                        continuation.resume(Unit)
                    }
                )
            }
        } catch (e: Exception) {
            // Ignore logout errors
        } finally {
            // Always reset to NotPremium after logout
            // User must log back in to restore premium status
            _premiumState.value = PremiumState.NotPremium
        }
    }

    /**
     * Check current premium status
     */
    suspend fun checkPremiumStatus() {
        try {
            val customerInfo = suspendCancellableCoroutine { continuation ->
                Purchases.sharedInstance.getCustomerInfo(
                    onError = { error ->
                        continuation.resumeWithException(Exception(error.message))
                    },
                    onSuccess = { customerInfo ->
                        continuation.resume(customerInfo)
                    }
                )
            }
            updatePremiumState(customerInfo)
        } catch (e: Exception) {
            _premiumState.value = PremiumState.Error(e.message ?: "Failed to check status")
        }
    }

    /**
     * Load available offerings/packages
     */
    suspend fun loadOfferings() {
        try {
            val offerings = suspendCancellableCoroutine { continuation ->
                Purchases.sharedInstance.getOfferings(
                    onError = { error ->
                        continuation.resumeWithException(Exception(error.message))
                    },
                    onSuccess = { offerings ->
                        continuation.resume(offerings)
                    }
                )
            }

            offerings.current?.let { currentOffering ->
                val monthly = currentOffering.availablePackages.find {
                    it.identifier == PACKAGE_MONTHLY
                }
                val yearly = currentOffering.availablePackages.find {
                    it.identifier == PACKAGE_YEARLY
                }
                val lifetime = currentOffering.availablePackages.find {
                    it.identifier == PACKAGE_LIFETIME
                }

                _packages.value = PremiumPackages(
                    monthly = monthly,
                    yearly = yearly,
                    lifetime = lifetime
                )
            }
        } catch (_: Exception) {
            // Don't show loadOfferings errors as purchase errors
            // Packages will remain null and UI will handle gracefully
        }
    }

    /**
     * Purchase a package
     */
    suspend fun purchase(packageToPurchase: Package, onResult: (PurchaseResult) -> Unit) {
        try {
            Purchases.sharedInstance.purchase(
                packageToPurchase = packageToPurchase,
                onError = { error, userCancelled ->
                    if (userCancelled) {
                        onResult(PurchaseResult.Cancelled)
                    } else {
                        onResult(PurchaseResult.Error(error.message))
                    }
                },
                onSuccess = { transaction, customerInfo ->
                    updatePremiumState(customerInfo)
                    onResult(PurchaseResult.Success(customerInfo))
                }
            )
        } catch (e: Exception) {
            onResult(PurchaseResult.Error(e.message ?: "Purchase failed"))
        }
    }

    /**
     * Restore previous purchases
     */
    suspend fun restorePurchases(onResult: (PurchaseResult) -> Unit) {
        try {
            Purchases.sharedInstance.restorePurchases(
                onError = { error ->
                    onResult(PurchaseResult.Error(error.message))
                },
                onSuccess = { customerInfo ->
                    updatePremiumState(customerInfo)
                    onResult(PurchaseResult.Success(customerInfo))
                }
            )
        } catch (e: Exception) {
            onResult(PurchaseResult.Error(e.message ?: "Restore failed"))
        }
    }

    private fun updatePremiumState(customerInfo: CustomerInfo) {
        val hasPremium = customerInfo.entitlements[PREMIUM_ENTITLEMENT]?.isActive == true
        _premiumState.value = if (hasPremium) {
            PremiumState.Premium
        } else {
            PremiumState.NotPremium
        }
    }
}
