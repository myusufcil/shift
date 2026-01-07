package com.cil.shift

import android.app.Application
import com.cil.shift.core.common.widget.WidgetNotifier
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.configure

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
        // Using the KMP extension function that auto-detects Android context
        Purchases.configure(apiKey = REVENUECAT_API_KEY) {
            // Optional configuration
        }
    }

    companion object {
        private const val REVENUECAT_API_KEY = "goog_bZnpedRehibcaYPThfoT0GwxEaL"
    }
}
