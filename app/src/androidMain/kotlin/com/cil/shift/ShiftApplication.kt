package com.cil.shift

import android.app.Application
import com.cil.shift.core.common.widget.WidgetNotifier
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.LogLevel

class ShiftApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize WidgetNotifier for widget updates
        WidgetNotifier.initialize(this)

        // Configure RevenueCat SDK
        initRevenueCat()
    }

    private fun initRevenueCat() {
        // Enable debug logs in debug builds
        Purchases.logLevel = LogLevel.DEBUG

        // Configure RevenueCat with your Google Play API key
        // Get your API key from: https://app.revenuecat.com/apps -> Your App -> API Keys
        Purchases.configure(
            configuration = PurchasesConfiguration(apiKey = REVENUECAT_API_KEY) {
                // Optional: Set app user ID if you already have the user logged in
                // appUserId = firebaseUserId
            }
        )
    }

    companion object {
        private const val REVENUECAT_API_KEY = "test_vjvuzIodDCVnrSumhQWtVwEpKk"
    }
}
