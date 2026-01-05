import SwiftUI
import RevenueCat

@main
struct iOSApp: App {

    init() {
        // Configure RevenueCat SDK
        initRevenueCat()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }

    private func initRevenueCat() {
        // Enable debug logs in debug builds
        #if DEBUG
        Purchases.logLevel = .debug
        #endif

        // Configure RevenueCat with your App Store API key
        // Get your API key from: https://app.revenuecat.com/apps -> Your App -> API Keys
        Purchases.configure(withAPIKey: Self.revenueCatApiKey)
    }

    private static let revenueCatApiKey = "test_vjvuzIodDCVnrSumhQWtVwEpKk"
}
