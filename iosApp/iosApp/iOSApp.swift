import SwiftUI
import RevenueCat
import FirebaseCore
import UserNotifications

// AppDelegate for handling notifications
class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Set notification delegate to handle foreground notifications
        UNUserNotificationCenter.current().delegate = self
        return true
    }

    // Called when notification is received while app is in foreground
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        // Show notification even when app is in foreground
        completionHandler([.banner, .sound, .badge])
    }

    // Called when user taps on notification
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        // Handle notification tap
        let identifier = response.notification.request.identifier
        print("iOS: User tapped notification with identifier: \(identifier)")
        completionHandler()
    }
}

@main
struct iOSApp: App {
    // Connect AppDelegate to SwiftUI app
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        // Configure Firebase first (required for Auth)
        FirebaseApp.configure()

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

        // Configure RevenueCat with API key from Info.plist (injected via xcconfig)
        guard let apiKey = Bundle.main.object(forInfoDictionaryKey: "RevenueCatApiKey") as? String, !apiKey.isEmpty else {
            fatalError("RevenueCatApiKey not found in Info.plist. Ensure Secrets.xcconfig is configured.")
        }
        Purchases.configure(withAPIKey: apiKey)
    }
}
