package com.cil.shift

import android.app.Application
import com.cil.shift.core.common.AppVersionHolder
import com.cil.shift.core.common.widget.WidgetNotifier
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.PurchasesConfiguration

class ShiftApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize app version from BuildConfig
        AppVersionHolder.version = packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"

        // Initialize WidgetNotifier for widget updates
        WidgetNotifier.initialize(this)

        // Configure RevenueCat SDK
        initRevenueCat()
    }

    private fun initRevenueCat() {
        // Enable debug logs in debug builds
        Purchases.logLevel = LogLevel.DEBUG

        // Configure RevenueCat
        Purchases.configure(
            configuration = PurchasesConfiguration(apiKey = REVENUECAT_API_KEY) {
                // Optional configuration
            }
        )
    }

    companion object {
        private const val REVENUECAT_API_KEY = "goog_bZnpedRehibcaYPThfoTOGwxEaL"
    }
}
